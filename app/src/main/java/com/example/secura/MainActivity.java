package com.example.secura;

import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import android.content.SharedPreferences;

public class
MainActivity extends AppCompatActivity {

    Button sosButton;
    DrawerLayout drawerLayout;
    ImageView menuIcon;
    LinearLayout sidebarLayout;
    MediaRecorder recorder;
    File audioFile;
    DBHelper dbHelper;
    LocationManager locationManager;
    private static final int REQUEST_PERMISSIONS = 101;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sosButton = findViewById(R.id.sosButton);
        drawerLayout = findViewById(R.id.drawerLayout);
        menuIcon = findViewById(R.id.menuIcon);
        sidebarLayout = findViewById(R.id.sidebarLayout);

        dbHelper = new DBHelper(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        requestPermissions();

        // ✅ SOS toggle
        sosButton.setOnClickListener(v -> {
            sosButton.setEnabled(false);
            if (!isRecording) startSOS();
            else stopRecording();
            sosButton.postDelayed(() -> sosButton.setEnabled(true), 2000);
        });

        // ✅ Sidebar menu
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        findViewById(R.id.fakeCallButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FakeCallActivity.class)));
        findViewById(R.id.recordingsButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecordingsActivity.class)));
        findViewById(R.id.helplineButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HelplineActivity.class)));
        findViewById(R.id.callHistoryButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CallHistoryActivity.class)));

        findViewById(R.id.navTrustedContacts).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TrustedContactsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        findViewById(R.id.navRecordings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecordingsActivity.class));
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        findViewById(R.id.navHelplines).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HelplineActivity.class));
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        findViewById(R.id.navFakeCall).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FakeCallActivity.class));
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        findViewById(R.id.navCallHistory).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CallHistoryActivity.class));
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        // ✅ Logout
        findViewById(R.id.navLogout).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("SecuraPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Toast.makeText(MainActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            drawerLayout.closeDrawer(GravityCompat.END);
        });
    }

    // ✅ Start SOS
    private void startSOS() {
        try {
            // 1) Start recording
            try {
                startRecording();
            } catch (Exception e) {
                Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            // 2) Get location asynchronously and send SMS
            FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_PERMISSIONS);
                Toast.makeText(this, "Location permission required for SOS", Toast.LENGTH_LONG).show();
                return;
            }

            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                String alertMessage;
                if (location != null) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    alertMessage = "🚨 Help! I need assistance urgently.\nMy location: https://maps.google.com/?q=" + lat + "," + lon;
                } else {
                    alertMessage = "🚨 Help! I need assistance urgently.\nLocation unavailable. Please contact me!";
                }

                final String message = alertMessage;
                new Thread(() -> sendAlertSMS(message)).start();

                runOnUiThread(() -> {
                    sosButton.setText("STOP");
                    sosButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
                    isRecording = true;
                    Toast.makeText(MainActivity.this, "SOS activated — alert sent.", Toast.LENGTH_SHORT).show();
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "SOS crashed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ Send alert SMS
    private void sendAlertSMS(String message) {
        try {
            SmsManager smsManager = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                    ? this.getSystemService(SmsManager.class)
                    : SmsManager.getDefault();

            ArrayList<String> parts = smsManager.divideMessage(message);
            ArrayList<HashMap<String, String>> contacts = dbHelper.getAllTrustedNumbers();

            if (contacts.isEmpty()) {
                runOnUiThread(() ->
                        Toast.makeText(this, "No trusted contacts saved!", Toast.LENGTH_SHORT).show());
                return;
            }

            for (HashMap<String, String> contact : contacts) {
                String phone = contact.get("phone");
                if (phone != null && !phone.isEmpty()) {
                    smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
                }
            }

            runOnUiThread(() ->
                    Toast.makeText(this, "🚨 SOS message sent successfully!", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    // ✅ Start recording
    private void startRecording() throws IOException {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "SecuraRecordings");
        if (!dir.exists()) dir.mkdirs();

        String fileName = "REC_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".mp3";
        audioFile = new File(dir, fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        recorder.prepare();
        recorder.start();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        dbHelper.saveRecording(fileName, audioFile.getAbsolutePath(), timestamp);
    }

    private void stopRecording() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
                Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error stopping recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sosButton.setText("SOS");
        sosButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        isRecording = false;
    }

    // ✅ Permission Request
    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            try {
                recorder.stop();
                recorder.release();
            } catch (Exception ignored) {}
            recorder = null;
        }
    }
}
