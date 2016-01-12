package com.mobile.iliketo.appiliketo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mobile.iliketo.appiliketo.model.User;

/**
 * Created by OSVALDIMAR on 8/28/2015.
 */
public class DBILiketo {

    private SQLiteDatabase db;
    private String tableUser = "tbl_user";

    public DBILiketo(Context context){
        DBCoreHelper helper = new DBCoreHelper(context);
        db = helper.getWritableDatabase();
    }

    public void insert(User user ){
        this.delete(1);
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("usuario", user.getUsername());
        values.put("senha", user.getPassword());
        values.put("ativado", user.getAtivado());

        Log.i("LOG ILIKETO", "DBILiketo insert = " + user.getUsername());
        db.insert(tableUser, null, values);
    }

    public void update(User user){
        ContentValues values = new ContentValues();
        values.put("usuario", user.getUsername());
        values.put("senha", user.getPassword());
        values.put("ativado", user.getAtivado());

        Log.i("LOG ILIKETO", "DBILiketo update = " + user.getUsername());
        db.update(tableUser, values, "id = 1", null);
    }

    public void delete(int id){
        Log.i("LOG ILIKETO", "DBILiketo delete = " + id);
        db.delete(tableUser, "id = " + id, null);
    }

    public User readById(int id){
        User user = new User();
        String[] colunas = new String[]{"usuario", "senha", "ativado"};

        Cursor cursor = db.query(tableUser, colunas, null, null, null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                user.setUsername(cursor.getString(0));
                user.setPassword(cursor.getString(1));
                user.setAtivado(Integer.parseInt(cursor.getString(2)));
            }while(cursor.moveToNext());
            return(user);
        }
        return null;
    }

}
