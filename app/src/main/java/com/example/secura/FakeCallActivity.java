package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FakeCallActivity extends AppCompatActivity {

    TextView fakeCallerName;
    Button acceptBtn, declineBtn;
    MediaPlayer ringtonePlayer;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        fakeCallerName = findViewById(R.id.callerName);
        acceptBtn = findViewById(R.id.acceptBtn);
        declineBtn = findViewById(R.id.declineBtn);
        db = new DBHelper(this);

        // Get trusted contact name or number
        ArrayList<HashMap<String, String>> contacts = db.getAllTrustedNumbers();
        String name = "Unknown Caller";
        if (!contacts.isEmpty()) {
            int randomIndex = (int) (Math.random() * contacts.size());
            name = contacts.get(randomIndex).get("name");
            if (name == null || name.trim().isEmpty()) {
                name = contacts.get(randomIndex).get("phone");
            }
        }

        fakeCallerName.setText(name);

        // Play system default ringtone
        playDefaultRingtone();

        // Decline button — stop ringtone immediately and finish
        declineBtn.setOnClickListener(v -> {
            stopRingtoneImmediately();
            Toast.makeText(this, "Call Declined", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Accept button — stop ringtone, save history, go to next activity
        String finalName = name;
        acceptBtn.setOnClickListener(v -> {
            stopRingtoneImmediately();
            saveFakeCall(finalName);
            Intent intent = new Intent(FakeCallActivity.this, CallConnectedActivity.class);
            intent.putExtra("callerName", finalName);
            startActivity(intent);
            finish();
        });
    }

    // 🎵 Play default ringtone (system sound)
    private void playDefaultRingtone() {
        try {
            stopRingtoneImmediately(); // stop if any is already playing
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtonePlayer = MediaPlayer.create(this, ringtoneUri);
            ringtonePlayer.setLooping(true);
            ringtonePlayer.start();
        } catch (Exception e) {
            Toast.makeText(this, "Error playing ringtone: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //  Stop ringtone immediately and release
    private void stopRingtoneImmediately() {
        try {
            if (ringtonePlayer != null) {
                if (ringtonePlayer.isPlaying()) ringtonePlayer.stop();
                ringtonePlayer.release();
                ringtonePlayer = null;
            }
        } catch (Exception ignored) {}
    }

    // 💾 Save fake call history
    private void saveFakeCall(String callerName) {
        try {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            db.saveFakeCall(callerName, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRingtoneImmediately();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtoneImmediately();
    }
}
