package com.xajd.wechat_01;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    //create table if not exists Student(name text primary key, code integer);
    //创建数据库
    public MyDataBaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    //建表  表若存在，就不会再建了
    public void OnCreatTable(SQLiteDatabase db,String sql){
        db.execSQL(sql);
    }

    //升级数据库
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
    }
}
