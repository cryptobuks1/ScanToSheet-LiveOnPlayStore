package com.nikhilchauhan.scantosheet;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Result;
import java.util.ArrayList;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView scannerView;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ArrayList<String> scannedList;
    ArrayList<String> listDescOfItem;
    ArrayList<String> listNoteOfItem;

    String itemName;
    int scannedTotal = 0;

    boolean flagFlash=false;
//    MediaPlayer beepSound;
    Boolean boolScanContinuous;
    Boolean boolVibrateOnScan;
//    Boolean boolSoundOnScan;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(ScanActivity.this);
        scannerView.setAutoFocus(true);
        setContentView(scannerView);

        pref = getApplicationContext().getSharedPreferences("mySharedPref", 0); // 0 - for private mode
        editor = pref.edit();

        scannedList = new ArrayList<>();
        listDescOfItem = new ArrayList<>();
        listNoteOfItem = new ArrayList<>();
        scannedList = (ArrayList) getIntent().getSerializableExtra("scannedList");
        listDescOfItem = (ArrayList) getIntent().getSerializableExtra("listDescOfItem");
        listNoteOfItem = (ArrayList) getIntent().getSerializableExtra("listNoteOfItem");


        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        beepSound = MediaPlayer.create(ScanActivity.this,R.raw.scan_beep);

        boolVibrateOnScan = pref.getBoolean("vibrateOnScan", true);
//        boolSoundOnScan = pref.getBoolean("soundOnScan", false);
        boolScanContinuous = getIntent().getExtras().getBoolean("boolScanContinuous",false);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setTitle("Done");
    }


    @Override
    public void handleResult(final Result result) {

        if(boolVibrateOnScan){
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(300);
            }
        }

        scannedTotal = scannedTotal+1;
        Toast.makeText(ScanActivity.this, scannedTotal+" Item Scanned", Toast.LENGTH_SHORT).show();

        itemName = result.getText();
        scannedList.add(itemName);

        listDescOfItem.add(pref.getString("descOfItem",""));
        listNoteOfItem.add(pref.getString("noteOfItem", ""));

        // Play sound
//        if(boolSoundOnScan){
//            beepSound.start();
//        }

        // If Continuous Scan
        if(boolScanContinuous){
            scannerView.resumeCameraPreview(ScanActivity.this);
        }else {
            editor.putBoolean("flagForScan", true);
            editor.commit();
            Intent intentMain = new Intent(ScanActivity.this, MainActivity.class);
            intentMain.putExtra("scannedList", scannedList);
            intentMain.putExtra("listDescOfItem", listDescOfItem);
            intentMain.putExtra("listNoteOfItem", listNoteOfItem);
            startActivity(intentMain);
            finish();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }


    @Override
    protected void onResume() {
        super.onResume();
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(scannedList.isEmpty()){
            Intent intentMain=new Intent(ScanActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }else {
            editor.putBoolean("flagForScan", true);
            editor.commit();
            Intent intentMain = new Intent(ScanActivity.this, MainActivity.class);
            intentMain.putExtra("scannedList", scannedList);
            intentMain.putExtra("listDescOfItem", listDescOfItem);
            intentMain.putExtra("listNoteOfItem", listNoteOfItem);
            startActivity(intentMain);
            finish();
        }
    }

    // Done pressed
    @Override
    public boolean onSupportNavigateUp() {
        if(scannedList.isEmpty()){
            Intent intentMain=new Intent(ScanActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }else {
            editor.putBoolean("flagForScan", true);
            editor.commit();
            Intent intentMain = new Intent(ScanActivity.this, MainActivity.class);
            intentMain.putExtra("scannedList", scannedList);
            intentMain.putExtra("listDescOfItem", listDescOfItem);
            intentMain.putExtra("listNoteOfItem", listNoteOfItem);
            startActivity(intentMain);
            finish();
        }
        return true;
    }

    //Settings Flash switch STARTS here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_flash,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.id_flashSwitch) {
            if(flagFlash){
                item.setIcon(R.drawable.ic_flash_off_white_24dp);
                scannerView.setFlash(false);
                flagFlash = false;
            }else{
                item.setIcon(R.drawable.ic_flash_on_white_24dp);
                scannerView.setFlash(true);
                flagFlash = true;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    } //Settings flash switch ENDS here

}