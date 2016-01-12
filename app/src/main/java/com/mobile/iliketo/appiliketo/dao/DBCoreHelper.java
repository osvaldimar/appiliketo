package com.mobile.iliketo.appiliketo.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by OSVALDIMAR on 8/28/2015.
 */
public class DBCoreHelper extends SQLiteOpenHelper {

    private static final String NAME_DB = "db_iliketo";
    private static final int VERSION_DB = 1;

    public DBCoreHelper(Context context){
        super(context, NAME_DB, null, VERSION_DB);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table tbl_user(id integer primary key, usuario text not null, senha text not null, ativado integer not null);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("drop table tbl_user;");
        onCreate(db);
    }

}
