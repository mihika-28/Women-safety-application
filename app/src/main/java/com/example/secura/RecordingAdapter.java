package com.example.secura;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecordingAdapter extends BaseAdapter {

    public interface Callback {
        void onAction(int position, String action);
    }

    Context ctx;
    ArrayList<HashMap<String,String>> list;
    Callback cb;

    public RecordingAdapter(Context c, ArrayList<HashMap<String,String>> l, Callback cb) {
        this.ctx = c; this.list = l; this.cb = cb;
    }

    @Override public int getCount() { return list.size(); }
    @Override public Object getItem(int i) { return list.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(ctx).inflate(R.layout.recording_item, parent, false);

        TextView title = convertView.findViewById(R.id.recordingTitle);
        TextView sub = convertView.findViewById(R.id.recordingSub);
        ImageButton play = convertView.findViewById(R.id.playButton);
        ImageButton del = convertView.findViewById(R.id.deleteButton);

        HashMap<String,String> item = list.get(pos);
        title.setText(item.get("filename"));
        sub.setText(item.get("timestamp"));

        play.setOnClickListener(v -> { if (cb != null) cb.onAction(pos, "play"); });
        del.setOnClickListener(v -> { if (cb != null) cb.onAction(pos, "delete"); });

        return convertView;
    }
}
