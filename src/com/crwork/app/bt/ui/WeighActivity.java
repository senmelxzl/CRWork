package com.crwork.app.bt.ui;

import java.text.DecimalFormat;
import java.util.Set;

import com.crwork.app.R;
import com.crwork.app.bt.util.BTChatUtil;
import com.crwork.app.dao.LitterDao;
import com.crwork.app.domain.LitterDomain;
import com.crwork.app.util.LitterUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Weigh activity
 * 
 * @author xiezhenlin
 *
 */
public class WeighActivity extends Activity implements OnClickListener {
	private final static String TAG = "WeighActivity";
	private boolean LOG_DEBUG = false;
	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;
	private Context mContext;
	private boolean mBTConnected = false;

	private TextView btn_blth_connect_state;
	private Button btn_upload_weight;
	private boolean uploaded_success = false;

	private TextView tv_user_detect_id;
	private TextView tv_user_detect_name;
	private TextView tv_weight_count;
	private TextView tv_litter_cost;
	private TextView tv_litter_earning;
	private TextView tv_litter_type;
	RelativeLayout rlt_litter_switch;
	private ProgressDialog mProgressDialog;
	private BTChatUtil mBlthChatUtil;

	private String weigh_data = "0.00";
	private Double money_cost = 0.00;
	private Double money_earning = 0.00;
	private int litter_type_ID = LitterUtil.LITTER_TYPE_NO_R;
	private boolean weigh_ready = false;
	private int userID = 19000102;
	private String userName = "周咏�?";
	private boolean userdetected = false;
	private DecimalFormat df = new DecimalFormat("######0.00");

