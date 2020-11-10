package com.journaldev.androidarcoredistancecamera;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Vector;

public class DbHandler {
    private Context context = null;
    private SQLiteDatabase database = null;

    public DbHandler(Context context) {
        this.context = context;
        this.database = this.context.openOrCreateDatabase("FISHDB",
                Context.MODE_PRIVATE, null);
        CreateTables();
    }
    //Table Memory Structures
    public static class FishInfo {
        public int user_id;
        public String name;
        public float size;
        public byte[] image;
    }
    //Public methods

    public void InsertInToFishInfo(int user_id, String name, float size, byte[] image) {
        SQLiteStatement insert_query = this.database.compileStatement("INSERT INTO FISH_INFO " +
                "VALUES (?, ?, ?, ?);");
        insert_query.bindDouble(1, user_id);
        insert_query.bindString(2, name);
        insert_query.bindDouble(3, size);
        insert_query.bindBlob(3, image);
        insert_query.executeInsert();
    }

    public void Delete (){
        String delete_query_ = "Delete From FISH_INFO";
        this.database.execSQL(delete_query_);
    }

    public Vector<FishInfo> SelectFromFishInfo(String name) {
        Cursor csr;
        String select_query = "SELECT * FROM FISH_INFO";
        if (name != null) {
            select_query = select_query + " WHERE NAME = ?";
            csr = this.database.rawQuery(select_query, new String[]{name});
        } else {
            csr = this.database.rawQuery(select_query,null);
        }
        return ConvertToFishInfos(csr);
    }
    //Private methods
    private void CreateTables() {
        String create_fish_info = "CREATE TABLE IF NOT EXISTS FISH_INFO (user_id INTEGER, " +
                " name TEXT, size REAL, image BLOB);";
        this.database.execSQL(create_fish_info);
    }

    private Vector<FishInfo> ConvertToFishInfos(Cursor csr) {
        Vector<FishInfo> fish_infos = new Vector<>();

        if (!csr.moveToFirst())
            return fish_infos;
        while(!csr.isAfterLast()) {
            FishInfo fish_info = new FishInfo();
            fish_info.user_id = csr.getInt(csr.getColumnIndex("user_id"));
            fish_info.name = csr.getString(csr.getColumnIndex("name"));
            fish_info.size = csr.getFloat(csr.getColumnIndex("size"));
            fish_info.image = csr.getBlob(csr.getColumnIndex("image"));
            fish_infos.add(fish_info);
            csr.moveToNext();
        }
        return fish_infos;
    }
}