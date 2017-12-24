package com.xinan.app.dao;

import java.util.ArrayList;

import com.xinan.app.database.WeightDatabaseHelper;
import com.xinan.app.domain.LitterDomain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class LitterDao {
	private final static String TAG = "LitterDao";
	private WeightDatabaseHelper mWeightDatabaseHelper;

	public LitterDao(Context context) {
		mWeightDatabaseHelper = new WeightDatabaseHelper(context);

	}

	/**
	 * insert litter data
	 * 
	 * @param mLitterDomain
	 * @return
	 */
	public boolean insertLitterData(LitterDomain mLitterDomain) {
		Log.e(TAG, "----insert----1");
		SQLiteDatabase db = mWeightDatabaseHelper.getWritableDatabase();
		Log.e(TAG,
				"----insert----2" + "userID:" + String.valueOf(mLitterDomain.getUserID()) + " LittertypeID:"
						+ String.valueOf(mLitterDomain.getLittertypeID()) + " Weight:"
						+ String.valueOf(mLitterDomain.getWeight()) + " Litterdate:"
						+ String.valueOf(mLitterDomain.getLitterdate()));
		ContentValues values = new ContentValues();
		values.put("userID", mLitterDomain.getUserID());
		values.put("littertypeID", mLitterDomain.getLittertypeID());
		values.put("weight", mLitterDomain.getWeight());
		values.put("litterdate", mLitterDomain.getLitterdate());
		db.insert(WeightDatabaseHelper.LITTERTABLE, null, values);
		Log.e(TAG, "----insert----3");
		db.close();
		return true;
	}

	/**
	 * query litter data
	 * 
	 * @param userID
	 * @return
	 */
	public ArrayList<LitterDomain> queryLitterData(int userID) {
		Log.e(TAG, "----query----1");
		SQLiteDatabase db = mWeightDatabaseHelper.getReadableDatabase();
		Cursor cursor;
		ArrayList<LitterDomain> list = new ArrayList<LitterDomain>();
		if (userID == 0) {
			cursor = db.rawQuery("SELECT * FROM " + WeightDatabaseHelper.LITTERTABLE, null);
		} else {
			cursor = db.rawQuery("SELECT * FROM " + WeightDatabaseHelper.LITTERTABLE + " where userID=?",
					new String[] { String.valueOf(userID) });
		}
		Log.e(TAG, "----query----2" + String.valueOf(cursor));
		while (cursor.moveToNext()) {
			LitterDomain mLitterDomain = new LitterDomain();
			mLitterDomain.setUserID(cursor.getInt(cursor.getColumnIndex("userID")));
			mLitterDomain.setLittertypeID(cursor.getInt(cursor.getColumnIndex("littertypeID")));
			mLitterDomain.setWeight(cursor.getDouble(cursor.getColumnIndex("weight")));
			mLitterDomain.setLitterdate(cursor.getString(cursor.getColumnIndex("litterdate")));
			Log.e(TAG, "----query----3" + mLitterDomain.toString());
			list.add(mLitterDomain);
		}
		cursor.close();
		db.close();
		if (list.size() == 0) {
			Log.e("SQLite", "****表中无数据****");
		}
		return list;
	}
}
