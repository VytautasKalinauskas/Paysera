package com.example.vytkal.paysera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vytkal on 5/23/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "Data2.db";
    public static final String TABLE_VALIUTOS = "Valiutos";
    public static final String TABLE_KOMISINIAI = "Likuciai";

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + TABLE_VALIUTOS + " (EUR DOUBLE, USD DOUBLE, JPY DOUBLE)");
        db.execSQL("create table " + TABLE_KOMISINIAI + " (EUR DOUBLE, USD DOUBLE, JPY DOUBLE, Amountkom INT)");
        db.execSQL("Insert INTO Valiutos(EUR) Values (1000)" );
        db.execSQL("INSERT INTO Likuciai(EUR, USD, JPY, Amountkom) Values (0.00, 0.00, 0.00, 0)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VALIUTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KOMISINIAI);
        onCreate(db);
    }

    /*
    getBalance
    @param currency - valiutos, kurios balansas ieskomas, pavadinimas
    @return double - grazinamas nurodytos valiutos balansas
     */
    public double getBalance(String currency) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select " + currency + " FROM " + TABLE_VALIUTOS, null);
        if (res.getCount() == 0)
            return -1;
        else {
            res.moveToFirst();
            return res.getFloat(0);
        }

    }

    /*
    getAmountKom
    @return int - grazinamas ivykdytu konvertaciju kiekis
     */
    public int getAmountKom() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select Amountkom FROM " + TABLE_KOMISINIAI, null);
        if (res.getCount() == 0)
            return -1;
        else {
            res.moveToFirst();
            return res.getInt(0);
        }

    }

    /*
    getKom
    @return Cursor - grazinamas kursorius, kuriame yra visomis valiutomis sumoketi komisiniai mokesciai
     */
    public Cursor getKom() {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * FROM " + TABLE_KOMISINIAI, null);
        if (res.getCount() == 0)
            return null;
        else {
            return res;
        }

    }

    /*
       changeBalance
       @param currencyFrom - valiuta, is kurios konvertuojama
       @param amountFrom - valiutos, is kurios konvertuojama kiekis
       @param currencyTo - valiuta, i kuria konvertuojama
       @param amountTo - valiutos, i kuria konvertuojama kiekis
       @param kom - komisinis mokestis procentais
    */
    public void changeBalance(String currencyFrom, double amountFrom, String currencyTo, double amountTo, double kom) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(currencyFrom, getBalance(currencyFrom) - amountFrom);
        values.put(currencyTo, getBalance(currencyTo) + amountTo);

        db.update(TABLE_VALIUTOS, values, null, null);
        db.execSQL("UPDATE " + TABLE_KOMISINIAI +  " SET Amountkom = Amountkom + 1 ");
        db.execSQL("UPDATE " + TABLE_KOMISINIAI +  " SET " + currencyFrom + " = " + currencyFrom + " + " + amountFrom + " * " + kom);

    }

}
