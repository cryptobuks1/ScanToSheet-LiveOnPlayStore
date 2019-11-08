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
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.material.snackbar.Snackbar;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ConnectivityManager connectivityManager;

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

        id_nameOfSheet = findViewById(R.id.id_nameOfSheet);
        id_urlOfSheet = findViewById(R.id.id_urlOfSheet);
//        id_soundOnScan = findViewById(R.id.id_soundOnScan);
        id_vibrateOnScan = findViewById(R.id.id_vibrateOnScan);


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


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intentMain = new Intent(SettingsActivity.this,MainActivity.class);
        startActivity(intentMain);
    }
}