package com.xinan.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeightDatabaseHelper extends SQLiteOpenHelper {
	// DB name
	public static final String DBNAME = "crwork.db";
	// DB version number
	public static final int VERSION = 2;
	// data table
	public static final String LITTERTABLE = "litter";
	public static final String LITTERTYPETABLE = "littertype";
	public static final String USERTABLE = "user";
	public static final String VILLAGETABLE = "village";

	public WeightDatabaseHelper(Context context) {
		super(context, DBNAME, null, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS " + LITTERTABLE
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, littertypeID INTEGER, weight REAL, litterdate TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + LITTERTYPETABLE
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, littertypeID INTEGER, typeName TEXT, typemark INTEGER, price REAL)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + USERTABLE
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, userName TEXT, villageID INTEGER, userType INTEGER,registeredDate TEXT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + VILLAGETABLE
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, villageID INTEGER, villageName TEXT, belongID INTEGER, statusMark INTEGER)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * 数据库打开时，此方法会调用
	 */
	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

}
