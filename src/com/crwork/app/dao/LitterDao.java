package com.crwork.app.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.crwork.app.database.WeightDatabaseHelper;
import com.crwork.app.domain.LitterDomain;
import com.crwork.app.util.LitterUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 
 * @author xiezhenlin
 *
 */
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
		Log.i(TAG, "----insert----1");
		SQLiteDatabase db = mWeightDatabaseHelper.getWritableDatabase();
		Log.i(TAG,
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
		Log.i(TAG, "----insert----3");
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
		Log.i(TAG, "----query----1");
		SQLiteDatabase db = mWeightDatabaseHelper.getReadableDatabase();
		Cursor cursor;
		ArrayList<LitterDomain> list = new ArrayList<LitterDomain>();
		if (userID == 0) {
			cursor = db.rawQuery("SELECT * FROM " + WeightDatabaseHelper.LITTERTABLE, null);
		} else {
			cursor = db.rawQuery("SELECT * FROM " + WeightDatabaseHelper.LITTERTABLE + " where userID=?",
					new String[] { String.valueOf(userID) });
		}
		Log.i(TAG, "----query----2" + String.valueOf(cursor));
		while (cursor.moveToNext()) {
			LitterDomain mLitterDomain = new LitterDomain();
			mLitterDomain.setUserID(cursor.getInt(cursor.getColumnIndex("userID")));
			mLitterDomain.setLittertypeID(cursor.getInt(cursor.getColumnIndex("littertypeID")));
			mLitterDomain.setWeight(cursor.getDouble(cursor.getColumnIndex("weight")));
			mLitterDomain.setLitterdate(cursor.getString(cursor.getColumnIndex("litterdate")));
			Log.i(TAG, "----query----3" + mLitterDomain.toString());
			list.add(mLitterDomain);
		}
		cursor.close();
		db.close();
		if (list.size() == 0) {
			Log.i(TAG, "****表中无数据****");
		}
		return list;
	}

	/**
	 * analysis txt content
	 * 
	 * @param dataFile
	 * @return
	 */
	public ArrayList<LitterDomain> readTXT(File dataFile) {
		// TODO Auto-generated method stub
		ArrayList<LitterDomain> list = new ArrayList<LitterDomain>();
		BufferedReader reader = null;
		String temp = null;
		int line = 1;
		try {
			reader = new BufferedReader(new FileReader(dataFile));
			while ((temp = reader.readLine()) != null) {
				System.out.println(TAG + line + ":" + temp);
				Log.i(TAG, "----read data by line----" + temp);
				LitterDomain mLitterDomain = new LitterDomain();
				String[] list_temp = temp.split(" ");
				mLitterDomain.setUserID(Integer.parseInt(list_temp[0]));
				mLitterDomain.setLittertypeID(Integer.parseInt(list_temp[1]));
				mLitterDomain.setWeight(Double.parseDouble(list_temp[2]));
				mLitterDomain.setLitterdate(LitterUtil.getLitterDate());
				list.add(mLitterDomain);
				line++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	/**
	 * for upload litter data from local file
	 * 
	 * @param mLitterDomainList
	 * @return
	 */
	public boolean uploadLitterlistData(ArrayList<LitterDomain> mLitterDomainList) {
		for (int i = 0; i < mLitterDomainList.size(); i++) {
			if (insertLitterData(mLitterDomainList.get(i))) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
}
