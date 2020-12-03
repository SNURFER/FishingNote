package com.journaldev.androidarcoredistancecamera;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.Vector;

public class DbHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "FISHDB";
    private static DbHandler m_dbHandler = null;

    private SQLiteDatabase m_database;

    public static DbHandler getInstance(Context context) {
        if (m_dbHandler == null)
            m_dbHandler = new DbHandler(context);
        return m_dbHandler;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables();
        insertIntoFishTypes("참돔");
        insertIntoFishTypes("배스");
        insertIntoFishTypes("붉바리");
        insertIntoFishTypes("돌돔");
        insertIntoFishTypes("송어");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables();
    }

    //Table Memory Structures
    public static class FishInfo {
        public int id;
        public String name;
        public float size;
        public byte[] image;
    }
    //Public methods

    public void insertInToFishInfo(String name, float size, byte[] image, String date,
                                   float altitude, float latitude) {
        SQLiteStatement insertQuery = this.m_database.compileStatement("INSERT INTO FISH_INFO " +
                "VALUES (NULL, ?, ?, ?, ?, ?, ?);");
        insertQuery.bindString(1, name);
        insertQuery.bindDouble(2, size);
        insertQuery.bindBlob(3, image);
        insertQuery.bindString(4, date);
        insertQuery.bindDouble(5, altitude);
        insertQuery.bindDouble(6, latitude);
        insertQuery.executeInsert();
    }

    public void insertIntoFishTypes(String fishType) {
        SQLiteStatement insertQuery = this.m_database.compileStatement("INSERT INTO FISH_TYPES " +
                "VALUES (?);");
        insertQuery.bindString(1, fishType);
        insertQuery.executeInsert();
    }

    public void deleteAll () {
        String deleteQuery = "Delete From FISH_INFO";
        this.m_database.execSQL(deleteQuery);
    }

    public void deleteSingle (int id) {
        String deleteQuery = "Delete From FISH_INFO WHERE id = " + id;
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
    private DbHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        m_database = getWritableDatabase();
    }

    private void createTables() {
        String createFishInfo = "CREATE TABLE IF NOT EXISTS FISH_INFO (id INTEGER PRIMARY KEY, " +
                " name TEXT, size REAL, image BLOB, date TEXT, altitude REAL, latitude REAL);";
        String createFishtypes = "CREATE TABLE IF NOT EXISTS FISH_TYPES(fish_type TEXT);";
        this.m_database.execSQL(createFishInfo);
        this.m_database.execSQL(createFishtypes);
    }

    private void dropTables() {
        String dropFishInfo = "DROP TABLE IF EXISTS FISH_INFO;";
        String dropFishTypes = "DROP TABLE IF EXISTS FISH_TYPES;";
        this.m_database.execSQL(dropFishInfo);
        this.m_database.execSQL(dropFishTypes);
    }

    private Vector<FishInfo> convertToFishInfos(Cursor csr) {
        Vector<FishInfo> fishInfos = new Vector<>();

        if (!csr.moveToFirst())
            return fishInfos;
        while(!csr.isAfterLast()) {
            FishInfo fishInfo = new FishInfo();
            fishInfo.id = csr.getInt(csr.getColumnIndex("id"));
            fishInfo.name = csr.getString(csr.getColumnIndex("name"));
            fishInfo.size = csr.getFloat(csr.getColumnIndex("size"));
            fishInfo.image = csr.getBlob(csr.getColumnIndex("image"));
            fishInfos.add(fishInfo);
            csr.moveToNext();
        }
        return fishInfos;
    }
}