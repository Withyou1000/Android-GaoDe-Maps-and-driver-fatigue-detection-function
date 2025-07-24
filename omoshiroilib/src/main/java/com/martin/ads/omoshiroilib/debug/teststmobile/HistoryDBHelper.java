package com.martin.ads.omoshiroilib.debug.teststmobile;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
public class HistoryDBHelper extends SQLiteOpenHelper{
        private static final String DB_NAME = "history.db";
        private static final int VERSION = 1;
        private static final String HISTORY_INFO = "history_info";
        private static HistoryDBHelper Helper;
        private static SQLiteDatabase Write;
        private static SQLiteDatabase Read;

    HistoryDBHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        public static HistoryDBHelper getInstance(Context context) {
            if (Helper == null) {
                Helper = new HistoryDBHelper(context);
            }
            return Helper;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE IF NOT EXISTS " + HISTORY_INFO +
                    "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " history VARCHAR NOT NULL);";
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

        public List<String> queryall() {
            String sql = "SELECT * FROM " + HISTORY_INFO;
            List<String> list = new ArrayList<>();
            Cursor cursor = Read.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String history = new String();
                history = cursor.getString(1);
                list.add(history);
            }
            return list;
        }


        public long insert_history(String history) {
            ContentValues values = new ContentValues();
            values.put("history", history);
            long result = Write.insert(HISTORY_INFO, null, values);
            return result;
        }

    public void delete_all_chosen() {
        Write.delete(HISTORY_INFO,"1=1",null);
    }
}
