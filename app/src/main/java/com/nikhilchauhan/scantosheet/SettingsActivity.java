package com.nikhilchauhan.scantosheet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ConnectivityManager connectivityManager;
    BillingProcessor billingProcessor;
    InterstitialAd mAdView;

    ArrayList<String> scannedList;
    ArrayList<String> listDescOfItem;
    ArrayList<String> listNoteOfItem;

    boolean flagPurchased;

    Button id_btnPremium;
    EditText id_nameOfSheet;
    EditText id_urlOfSheet;
//    Switch id_soundOnScan;
    Switch id_vibrateOnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        pref = getApplicationContext().getSharedPreferences("mySharedPref", 0); // 0 - for private mode
        editor = pref.edit();

        flagPurchased =pref.getBoolean("flagPurchased", false);

        billingProcessor = BillingProcessor.newBillingProcessor(SettingsActivity.this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgweHIkf4bnV254X8bipY9Z5ZztQACMZdm/mZDlU+KCLwwM0MCaq+lbdpBleW1NzKAAjDl3LNbe1dKwaM+5/ESRit9BnlqXxGs+7FEaFLMkz95uJHS0QN5ffDuuMEMUpYzfj9Ir2uJDp+OFwg7euKb7U2biY+k0/oBlfRK7eVGEGB/Ju9JiNUerCayyFGAXz9/Q53/oPuBetAqRWIzPX1C8VjWOUknFR4TrJi0IrNz5hzBtHgdj4hQe2FYkFSpLS/MSsX3vCN3cvqjELxgqflysbWy79c/+jxxeD9d2EMH4eAnvo56k/x4dNQRR56XLVN3j6zrvnd0rYg84VcjLEjFQIDAQAB", SettingsActivity.this);
        billingProcessor.initialize(); // binds

        scannedList = new ArrayList<>();
        listDescOfItem = new ArrayList<>();
        listNoteOfItem = new ArrayList<>();
        scannedList = (ArrayList) getIntent().getSerializableExtra("scannedList");
        listDescOfItem = (ArrayList) getIntent().getSerializableExtra("listDescOfItem");
        listNoteOfItem = (ArrayList) getIntent().getSerializableExtra("listNoteOfItem");

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setTitle("Done");

        id_nameOfSheet = findViewById(R.id.id_nameOfSheet);
        id_urlOfSheet = findViewById(R.id.id_urlOfSheet);
//        id_soundOnScan = findViewById(R.id.id_soundOnScan);
        id_vibrateOnScan = findViewById(R.id.id_vibrateOnScan);
        id_btnPremium = findViewById(R.id.id_btnPremium);


        // Setting Data to Input Boxes
        id_nameOfSheet.setText(pref.getString("nameOfSheet", "Sheet1"));
        id_urlOfSheet.setText(pref.getString("urlOfSheet", null));

        // Getting data for Switches
        if(pref.getBoolean("vibrateOnScan", true)){
            id_vibrateOnScan.setChecked(true);
        }else {
            id_vibrateOnScan.setChecked(false);
        }
//        if(pref.getBoolean("soundOnScan", false)){
//            id_soundOnScan.setChecked(true);
//        }


        // Listening for Switch Change
        id_vibrateOnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("vibrateOnScan", !pref.getBoolean("vibrateOnScan", true));
                editor.commit(); // commit changes
            }
        });
//        id_soundOnScan.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SharedPreferences.Editor editor = pref.edit();
//                editor.putBoolean("soundOnScan", !pref.getBoolean("soundOnScan", false));
//                editor.commit(); // commit changes
//            }
//        });

        if(flagPurchased){
            id_btnPremium.setText("Congratulations! you're a premium user");
            id_btnPremium.setEnabled(false);
        }

        // Load Interstitial Ads
        if(!flagPurchased){
            mAdView = new InterstitialAd(SettingsActivity.this);
            mAdView.setAdUnitId(getString(R.string.inter_settings));
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.show();
                }
            });
        }

    }



    public void saveTapped(View view){

        String nameOfSheet = id_nameOfSheet.getText().toString().trim();
        String urlOfSheet = id_urlOfSheet.getText().toString().trim();

        if ((id_nameOfSheet.getText().toString().trim()).isEmpty()||(id_urlOfSheet.getText().toString().trim()).isEmpty()){

            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Fields can't be empty!")
                    .setMessage("Please make sure that spreadsheet URL is valid & publicly editable.")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Whatever
                        }
                    }).show();

        }else{
            if(Patterns.WEB_URL.matcher(id_urlOfSheet.getText().toString().trim()).matches()){
                editor.putString("nameOfSheet", nameOfSheet); // Storing NameOfSheet
                editor.putString("urlOfSheet", urlOfSheet); // Storing UrlOfSheet
                editor.commit(); // commit changes
                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                Snackbar.make(view, "Saved successfully...", Snackbar.LENGTH_LONG).show();
            }else{
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Warning!")
                        .setMessage("Please enter a valid spreadsheet URL!...")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Whatever
                            }
                        }).show();
            }
        }
    }


    // Show Sheet
    public void showTapped(View view){
        if(pref.getString("urlOfSheet", null)!=null){
            if((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pref.getString("urlOfSheet", null)));
                startActivity(browserIntent);
            }else{
                Snackbar.make(view,"No Internet Connection!...️", Snackbar.LENGTH_LONG).show();
            }

        }else {
            Snackbar.make(view, "No spreadsheet is added!...️", Snackbar.LENGTH_LONG).show();
        }
    }


    //Premium Tapped
    public void premiumTapped(View view){
        billingProcessor.purchase(SettingsActivity.this,"scantosheet_premium");
    }


    @Override
    protected void onStart() {
        super.onStart();

    }


    // Done pressed
    @Override
    public boolean onSupportNavigateUp() {
        if(scannedList.isEmpty()){
            Intent intentMain=new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }else {
            editor.putBoolean("flagForScan", true);
            editor.commit();
            Intent intentMain = new Intent(SettingsActivity.this, MainActivity.class);
            intentMain.putExtra("scannedList", scannedList);
            intentMain.putExtra("listDescOfItem", listDescOfItem);
            intentMain.putExtra("listNoteOfItem", listNoteOfItem);
            startActivity(intentMain);
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(scannedList.isEmpty()){
            Intent intentMain=new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }else {
            editor.putBoolean("flagForScan", true);
            editor.commit();
            Intent intentMain = new Intent(SettingsActivity.this, MainActivity.class);
            intentMain.putExtra("scannedList", scannedList);
            intentMain.putExtra("listDescOfItem", listDescOfItem);
            intentMain.putExtra("listNoteOfItem", listNoteOfItem);
            startActivity(intentMain);
            finish();
        }
    }

    @Override
    public void onBillingInitialized() {
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        editor.putBoolean("flagPurchased", true);
        editor.commit();
        flagPurchased =pref.getBoolean("flagPurchased", false);
        id_btnPremium.setText("Congratulations! you're a premium user");
        id_btnPremium.setEnabled(false);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Toast.makeText(SettingsActivity.this, "Something went wrong!...", Toast.LENGTH_SHORT).show();
        editor.putBoolean("flagPurchased", false);
        editor.commit();
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onDestroy() {
        if (billingProcessor != null) {
            billingProcessor.release();
        }
        super.onDestroy();
    }
}