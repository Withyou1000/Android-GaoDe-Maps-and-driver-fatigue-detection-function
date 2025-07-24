package com.example.myapplication.Method;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.myapplication.Class.MyMarker;
import java.util.ArrayList;
import java.util.List;

public class MapMarkDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "marker.db";
    private static final int VERSION = 1;
    private static final String MARKER_INFO = "marker_info";
    private static MapMarkDBHelper Helper;
    private static SQLiteDatabase Write;
    private static SQLiteDatabase Read;

    MapMarkDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static MapMarkDBHelper getInstance(Context context) {
        if (Helper == null) {
            Helper = new MapMarkDBHelper(context);
        }
        return Helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + MARKER_INFO +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " name VARCHAR NOT NULL," +
                " description VARCHAR NOT NULL," +
                " lat DOUBLE NOT NULL," +
                " lon DOUBLE NOT NULL," +
                " pic_path VARCHAR NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase openWrite() {
        if (Write == null || !Write.isOpen()) {
            Write = Helper.getWritableDatabase();
        }
        return Write;
    }

    public SQLiteDatabase openRead() {
        if (Read == null || !Read.isOpen()) {
            Read = Helper.getReadableDatabase();
        }
        return Read;
    }

    public void closelink() {
        if (Read != null && Read.isOpen()) {
            Read.close();
            Read = null;//利于系统回收
        }
        if (Write != null && Write.isOpen()) {
            Write.close();
            Write = null;//利于系统回收
        }
    }

    public List<MyMarker> queryall() {
        String sql = "SELECT * FROM " + MARKER_INFO;
        List<MyMarker> list = new ArrayList<>();
        Cursor cursor = Read.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            MyMarker marker = new MyMarker();
            marker.id = cursor.getInt(0);
            marker.name = cursor.getString(1);
            marker.description = cursor.getString(2);
            marker.lat = cursor.getDouble(3);
            marker.lon = cursor.getDouble(4);
            marker.pic_path = cursor.getString(5);
            list.add(marker);
        }
        return list;
    }

    public MyMarker query_marker_byName(String name) {
        MyMarker marker = new MyMarker();
        Cursor cursor = Read.query(MARKER_INFO, null, "name=?", new String[]{name}, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToNext()) {
            marker.id = cursor.getInt(0);
            marker.name = cursor.getString(1);
            marker.description = cursor.getString(2);
            marker.lat = cursor.getDouble(3);
            marker.lon = cursor.getDouble(4);
            marker.pic_path = cursor.getString(5);
        }
        cursor.close();
        return marker;
    }

    public long insert_marker(MyMarker marker) {
        ContentValues values = new ContentValues();
        values.put("name", marker.name);
        values.put("description", marker.description);
        values.put("pic_path", marker.pic_path);
        values.put("lat", marker.lat);
        values.put("lon", marker.lon);
        long result = Write.insert(MARKER_INFO, null, values);
        return result;
    }

    public void update_marker(MyMarker marker) {
        ContentValues values = new ContentValues();
        values.put("name", marker.name);
        values.put("description", marker.description);
        values.put("pic_path", marker.pic_path);
        values.put("lat", marker.lat);
        values.put("lon", marker.lon);
        Write.update(MARKER_INFO, values, "lat=? and lon=?", new String[]{String.valueOf(marker.lat),String.valueOf(marker.lon)});
    }
    public void delete_marker_byName(String name) {
        Write.delete(MARKER_INFO, "name=?", new String[]{name});
    }
}
