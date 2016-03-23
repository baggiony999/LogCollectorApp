package util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    //   public static final String DATABASE_NAME = "/sdcard/CER/databases/speedtest";
    //public static final String DATABASE_NAME = "/sdcard/bgspeed";
    //   public static final String DATABASE_NAME = "/data/data/org.zwanoo.android.speedtest/databases/speedtest";
    String dbLocation;
    String[] cols = new String[]{"conntype", "latitude", "longitude", "download", "upload", "latency", "serverid", "servername", "date", "internalip", "externalip", "data", "downloadBytes", "uploadBytes"};

    public DBHelper(Context context, String dbLocation) {
        super(context, dbLocation, null, 11);
        U.d("dbLocation=" + dbLocation);
    }

    public Map<String, String> getLastResult() {
        Map<String, String> r = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from results order by date desc", null);
        int colCnt = res.getColumnCount();
        if (res.moveToFirst()) {
            for (int i = 0; i < colCnt; i++) {
                String colName = res.getColumnName(i);
                r.put(colName, res.getString(i));
            }
        }
        return r;
    }

    public ArrayList<String> getAllData() {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from results", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex("latitude")));
            res.moveToNext();
        }
        return array_list;
    }

    //-------------------------------------------------------------------
    @Override
    public void onCreate(SQLiteDatabase db) {
        U.d("onCreate...");
        //  db.execSQL("create table contacts " + "(id integer primary key, name text,phone text,email text, street text,place text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        U.d("onUpgrade...");
//        db.execSQL("DROP TABLE IF EXISTS contacts");
//        onCreate(db);
    }

    public boolean insertContact(String name, String phone, String email, String street, String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.insert("contacts", null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts where id=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "results");
        return numRows;
    }

    public boolean updateContact(Integer id, String name, String phone, String email, String street, String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update("contacts", contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts", "id = ? ", new String[]{Integer.toString(id)});
    }


}
