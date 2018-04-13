package com.crwork.app.bt.ui;

import java.io.File;
import java.util.ArrayList;

import com.crwork.app.R;
import com.crwork.app.adapter.LitterReportAdapter;
import com.crwork.app.dao.LitterDao;
import com.crwork.app.domain.LitterDomain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * UploadLitterDataActivity
 * 
 * @author xiezhenlin
 *
 */
public class UploadLitterDataActivity extends Activity implements OnClickListener {
	private Context mContext;
	private final static int XN_LD_SELECTED = 1;

	private final static int STATE_NO_DATA_FILE = 0;
	private final static int STATE_NO_DATA = 1;
	private final static int STATE_HAS_DATA = 2;
	private Button ld_loadfile_btn;
	private Button ld_upload_btn;
	private TextView ld_file_name;
	private File dataFile = null;
	private boolean uploaded_success = false;

	private ListView listView;
	private TextView ld_upload_content_listdata_empty;
	private LitterReportAdapter mLitterReportAdapter;
	private LitterDao mLitterDao;
	private ArrayList<LitterDomain> mLitterDomainList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uploadlitterdata);
		LoadSet();
		LoadView();
		LoadData();
	}

	private void LoadSet() {
		// TODO Auto-generated method stub
		mContext = this;
		mLitterDao = new LitterDao(mContext);
	}

	private void LoadView() {
		// TODO Auto-generated method stub
		ld_loadfile_btn = (Button) findViewById(R.id.ld_loadfile_btn);
		ld_loadfile_btn.setText(this.getString(R.string.litterdata_upload_btn_str));
		ld_loadfile_btn.setOnClickListener(this);

		ld_upload_btn = (Button) findViewById(R.id.ld_upload_btn);
		ld_upload_btn.setOnClickListener(this);

		ld_file_name = (TextView) findViewById(R.id.ld_file_name);

		listView = (ListView) findViewById(R.id.ld_upload_content_listdata);
		ld_upload_content_listdata_empty = (TextView) findViewById(R.id.ld_upload_content_listdata_empty);
	}

	private void LoadData() {
		// TODO Auto-generated method stub
		if (dataFile != null) {
			ld_file_name.setText(dataFile.getName());
			mLitterDomainList = mLitterDao.readTXT(dataFile);
			if (mLitterDomainList != null && mLitterDomainList.size() != 0) {
				mLitterReportAdapter = new LitterReportAdapter(mLitterDomainList, this);
				dataShowView(STATE_HAS_DATA);
				listView.setAdapter(mLitterReportAdapter);
			} else {
				dataShowView(STATE_NO_DATA);
			}
		} else {
			dataShowView(STATE_NO_DATA_FILE);
		}
	}

	private void dataShowView(int Mark) {
		// TODO Auto-generated method stub
		if (Mark == STATE_HAS_DATA) {
			listView.setVisibility(View.VISIBLE);
			ld_upload_btn.setVisibility(View.VISIBLE);
			ld_upload_content_listdata_empty.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			ld_upload_content_listdata_empty.setVisibility(View.VISIBLE);
			ld_upload_btn.setVisibility(View.GONE);
			if (Mark == STATE_NO_DATA) {
				ld_upload_content_listdata_empty.setText(getResources().getString(R.string.litterdatafile_empty));
			} else if (Mark == STATE_NO_DATA_FILE) {
				ld_file_name.setText(getResources().getString(R.string.litterdatafile_not_exsit));
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ld_loadfile_btn:
			openFileSelect();
			break;
		case R.id.ld_upload_btn:
			litterDataUpload();
			break;
		default:
			break;

		}
	}

	/**
	 * select litter data from phone storage
	 */
	private void openFileSelect() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FileExplorerActivity.class);
		startActivityForResult(intent, XN_LD_SELECTED);
	}

	/**
	 * Upload litter data from local data file
	 */
	private void litterDataUpload() {
		// TODO Auto-generated method stub
		uploaded_success = mLitterDao.uploadLitterlistData(mLitterDomainList);
		if (uploaded_success) {
			uploaded_success = false;
			Toast.makeText(mContext, R.string.upload_success, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, R.string.upload_fail, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			dataFile = new File(data.getStringExtra("apk_path"));
			LoadData();
		}
	}
}