	private MenuItem action_bt_connect;
	private String deviceName;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTChatUtil.STATE_CONNECTED:
				deviceName = msg.getData().getString(BTChatUtil.DEVICE_NAME);
				mBTConnected = true;
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				break;
			case BTChatUtil.STATAE_CONNECT_FAILURE:
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mBTConnected = false;
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.connected_failed_tip),
						Toast.LENGTH_SHORT).show();
				break;
			case BTChatUtil.MESSAGE_DISCONNECTED:
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mBTConnected = false;
				break;
			case BTChatUtil.MESSAGE_READ: {
				weigh_data = msg.getData().getString(BTChatUtil.READ_MSG);
				money_cost = litter_type_ID == 0
						? Double.valueOf(weigh_data).doubleValue() * LitterUtil.LITTER_PRICE_NO_R : 0.00;
				money_earning = litter_type_ID == 1
						? Double.valueOf(weigh_data).doubleValue() * LitterUtil.LITTER_PRICE_YES_R : 0.00;
				if (LOG_DEBUG) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.bt_data_received_success) + weigh_data,
							Toast.LENGTH_SHORT).show();
				}
				Log.i(TAG, "weight data from BT" + weigh_data);
				weigh_ready = !weigh_data.equals("0.00");
				weigh_ready = true;// debug
				if (weigh_ready) {
					refeshdata();
				}
				break;
			}
			case BTChatUtil.MESSAGE_WRITE:
				break;
			default:
				break;
			}
			btn_blth_connect_state
					.setText(mBTConnected ? deviceName : getResources().getString(R.string.connect_state_init));
			action_bt_connect.setIcon(mBTConnected ? R.drawable.ic_bluetooth_connected_white_36dp
					: R.drawable.ic_bluetooth_disabled_white_36dp);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weigh);
		mContext = this;
		initView();
		initBluetooth();
		mBlthChatUtil = BTChatUtil.getInstance(mContext);
		mBlthChatUtil.registerHandler(mHandler);
		// initConnected();
		detectUserID();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult request=" + requestCode + " result=" + resultCode);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {

			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBlthChatUtil != null) {
			if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
				btn_blth_connect_state
						.setText(mBTConnected ? deviceName : getResources().getString(R.string.connect_state_init));
			}
		}
		refeshdata();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		mBlthChatUtil = null;
		unregisterReceiver(mBluetoothReceiver);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_blth_connect:
			if (!mBTConnected) {
				if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
					Toast.makeText(mContext, R.string.bt_connected_xn, Toast.LENGTH_SHORT).show();
				} else {
					discoveryDevices();
				}
			} else {
				if (mBlthChatUtil.getState() != BTChatUtil.STATE_CONNECTED) {
					Toast.makeText(mContext, R.string.bt_disconnected_xn, Toast.LENGTH_SHORT).show();
				} else {
					mBlthChatUtil.disconnect();
				}
			}
			break;
		case R.id.btn_upload_weight:
			UploadWeight();
			break;
		case R.id.rlt_litter_switch:
			SwitchLitterType();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.weight_menu, menu);
		action_bt_connect = menu.findItem(R.id.action_bt_connect);
		action_bt_connect.setIcon(mBTConnected ? R.drawable.ic_bluetooth_connected_white_36dp
				: R.drawable.ic_bluetooth_disabled_white_36dp);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_bt_connect:
			if (!mBTConnected) {
				if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
					Toast.makeText(mContext, R.string.bt_connected_xn, Toast.LENGTH_SHORT).show();
				} else {
					discoveryDevices();
				}
			} else {
				if (mBlthChatUtil.getState() != BTChatUtil.STATE_CONNECTED) {
					Toast.makeText(mContext, R.string.bt_disconnected_xn, Toast.LENGTH_SHORT).show();
				} else {
					mBlthChatUtil.disconnect();
				}
			}
			break;
		}
		return true;
	}

	/**
	 * init weight view for admin user
	 */
	private void initView() {
		btn_blth_connect_state = (TextView) findViewById(R.id.btn_blth_connect_state);
		rlt_litter_switch = (RelativeLayout) findViewById(R.id.rlt_litter_switch);
		btn_upload_weight = (Button) findViewById(R.id.btn_upload_weight);
		tv_user_detect_id = (TextView) findViewById(R.id.tv_user_detect_id);
		tv_user_detect_name = (TextView) findViewById(R.id.tv_user_detect_name);
		tv_weight_count = (TextView) findViewById(R.id.tv_weight_count);
		tv_litter_type = (TextView) findViewById(R.id.tv_litter_type);
		tv_litter_cost = (TextView) findViewById(R.id.tv_litter_cost);
		tv_litter_earning = (TextView) findViewById(R.id.tv_litter_earning);

		btn_upload_weight.setOnClickListener(this);
		rlt_litter_switch.setOnClickListener(this);
		mProgressDialog = new ProgressDialog(this);
	}

	/**
	 * init Bluetooth
	 */
	private void initBluetooth() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_unsupported),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mBluetoothReceiver, filter);
	}

	/**
	 * update view data from bt
	 */
	private void refeshdata() {
		// TODO Auto-generated method stub
		tv_weight_count.setText(weigh_data + getResources().getString(R.string.tv_weight_count_tip));
		tv_litter_cost.setText(getResources().getString(R.string.tv_litter_cost_tip) + df.format(money_cost)
				+ getResources().getString(R.string.tv_litter_money_tip));
		tv_litter_earning.setText(getResources().getString(R.string.tv_litter_earning_tip) + df.format(money_earning)
				+ getResources().getString(R.string.tv_litter_money_tip));
		// btn_upload_weight.setEnabled(mBTConnected && weigh_ready &&
		// userdetected);
		btn_upload_weight.setEnabled(true);
	}

	/**
	 * switch litter type
	 */
	private void SwitchLitterType() {
		// TODO Auto-generated method stub
		if (litter_type_ID == LitterUtil.LITTER_TYPE_NO_R) {
			litter_type_ID = LitterUtil.LITTER_TYPE_YES_R;
		} else {
			litter_type_ID = LitterUtil.LITTER_TYPE_NO_R;
		}
		tv_litter_type.setText(getResources()
				.getString(litter_type_ID == 0 ? R.string.litter_union_type : R.string.litter_recyclable_type));
		rlt_litter_switch.setBackgroundResource(
				litter_type_ID == 0 ? R.drawable.weight_background_union : R.drawable.weight_background_recyclable);
	}

	/**
	 * do upload data to server
	 */
	private void UploadWeight() {
		// TODO Auto-generated method stub
		LitterDomain mLitterDomain = new LitterDomain();
		mLitterDomain.setUserID(userID);
		mLitterDomain.setLittertypeID(litter_type_ID);
		mLitterDomain.setWeight(Double.valueOf(weigh_data).doubleValue());
		mLitterDomain.setLitterdate(LitterUtil.getLitterDate());
		LitterDao mLitterDao = new LitterDao(this);
		uploaded_success = mLitterDao.insertLitterData(mLitterDomain);
		if (uploaded_success) {
			uploaded_success = false;
			Toast.makeText(mContext, R.string.upload_success, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, R.string.upload_fail, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * start discovery bt devices
	 */
	private void discoveryDevices() {
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
		if (mBluetoothAdapter.isDiscovering()) {
			return;
		}
		mProgressDialog.setTitle(getResources().getString(R.string.progress_scaning));
		mProgressDialog.show();
		mBluetoothAdapter.startDiscovery();

	}

	private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "mBluetoothReceiver action =" + action);
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (scanDevice == null || scanDevice.getName() == null) {
					return;
				}

				Log.d(TAG, "name=" + scanDevice.getName() + "address=" + scanDevice.getAddress());

				String name = scanDevice.getName();
				String address = scanDevice.getAddress();
				if ((name != null && name.equals(BTChatUtil.BLUETOOTH_NAME))
						|| (address != null && address.equals(BTChatUtil.BLUETOOTH_ADDRESS))) {
					mBluetoothAdapter.cancelDiscovery();
					mProgressDialog.setTitle(getResources().getString(R.string.progress_connecting));
					mBlthChatUtil.connect(scanDevice);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				// do something
			}
		}
	};

	@SuppressWarnings("unused")
	private void getBtDeviceInfo() {
		String name = mBluetoothAdapter.getName();
		String address = mBluetoothAdapter.getAddress();

		Log.d(TAG, "bluetooth name =" + name + " address =" + address);
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		Log.d(TAG, "bonded device size =" + devices.size());
		for (BluetoothDevice bonddevice : devices) {
			Log.d(TAG, "bonded device name =" + bonddevice.getName() + " address" + bonddevice.getAddress());
		}
	}

	/**
	 * init connection
	 */
	private void initConnected() {
		// TODO Auto-generated method stub
		if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
			Toast.makeText(mContext, R.string.bt_connected_xn, Toast.LENGTH_SHORT).show();
		} else {
			discoveryDevices();
		}

	}

	/**
	 * detected user cardID
	 */
	private void detectUserID() {
		// TODO Auto-generated method stub
		if (userID > 0) {
			userdetected = true;
			tv_user_detect_id.setText(String.valueOf(userID));
			tv_user_detect_name.setText(userName);
		}
	}
}
