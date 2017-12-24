package com.xinan.app.bt.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xinan.app.R;
import com.xinan.app.adapter.LitterReportAdapter;
import com.xinan.app.dao.LitterDao;
import com.xinan.app.domain.LitterDomain;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Report activity
 * 
 * @author xiezhenlin
 *
 */
public class ReportActivity extends Activity implements OnClickListener {
	private final static String TAG = "ReportActivity";
	private ListView lv_litter_data_list;
	private EditText userid_fetch;
	private Button litter_data_fetch, litter_data_fetch_clean;
	private LitterReportAdapter mLitterReportAdapter;
	private LitterDao mLitterDao;
	ArrayList<LitterDomain> mLitterDomainList;
	private int userID_fetch = 0;
	List<Map<String, Object>> dataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.report);
		mLitterDao = new LitterDao(this);
		initview();
	}

	private void initview() {
		// TODO Auto-generated method stub
		lv_litter_data_list = (ListView) findViewById(R.id.lv_litter_data_list);
		userid_fetch = (EditText) findViewById(R.id.userid_fetch);
		litter_data_fetch = (Button) findViewById(R.id.litter_data_fetch);
		litter_data_fetch_clean = (Button) findViewById(R.id.litter_data_fetch_clean);
		if (!userid_fetch.getText().toString().equals("")) {
			userID_fetch = Integer.parseInt(userid_fetch.getText().toString());
		}
		litter_data_fetch.setOnClickListener(this);
		litter_data_fetch_clean.setOnClickListener(this);
		mLitterDomainList = mLitterDao.queryLitterData(userID_fetch);
		int litterlistSize = mLitterDomainList.size();
		if (litterlistSize == 0) {
			Toast.makeText(this, getResources().getString(R.string.litter_is_empty), Toast.LENGTH_LONG).show();
		} else {
			mLitterReportAdapter = new LitterReportAdapter(mLitterDomainList, this);
			lv_litter_data_list.setAdapter(mLitterReportAdapter);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!userid_fetch.getText().toString().equals("")) {
			userID_fetch = Integer.parseInt(userid_fetch.getText().toString());
		}
		mLitterDomainList = mLitterDao.queryLitterData(userID_fetch);
		mLitterReportAdapter.refresh(mLitterDomainList);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.litter_data_fetch:
			if (userid_fetch.getText().toString().equals("")) {
				Toast.makeText(this, getResources().getString(R.string.userid_query_hint), Toast.LENGTH_LONG).show();
				break;
			}
			mLitterDomainList = mLitterDao.queryLitterData(Integer.parseInt(userid_fetch.getText().toString()));

			Toast.makeText(this, "数据大小：" + String.valueOf(mLitterDomainList.size()), Toast.LENGTH_LONG).show();
			for (int i = 0; i < mLitterDomainList.size(); i++) {
				Log.i(TAG,
						"详细：" + "userID:" + String.valueOf(mLitterDomainList.get(i).getUserID()) + " LittertypeID:"
								+ String.valueOf(mLitterDomainList.get(i).getLittertypeID()) + " Weight:"
								+ String.valueOf(mLitterDomainList.get(i).getWeight()) + " Litterdate:"
								+ String.valueOf(mLitterDomainList.get(i).getLitterdate()));
			}
			mLitterReportAdapter.refresh(mLitterDomainList);
			break;
		case R.id.litter_data_fetch_clean:
			userid_fetch.setText("");
			userID_fetch = 0;
			mLitterDomainList = mLitterDao.queryLitterData(userID_fetch);
			mLitterReportAdapter.refresh(mLitterDomainList);
			break;
		}
	}

}
