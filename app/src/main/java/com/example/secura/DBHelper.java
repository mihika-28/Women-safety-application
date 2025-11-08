package com.example.secura;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "secura.db";
    private static final int DATABASE_VERSION = 2; // incremented for unsafe areas

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Recordings Table
        db.execSQL("CREATE TABLE IF NOT EXISTS recordings(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "filename TEXT," +
                "filepath TEXT," +
                "timestamp TEXT)");

        // Fake Calls Table
        db.execSQL("CREATE TABLE IF NOT EXISTS fakecalls(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "time TEXT)");

        // Trusted Contacts Table
        db.execSQL("CREATE TABLE IF NOT EXISTS trusted_contacts(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "phone TEXT)");

        // New: Unsafe Areas Table
        db.execSQL("CREATE TABLE IF NOT EXISTS unsafe_areas(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "latitude REAL," +
                "longitude REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS unsafe_areas(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "latitude REAL," +
                    "longitude REAL)");
        }
    }

    // Recordings
    public long saveRecording(String filename, String filepath, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("filename", filename);
        cv.put("filepath", filepath);
        cv.put("timestamp", timestamp);
        long id = db.insert("recordings", null, cv);
        db.close();
        return id;
    }

    public ArrayList<HashMap<String, String>> getAllRecordings() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, filename, filepath, timestamp FROM recordings ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                HashMap<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(c.getInt(0)));
                m.put("filename", c.getString(1));
                m.put("filepath", c.getString(2));
                m.put("timestamp", c.getString(3));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public void deleteRecordingById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("recordings", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ✅ Fake Calls
    public void saveFakeCall(String name, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("time", time);
        db.insert("fakecalls", null, cv);
        db.close();
    }

    public ArrayList<HashMap<String, String>> getAllFakeCalls() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, time FROM fakecalls ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                HashMap<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(c.getInt(0)));
                m.put("name", c.getString(1));
                m.put("time", c.getString(2));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // ✅ Trusted Contacts
    public void clearTrustedContacts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("trusted_contacts", null, null);
        db.close();
    }

    public long saveTrustedContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        long id = db.insert("trusted_contacts", null, cv);
        db.close();
        return id;
    }

    public ArrayList<HashMap<String, String>> getAllTrustedNumbers() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, phone FROM trusted_contacts ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                HashMap<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(c.getInt(0)));
                m.put("name", c.getString(1));
                m.put("phone", c.getString(2));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // ✅ Unsafe Areas (Improved)
    public boolean isUnsafeAreaExists(double latitude, double longitude) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id FROM unsafe_areas WHERE ABS(latitude - ?) < 0.0001 AND ABS(longitude - ?) < 0.0001",
                new String[]{String.valueOf(latitude), String.valueOf(longitude)}
        );
        boolean exists = c.moveToFirst();
        c.close();
        db.close();
        return exists;
    }

    public void saveUnsafeArea(String name, double latitude, double longitude) {
        if (isUnsafeAreaExists(latitude, longitude)) return; // prevent duplicates
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);
        db.insert("unsafe_areas", null, cv);
        db.close();
    }

    public ArrayList<HashMap<String, String>> getAllUnsafeAreas() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor c = db.rawQuery("SELECT id, name, latitude, longitude FROM unsafe_areas ORDER BY id DESC", null)) {
            while (c.moveToNext()) {
                HashMap<String, String> m = new HashMap<>();
                m.put("id", String.valueOf(c.getInt(0)));
                m.put("name", c.getString(1));
                m.put("latitude", String.valueOf(c.getDouble(2)));
                m.put("longitude", String.valueOf(c.getDouble(3)));
                list.add(m);
            }
        }
        return list;
    }

    // ✅ Delete unsafe area using coordinates (for newly added markers)
    // ✅ Delete unsafe area by its ID
    // ✅ Delete unsafe area using ID
    public void deleteUnsafeArea(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("unsafe_areas", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ✅ Delete unsafe area by location (if user long-pressed wrong spot)
    public void deleteUnsafeAreaByLocation(double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("unsafe_areas",
                "ABS(latitude - ?) < 0.0001 AND ABS(longitude - ?) < 0.0001",
                new String[]{String.valueOf(latitude), String.valueOf(longitude)});
        db.close();
    }

    // ✅ Clear all unsafe areas (optional admin use)
    public void clearUnsafeAreas() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("unsafe_areas", null, null);
        db.close();
    }

}
