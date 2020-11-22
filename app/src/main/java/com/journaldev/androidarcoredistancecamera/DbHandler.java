package com.journaldev.androidarcoredistancecamera;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Vector;

public class DbHandler {
    private Context m_context = null;
    private SQLiteDatabase m_database = null;

    public DbHandler(Context context) {
        this.m_context = context;
        this.m_database = this.m_context.openOrCreateDatabase("FISHDB",
                Context.MODE_PRIVATE, null);
        createTables();
    }
    //Table Memory Structures
    public static class FishInfo {
        public int user_id;
        public String name;
        public float size;
        public byte[] image;
    }
    //Public methods

    public void insertInToFishInfo(int user_id, String name, float size, byte[] image, String date,
                                   float altitude, float latitude) {
        SQLiteStatement insertQuery = this.m_database.compileStatement("INSERT INTO FISH_INFO " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);");
        insertQuery.bindDouble(1, user_id);
        insertQuery.bindString(2, name);
        insertQuery.bindDouble(3, size);
        insertQuery.bindBlob(4, image);
        insertQuery.bindString(5, date);
        insertQuery.bindDouble(6, altitude);
        insertQuery.bindDouble(7, latitude);
        insertQuery.executeInsert();
    }

    public void delete () {
        String deleteQuery = "Delete From FISH_INFO";
        this.m_database.execSQL(deleteQuery);
    }

    public Vector<FishInfo> selectFromFishInfo(String name) {
        Cursor csr;
        String selectQuery = "SELECT * FROM FISH_INFO";
        if (name != null) {
            selectQuery = selectQuery + " WHERE NAME = ?";
            csr = this.m_database.rawQuery(selectQuery, new String[]{name});
        } else {
            csr = this.m_database.rawQuery(selectQuery,null);
        }
        return convertToFishInfos(csr);
    }
    //Private methods
    private void createTables() {
        String createFishInfo = "CREATE TABLE IF NOT EXISTS FISH_INFO (user_id INTEGER, " +
                " name TEXT, size REAL, image BLOB, date TEXT, altitude REAL, latitude REAL);";
        this.m_database.execSQL(createFishInfo);
    }

    private Vector<FishInfo> convertToFishInfos(Cursor csr) {
        Vector<FishInfo> fishInfos = new Vector<>();

        if (!csr.moveToFirst())
            return fishInfos;
        while(!csr.isAfterLast()) {
            FishInfo fishInfo = new FishInfo();
            fishInfo.user_id = csr.getInt(csr.getColumnIndex("user_id"));
            fishInfo.name = csr.getString(csr.getColumnIndex("name"));
            fishInfo.size = csr.getFloat(csr.getColumnIndex("size"));
            fishInfo.image = csr.getBlob(csr.getColumnIndex("image"));
            fishInfos.add(fishInfo);
            csr.moveToNext();
        }
        return fishInfos;
    }

}