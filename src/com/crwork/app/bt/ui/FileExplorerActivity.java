package com.crwork.app.bt.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.crwork.app.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * FileExplorerActivity
 * 
 * @author xiezhenlin
 *
 */
public class FileExplorerActivity extends Activity implements AdapterView.OnItemClickListener {

	private static final String LITTERDATA_FILE_NAME = "litterdata.txt";
	private ListView listView;
	private SimpleAdapter adapter;
	private String rootPath = Environment.getExternalStorageDirectory().getPath();
	private String currentPath = rootPath;
	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_explorer);
		listView = (ListView) findViewById(R.id.file_list_view);
		adapter = new SimpleAdapter(this, list, R.layout.ld_file_list_item, new String[] { "name", "img" },
				new int[] { R.id.filename, R.id.fileimg });
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		refreshListItems(currentPath);
	}

	private void refreshListItems(String path) {
		setTitle(path);
		File[] files = new File(path).listFiles();
		list.clear();
		if (files != null) {
			for (File file : files) {
				Map<String, Object> map = new HashMap<String, Object>();
				if (file.isDirectory()) {
					map.put("img", R.drawable.fm_folder);
				} else {
					map.put("img", R.drawable.fm_txt);
				}
				map.put("name", file.getName());
				map.put("currentPath", file.getPath());
				list.add(map);
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		currentPath = (String) list.get(position).get("currentPath");
		File file = new File(currentPath);
		if (file.isDirectory())
			refreshListItems(currentPath);
		else {
			if (LITTERDATA_FILE_NAME.equals(file.getName())) {
				Intent intent = new Intent();
				intent.putExtra("apk_path", file.getPath());
				setResult(RESULT_OK, intent);
				finish();
			} else {
				Toast.makeText(this, R.string.litterdatafilename, Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	public void onBackPressed() {
		if (rootPath.equals(currentPath)) {
			super.onBackPressed();
		} else {
			File file = new File(currentPath);
			currentPath = file.getParentFile().getPath();
			refreshListItems(currentPath);
		}
	}
}
