package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RecordingsActivity extends AppCompatActivity {

    ListView listView;
    DBHelper dbHelper;
    ArrayList<HashMap<String, String>> recordings;
    RecordingAdapter adapter;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings);

        listView = findViewById(R.id.recordingsListView); // matches layout
        dbHelper = new DBHelper(this);

        loadList();
    }

    private void loadList() {
        recordings = dbHelper.getAllRecordings();
        adapter = new RecordingAdapter(this, recordings, (position, action) -> {
            HashMap<String, String> item = recordings.get(position);
            String filePath = item.get("filepath");

            if ("play".equals(action)) {
                playRecording(filePath);
            } else if ("delete".equals(action)) {
                deleteRecording(filePath, position);
            }
        });
        listView.setAdapter(adapter);
    }

    private void playRecording(String filePath) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Playing recording...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error playing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteRecording(String filePath, int position) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        dbHelper.getWritableDatabase().delete("recordings", "filepath=?", new String[]{filePath});
        recordings.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Recording deleted.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
