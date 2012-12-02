package com.badger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.badger.attendance.AttendanceListActivity;

public class MainActivity extends Activity {

  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat();
  private LinearLayout linearLayout;

  private NfcAdapter nfcAdapter;
  private PendingIntent pendingIntent;
  
  private String currentName;
  private Tag currentTag;
  
  private SharedPreferences usersMap;
  private SharedPreferences attendanceMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    linearLayout = (LinearLayout) findViewById(R.id.list);
    handleIntent(getIntent());
    
    usersMap = getSharedPreferences(Constants.USERS_PREFS_NAME, 0);
    attendanceMap = getSharedPreferences(Constants.ATTENDANCE_PREFS_NAME, 0);

    nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    if (nfcAdapter == null) {
      showAlertMessage(R.string.error, R.string.no_nfc);
    }

    pendingIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (nfcAdapter != null) {
      if (!nfcAdapter.isEnabled()) {
        showAlertMessage(R.string.error, R.string.nfc_disabled);
      }
      nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (nfcAdapter != null) {
      nfcAdapter.disableForegroundDispatch(this);
    }
  }
  
  private void showAlertMessage(int title, int message) {
    AlertDialog alertDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();
    alertDialog.setTitle(title);
    alertDialog.setMessage(getText(message));
    alertDialog.show();
  }
  
  @Override
  public void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent(intent);
  }
  
  private void handleIntent(Intent intent) {
    String action = intent.getAction();
    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
        || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
      currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
      if (!usersMap.contains(Util.getTagAsHex(currentTag))) {
        processNewNfcTag();
      } else {
        processKnownNfcTag();
      }
    }
  }

  /**
   * Processes a tag that has been previously recorded.
   */
  private void processKnownNfcTag() {
    String name = usersMap.getString(Util.getTagAsHex(currentTag), "");

    // Mark attendance
    Editor editor = attendanceMap.edit();
    editor.putBoolean(name, true);
    editor.commit();

    // Time
    TextView timeView = new TextView(this);
    timeView.setText("Check-in time: " + TIME_FORMAT.format(new Date()));
    linearLayout.addView(timeView, 0);

    // Name
    TextView nameTextView = new TextView(this);
    nameTextView.setText("Name: " + name);
    linearLayout.addView(nameTextView, 1);

    // Image
    try {
      ImageView image = new ImageView(this);
      File imageFile = AlbumUtil.getImageFile(name);
      Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

      LinearLayout.LayoutParams vp = 
        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
            LayoutParams.MATCH_PARENT);
      image.setLayoutParams(vp);
      image.setImageBitmap(bitmap);
      linearLayout.addView(image, 2);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Divider
    LayoutInflater inflater = LayoutInflater.from(this);
    linearLayout.addView(inflater.inflate(R.layout.main_divider, linearLayout, false), 3);
  }

  /**
   * Processes an NFC that has never been seen before.
   */
  private void processNewNfcTag() {
    TextView textView = new TextView(this);
    String text = String.format("Registering new user (%s)", Util.getTagAsHex(currentTag));
    textView.setText(text);
    linearLayout.addView(textView, 0);
    final EditText input = new EditText(this);
    new AlertDialog.Builder(MainActivity.this)
      .setTitle("New user")
      .setMessage("Please enter your name")
      .setView(input)
      .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog,
            int whichButton) {
          try {
            currentName = input.getText().toString();
            
            // Take picture
            File imageFile = AlbumUtil.getImageFile(currentName);
            Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(imageCaptureIntent, Constants.CAMERA_INTENT_REQUEST_CODE);
            
            // Save data
            SharedPreferences.Editor editor = usersMap.edit();
            editor.putString(Util.getTagAsHex(currentTag), currentName);
            editor.commit();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      })
      .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog,
            int whichButton) {
          // Do nothing.
        }
      }).show();
  }


  /** 
   * Handles callback from camera intent.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == Constants.CAMERA_INTENT_REQUEST_CODE && resultCode == RESULT_OK) {
      // There's a bug in Google Experiences devices, where you can't get
      // URI data from the INtent. Therefore, we use a private variables
      // for bookkeeeping. More details at:
      // http://stackoverflow.com/q/5059731/100633
      try {
        File imageFile = AlbumUtil.getImageFile(currentName);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        linearLayout.addView(imageView, 0);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  } 
  
  // Menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.list_attendance:
        Intent intent = new Intent(this, AttendanceListActivity.class);
        startActivity(intent);
        break;
      case R.id.clear_attendance:
        clearAttendance();
        break;
      case R.id.reset:
        resetData();
        break;        
      case R.id.about:
        Toast.makeText(this, "Created by Alec Go.", Toast.LENGTH_LONG).show();
        break;
    }
    return true;
  }
  
  private void clearAttendance() {
    Editor editor = attendanceMap.edit();
    editor.clear();
    editor.commit();
    Toast.makeText(this, "Cleared attendance!", Toast.LENGTH_LONG).show();
  }
  
  public void resetData() {
    Editor editor = usersMap.edit();
    editor.clear();
    editor.commit();
    clearAttendance();
  }
}
