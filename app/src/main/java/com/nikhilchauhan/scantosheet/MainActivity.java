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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ConnectivityManager connectivityManager;
    FloatingActionButton id_fab;
    ListView id_listView;
    LinearLayout id_layoutClear;
    RelativeLayout id_layoutInfo;
    ProgressDialog dialog;

    String[] scannedArray;
    ArrayList arrayList;
    ArrayAdapter arrayAdapter;
    int sizeOfArrayList;
    int position = 0;

    String itemDesc;
    String itemNote;

    Boolean flagFab = true;
    boolean flagDialog;
    Dialog modalScan;
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


        id_fab = findViewById(R.id.id_fab);
        id_txtClearAll = findViewById(R.id.id_txtClearAll);
        id_txtInfo = findViewById(R.id.id_txtInfo);
        id_listView = findViewById(R.id.id_listView);
        id_layoutClear = findViewById(R.id.id_layoutClear);
        id_layoutInfo = findViewById(R.id.id_layoutInfo);
        modalScan = new Dialog(MainActivity.this);

        if(pref.getBoolean("flagForScan", false)){
            Bundle b = this.getIntent().getExtras();
            editor.putBoolean("flagForScan", false);
            editor.commit(); // commit changes
            scannedArray=b.getStringArray("scannedArray");
            arrayList = new ArrayList(Arrays.asList(scannedArray));
            id_layoutInfo.setVisibility(GONE);
            id_layoutClear.setVisibility(VISIBLE);
            id_listView.setVisibility(VISIBLE);
            id_fab.setImageResource(R.drawable.ic_send_white_24dp);
            flagFab=false;
            arrayAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,arrayList);
            id_txtClearAll.setText("Remove All("+arrayList.size()+")");
            id_listView.setAdapter(arrayAdapter);
        }else {
            if(pref.getString("urlOfSheet", null)!=null){
                id_txtInfo.setText("Congratulations! you're all set to start scanning...\nJust press Floating Action Button below to get Started.");
                id_fab.setImageResource(R.drawable.ic_scan_white_24dp);
                flagFab = true;
            }
        }

        //On ScannedList Item Tapped
        id_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,final int i, long l) {
                final String tempItem = arrayList.get(i).toString();
                arrayList.remove(i);
                id_txtClearAll.setText("Remove All("+arrayList.size()+")");
                arrayAdapter.notifyDataSetChanged();

                // Undo logic
                Snackbar.make(view, "Removed successfully...", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(arrayList.isEmpty()){
                            id_layoutInfo.setVisibility(GONE);
                            id_listView.setVisibility(VISIBLE);
                            id_layoutClear.setVisibility(VISIBLE);
                            id_fab.setImageResource(R.drawable.ic_send_white_24dp);
                            flagFab = false;
                        }
                        arrayList.add(i,tempItem);
                        id_txtClearAll.setText("Remove All("+arrayList.size()+")");
                        arrayAdapter.notifyDataSetChanged();
                    }
                }).show();

                // If All items are removed
                if(arrayList.isEmpty()){
                    id_txtInfo.setText("Hey! you're all set to start scanning...\nJust press Floating Action Button below to get Started.");
                    id_layoutClear.setVisibility(GONE);
                    id_listView.setVisibility(GONE);
                    id_layoutInfo.setVisibility(VISIBLE);
                    id_fab.setImageResource(R.drawable.ic_scan_white_24dp);
                    flagFab = true;
                }
            }
        });

        //Checking for Internet Connection
        View viewButton = findViewById(R.id.id_fab);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            Snackbar.make(viewButton,"No Internet Connection!...", Snackbar.LENGTH_LONG).show();
        }
    }

    // Help text tapped
    public void helpTapped(View view){
            if((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=X9B7uaxQnNg"));
                startActivity(browserIntent);
            }else{
                Snackbar.make(view,"No Internet Connection!...️", Snackbar.LENGTH_LONG).show();
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
                        arrayList.clear();
                        arrayAdapter.notifyDataSetChanged();
                        View view = findViewById(R.id.id_fab);
                        id_txtInfo.setText("Hey! you're all set to start scanning...\nJust press Floating Action Button below to get Started.");
                        id_layoutClear.setVisibility(GONE);
                        id_listView.setVisibility(GONE);
                        id_layoutInfo.setVisibility(VISIBLE);
                        id_fab.setImageResource(R.drawable.ic_scan_white_24dp);
                        flagFab = true;
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


    // Floating Action Button is Tapped
    public void fabTapped(View view){
        if(flagFab) {
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
        }else {
            if ((connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Sending To Google SpreadSheet, Please wait...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                addItemToSheet();
            } else {
                Snackbar.make(view, "No Internet Connection!...️", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    //Fab Download tapped
    public void fabDownloadTapped(View view){
        if(flagFab){
            Snackbar.make(view,"No data for Offline download!...️", Snackbar.LENGTH_LONG).show();
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
        sizeOfArrayList = arrayList.size();
        for (int i = 0; i < sizeOfArrayList; i++) {
            View view = findViewById(R.id.id_fab);
            String fileName = "STS" + ".txt";//like 2016_01_12.txt
            try {
                File root = new File(Environment.getExternalStorageDirectory() + File.separator + "ScanToSheet");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, fileName);
                FileWriter writer = new FileWriter(gpxfile, true);
                writer.append(arrayList.get(i)+"-"+pref.getString("descOfItem","NULL")+"-"+pref.getString("noteOfItem", "NULL")+"\n\n");
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
            intentScan.putExtra("boolScanContinuous", id_continuousScan.isChecked() ? true : false);
            modalScan.dismiss();
            startActivity(intentScan);
            finish();
        }
    }

    // HTTP Rest API Calls
    private void addItemToSheet() {
        flagDialog=true;
        sizeOfArrayList = arrayList.size();
        Handler handler = new Handler();
        for (int i=0;i<sizeOfArrayList;i++){
            position = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final String textUrl = pref.getString("urlOfSheet", null);
                    final String textSheet = pref.getString("nameOfSheet", null);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST,
                            "https://script.google.com/macros/s/AKfycbwD30p359_LRXGLZKlaJU5lry78qFrZbyF50uldO-JRqv-Eqkg/exec",
                            new com.android.volley.Response.Listener<String>() {
                                @Override
                                public void onResponse(final String response) {
                                    final View view = findViewById(R.id.id_fab);
                                    if(sizeOfArrayList == 1){
                                        dialog.dismiss();
                                        Snackbar.make(view,sizeOfArrayList+" item added to spreadseet successfully...️", Snackbar.LENGTH_LONG).show();
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
                                                        Snackbar.make(view,sizeOfArrayList+" items added to spreadseet successfully...️", Snackbar.LENGTH_LONG).show();
                                                    }
                                                }, 1500*sizeOfArrayList);
                                            }
                                        }
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,"Error! sending data failed...",Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> parmas = new HashMap<>();
                            // Passing parameters
                            parmas.put("TextUrl", textUrl);
                            parmas.put("TextSheet", textSheet);
                            parmas.put("Item_name", arrayList.get(position).toString());
                            parmas.put("Item_desc", pref.getString("descOfItem",""));
                            parmas.put("Item_note", pref.getString("noteOfItem", ""));
                            return parmas;
                        }
                    };
                    int socketTimeOut = 30000;
                    RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(retryPolicy);
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    queue.add(stringRequest);
                }
            }, 1500*(i+1));
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
            Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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


}
