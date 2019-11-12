package com.nikhilchauhan.scantosheet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.github.yavski.fabspeeddial.FabSpeedDial;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ConnectivityManager connectivityManager;
    View view;
    ListView id_listView;
    LinearLayout id_layoutClear;
    RelativeLayout id_layoutInfo;
    ProgressDialog dialog;

    ArrayList<String> scannedList;
    ArrayList<String> listDescOfItem;
    ArrayList<String> listNoteOfItem;
    ArrayAdapter arrayAdapter;
    int sizeOfArrayList;
    boolean flagDialog;
    boolean flagPurchased;
    boolean flagAlertDialog;
    String appScriptURL;
    String itemDesc;
    String itemNote;

    Vibrator vibrator;
    BillingProcessor billingProcessor;
    InterstitialAd mAdView;

    Dialog modalScan;
    FabSpeedDial fabSpeedDial;
    Switch id_continuousScan;
    TextView id_txtClearAll;
    TextView id_txtInfo;
    EditText id_txtDesc;
    EditText id_txtNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        pref = getApplicationContext().getSharedPreferences("mySharedPref", 0); // 0 - for private mode
        editor = pref.edit();

        scannedList = new ArrayList<>();
        listDescOfItem = new ArrayList<>();
        listNoteOfItem = new ArrayList<>();

        flagPurchased =pref.getBoolean("flagPurchased", false);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        billingProcessor = BillingProcessor.newBillingProcessor(MainActivity.this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgweHIkf4bnV254X8bipY9Z5ZztQACMZdm/mZDlU+KCLwwM0MCaq+lbdpBleW1NzKAAjDl3LNbe1dKwaM+5/ESRit9BnlqXxGs+7FEaFLMkz95uJHS0QN5ffDuuMEMUpYzfj9Ir2uJDp+OFwg7euKb7U2biY+k0/oBlfRK7eVGEGB/Ju9JiNUerCayyFGAXz9/Q53/oPuBetAqRWIzPX1C8VjWOUknFR4TrJi0IrNz5hzBtHgdj4hQe2FYkFSpLS/MSsX3vCN3cvqjELxgqflysbWy79c/+jxxeD9d2EMH4eAnvo56k/x4dNQRR56XLVN3j6zrvnd0rYg84VcjLEjFQIDAQAB", MainActivity.this);
        billingProcessor.initialize(); // binds

        view = findViewById(R.id.id_fab);
        fabSpeedDial = findViewById(R.id.id_fab);
        id_txtClearAll = findViewById(R.id.id_txtClearAll);
        id_txtInfo = findViewById(R.id.id_txtInfo);
        id_listView = findViewById(R.id.id_listView);
        id_layoutClear = findViewById(R.id.id_layoutClear);
        id_layoutInfo = findViewById(R.id.id_layoutInfo);
        modalScan = new Dialog(MainActivity.this);


        if(pref.getBoolean("flagForScan", false)){
            editor.putBoolean("flagForScan", false);
            editor.commit(); // commit changes
            scannedList = (ArrayList) getIntent().getSerializableExtra("scannedList");
            listDescOfItem = (ArrayList) getIntent().getSerializableExtra("listDescOfItem");
            listNoteOfItem = (ArrayList) getIntent().getSerializableExtra("listNoteOfItem");
            id_layoutInfo.setVisibility(GONE);
            id_layoutClear.setVisibility(VISIBLE);
            id_listView.setVisibility(VISIBLE);
            arrayAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,scannedList);
            id_txtClearAll.setText("Remove All("+scannedList.size()+")");
            id_listView.setAdapter(arrayAdapter);
        }else {
            if(pref.getString("urlOfSheet", null)!=null){
                id_txtInfo.setText("Congratulations! you're all set to start scanning...\nJust press Floating Action Button below to get Started.");
            }
        }

        //On ScannedList Item Tapped
        id_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,final int i, long l) {
                final String tempScannedItem = scannedList.get(i);
                final String tempDescItem = listDescOfItem.get(i);
                final String tempNoteItem = listNoteOfItem.get(i);
                scannedList.remove(i);
                listDescOfItem.remove(i);
                listNoteOfItem.remove(i);
                id_txtClearAll.setText("Remove All("+scannedList.size()+")");
                arrayAdapter.notifyDataSetChanged();

                // Undo logic
                Snackbar.make(view, "Removed successfully...", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(scannedList.isEmpty()){
                            id_layoutInfo.setVisibility(GONE);
                            id_listView.setVisibility(VISIBLE);
                            id_layoutClear.setVisibility(VISIBLE);
                        }
                        scannedList.add(i,tempScannedItem);
                        listDescOfItem.add(i,tempDescItem);
                        listNoteOfItem.add(i,tempNoteItem);
                        id_txtClearAll.setText("Remove All("+scannedList.size()+")");
                        arrayAdapter.notifyDataSetChanged();
                    }
                }).show();

                // If All items are removed
                if(scannedList.isEmpty()){
                    id_txtInfo.setText("Hey! you're all set to start scanning...\nJust press Floating Action Button below to get Started.");
                    id_layoutClear.setVisibility(GONE);
                    id_listView.setVisibility(GONE);
                    id_layoutInfo.setVisibility(VISIBLE);
                }
            }
        });


        //MAIN FAB Tapped
        fabSpeedDial.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_scan) {
                    if(flagPurchased){
                        scanNow();
                    }else {
                        editor.putInt("scanCount", pref.getInt("scanCount", 0)+1); // Storing scanCount
                        editor.commit();
                        if(pref.getInt("scanCount", 1)>50){
                            Toast.makeText(MainActivity.this, "Trial period ended, please purchase!...", Toast.LENGTH_SHORT).show();
                            billingProcessor.purchase(MainActivity.this,"scantosheet_premium");
                        }else{
                            scanNow();
                        }
                    }
                }
                if (menuItem.getItemId() == R.id.action_download) {
                    offlineDownload();
                }
                if (menuItem.getItemId() == R.id.action_send) {
                    sendToSpreadsheet();
                }
                return true;
            }

            @Override
            public void onMenuClosed() {

            }
        });

        // Load Interstitial Ads
        if(!flagPurchased){
            mAdView = new InterstitialAd(MainActivity.this);
            mAdView.setAdUnitId(getString(R.string.inter_home));
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


        //Checking for Internet Connection
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            Snackbar.make(view,"No Internet Connection!...", Snackbar.LENGTH_LONG).show();
        }

    }


    // Clear All Button Tapped
    public void clearAllTapped(View view){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you really want to remove all items?");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        scannedList.clear();
                        arrayAdapter.notifyDataSetChanged();
                        View view = findViewById(R.id.id_fab);
                        id_txtInfo.setText("Hey! you're all set to start scanning...\nJust press Floating Action Button below to get Started.");
                        id_layoutClear.setVisibility(GONE);
                        id_listView.setVisibility(GONE);
                        id_layoutInfo.setVisibility(VISIBLE);
                        Snackbar.make(view, "All items cleared successfully...", Snackbar.LENGTH_LONG).show();                            }
                });
        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    //Fab ScanNow tapped
    public void scanNow(){
            if (pref.getString("nameOfSheet", "").isEmpty() || pref.getString("urlOfSheet", "").isEmpty()) {
                Snackbar.make(view, "No spreadsheet is added!...️", Snackbar.LENGTH_LONG).show();
            } else {
                //Checking CAMERA Uses Permissions
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(view, "No camera permission!...️", Snackbar.LENGTH_LONG).show();
                }else {
                    modalScan.setContentView(R.layout.modal_scan);
                    id_txtDesc = modalScan.findViewById(R.id.id_txtDesc);
                    id_txtNote = modalScan.findViewById(R.id.id_txtNote);
                    id_continuousScan = modalScan.findViewById(R.id.id_continuousScan);
                    modalScan.setCanceledOnTouchOutside(false);
                    modalScan.show();
                }
            }
        }


    //Fab SendToSpreadsheet tapped
    public void sendToSpreadsheet(){
        if(scannedList.isEmpty()){
            Snackbar.make(view,"No data to send, please scan first!...️", Snackbar.LENGTH_LONG).show();
        }else {
            if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Sending To Google SpreadSheet, Please wait...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                callSpreadsheetAPI();
            } else {
                Snackbar.make(view, "No Internet Connection!, Try Offline Download...️", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    //Fab Download tapped
    public void offlineDownload(){
        if(scannedList.isEmpty()){
            Snackbar.make(view,"No data to download, please scan first!...️", Snackbar.LENGTH_LONG).show();
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    saveToSD();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            else {
                saveToSD();
            }
        }

    }

    // Save data to SD Card
    public void saveToSD() {
        sizeOfArrayList = scannedList.size();
        for (int i = 0; i < sizeOfArrayList; i++) {
            String fileName = "STS" + ".txt";//like 2016_01_12.txt
            try {
                File root = new File(Environment.getExternalStorageDirectory() + File.separator + "ScanToSheet");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, fileName);
                FileWriter writer = new FileWriter(gpxfile, true);
                writer.append("--------------------------------------------------------\n"
                        +scannedList.get(i)+"-"+listDescOfItem.get(i)
                        +"\n"+listNoteOfItem.get(i)+"\n--------------------------------------------------------\n\n\n");
                writer.flush();
                writer.close();
                if(sizeOfArrayList==i+1){
                    Snackbar.make(view, sizeOfArrayList+" item saved to './ScanToSheet/STS.txt'...️", Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    //Submit modal button Tapped
    public void modalSubmit(View view){
        itemDesc = id_txtDesc.getText().toString().trim();
        itemNote = id_txtNote.getText().toString().trim();

        if(itemDesc.isEmpty()){
            id_txtDesc.requestFocus();
            id_txtDesc.setError("Please enter something!");
        }else {
                editor.putString("descOfItem", itemDesc); // Storing Description
                editor.putString("noteOfItem", itemNote); // Storing Note
                editor.commit(); // commit changes
                Intent intentScan = new Intent(MainActivity.this, ScanActivity.class);
                intentScan.putExtra("scannedList", scannedList);
                intentScan.putExtra("listDescOfItem", listDescOfItem);
                intentScan.putExtra("listNoteOfItem", listNoteOfItem);
                intentScan.putExtra("boolScanContinuous", id_continuousScan.isChecked() ? true : false);
                modalScan.dismiss();
                startActivity(intentScan);
                finish();
        }
    }

    // HTTP Rest API Calls
    private void callSpreadsheetAPI() {
        int i;
        flagDialog=true;
        flagAlertDialog =true;
        sizeOfArrayList = scannedList.size();
        final String textUrl = pref.getString("urlOfSheet", null);
        final String textSheet = pref.getString("nameOfSheet", null);
        // Random googleAppScript assign logic
        Random r = new Random();
        int ran = r.nextInt(7 - 1) + 1;
        switch (ran) {
            case 2:
                appScriptURL = "https://script.google.com/macros/s/AKfycbzBj66goAeosyG3sXjNbnrImr2XhaDrQhDfHNa37LYaVGWWULo/exec";
                break;
            case 3:
                appScriptURL = "https://script.google.com/macros/s/AKfycbzMG4cf1XYPcYdvvaCcCFCxR0z9TwJw7OuYGqiJpIjRzyTTd7s/exec";
                break;
            case 4:
                appScriptURL = "https://script.google.com/macros/s/AKfycbx3G9vwammfyMtkAamfM1lDIB_OJDg4Q17rX9XOMbTNT2Zp6g/exec";
                break;
            case 5:
                appScriptURL = "https://script.google.com/macros/s/AKfycbwTuGZAG8VPoxdOpRsw4Eo6Ak4kz9KHP_hs9-c-0NKbHo3rfQ/exec";
                break;
            case 6:
                appScriptURL = "https://script.google.com/macros/s/AKfycbyRevrHW9TyG0S20FkOnkNDbE1HW0hsGJpxOSyx_w0NJCFkGg/exec";
                break;
        }
        Handler handler = new Handler();
        for (i=0;i<sizeOfArrayList;i++){
            final int j=i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final int position = j;
                    StringRequest stringRequest = new StringRequest(Request.Method.POST,
                            appScriptURL,
                            new com.android.volley.Response.Listener<String>() {
                                @Override
                                public void onResponse(final String response) {
                                    if(sizeOfArrayList == 1){
                                        dialog.dismiss();
                                        if (response.matches("Added Successfully")) {
                                            Snackbar.make(view, sizeOfArrayList + " item added to spreadsheet successfully...️", Snackbar.LENGTH_LONG).setAction("Show", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pref.getString("urlOfSheet", null)));
                                                    startActivity(browserIntent);
                                                }
                                            }).show();
                                            // Vibration
                                            if (Build.VERSION.SDK_INT >= 26) {
                                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                            } else {
                                                vibrator.vibrate(500);
                                            }
                                        }else {
                                            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("Error! Failed to send data...")
                                                    .setMessage("Please make sure that spreadsheet URL is valid & publicly editable.")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //Whatever
                                                        }
                                                    }).show();
                                        }
                                        flagDialog = true;
                                    }else{
                                        if (sizeOfArrayList == position+1) {
                                            if(flagDialog) {
                                                flagDialog = false;
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dialog.dismiss();
                                                        if (response.matches("Added Successfully")) {
                                                            Snackbar.make(view, sizeOfArrayList + " items added to spreadsheet successfully...️", Snackbar.LENGTH_LONG).setAction("Show", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pref.getString("urlOfSheet", null)));
                                                                    startActivity(browserIntent);
                                                                }
                                                            }).show();
                                                            // Vibration
                                                            if (Build.VERSION.SDK_INT >= 26) {
                                                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                                                            } else {
                                                                vibrator.vibrate(500);
                                                            }
                                                        }else{
                                                            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                                                                    .setTitle("Error! Failed to send data...")
                                                                    .setMessage("Please make sure that spreadsheet URL is valid & publicly editable.")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            //Whatever
                                                                        }
                                                                    }).show();
                                                        }
                                                    }
                                                }, 1000*sizeOfArrayList);
                                            }
                                        }
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            if(flagAlertDialog) {
                                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Error! Failed to send data...")
                                        .setMessage("Please make sure that spreadsheet URL is valid & publicly editable.")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Whatever
                                            }
                                        }).show();
                                flagAlertDialog = false;
                            }
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> parmas = new HashMap<>();
                            // Passing parameters
                            parmas.put("TextUrl", textUrl);
                            parmas.put("TextSheet", textSheet);
                            parmas.put("Item_name", scannedList.get(position));
                            parmas.put("Item_desc",listDescOfItem.get(position));
                            parmas.put("Item_note", listNoteOfItem.get(position));
                            return parmas;
                        }
                    };
                    int socketTimeOut = 30000;
                    RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(retryPolicy);
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    queue.add(stringRequest);
                }
            }, 1000*(i+1));
        }
    }


    // Help text tapped
    public void helpTapped(View view){
        if((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/74wt6XTpzgk"));
            startActivity(browserIntent);
        }else{
            Snackbar.make(view,"No Internet Connection!...️", Snackbar.LENGTH_LONG).show();
        }
    }


    //Settings menu STARTS here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.id_settingsMenu) {
                Intent intentSettings=new Intent(MainActivity.this,SettingsActivity.class);
                intentSettings.putExtra("scannedList", scannedList);
                intentSettings.putExtra("listDescOfItem", listDescOfItem);
                intentSettings.putExtra("listNoteOfItem", listNoteOfItem);
                startActivity(intentSettings);
                finish();
        }
        return true;
    } //Settings menu ENDS here


    public void modalClose(View view){
        modalScan.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Asking CAMERA Uses Permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onBillingInitialized() {
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        editor.putBoolean("flagPurchased", true);
        editor.commit();
        flagPurchased =pref.getBoolean("flagPurchased", false);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Toast.makeText(MainActivity.this, "Something went wrong!...", Toast.LENGTH_SHORT).show();
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
