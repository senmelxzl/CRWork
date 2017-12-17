package com.xinan.app.bt.ui;

import java.util.Set;

import com.xinan.app.R;
import com.xinan.app.bt.util.BTChatUtil;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
	private boolean LOG_DEBUG=false;
	public String BLUETOOTH_NAME = "HC-06";
	public String BLUETOOTH_ADDRESS = "20:17:08:14:93:15";
	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;
	private Context mContext;
	private boolean mBTConnected = false;

	private Button mBtnBluetoothConnect;
	private Button mBtnBluetoohDisconnect;

	private TextView mBtConnectState;
	private TextView tv_weight_count;
	private ProgressDialog mProgressDialog;
	private BTChatUtil mBlthChatUtil;
	private String weigh_data="0.00";

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTChatUtil.STATE_CONNECTED:
				String deviceName = msg.getData().getString(BTChatUtil.DEVICE_NAME);
				mBtConnectState.setText(deviceName);
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				break;
			case BTChatUtil.STATAE_CONNECT_FAILURE:
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.connected_failed_tip),
						Toast.LENGTH_SHORT).show();
				break;
			case BTChatUtil.MESSAGE_DISCONNECTED:
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mBtConnectState.setText(R.string.disconnected_device_tip);
				break;
			case BTChatUtil.MESSAGE_READ: {
				String str =msg.getData().getString(BTChatUtil.READ_MSG);
				if(LOG_DEBUG){
					Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.bt_data_received_success) + str.substring(7, 14).trim(), Toast.LENGTH_SHORT).show();
				}
				Log.i(TAG, str);
				tv_weight_count.setText(str + getResources().getString(R.string.tv_weight_count_tip));

				break;
			}
			case BTChatUtil.MESSAGE_WRITE: {
				byte[] buf = (byte[]) msg.obj;
				String str = new String(buf, 0, buf.length);
				if(LOG_DEBUG){
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_data_send_success) + str,
						Toast.LENGTH_SHORT).show();
				}
				break;
			}
			default:
				break;
			}
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
		initConnected();
	}

	private void initConnected() {
		// TODO Auto-generated method stub
		if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
			Toast.makeText(mContext, R.string.bt_connected_xn, Toast.LENGTH_SHORT).show();
		} else {
			discoveryDevices();
		}
		
	}

	private void initView() {
		mBtnBluetoothConnect = (Button) findViewById(R.id.btn_blth_weight);
		mBtnBluetoohDisconnect = (Button) findViewById(R.id.btn_blth_disconnect);
		mBtnBluetoothConnect.setVisibility(LOG_DEBUG?View.VISIBLE:View.INVISIBLE);
		mBtnBluetoohDisconnect.setVisibility(LOG_DEBUG?View.VISIBLE:View.INVISIBLE);
		mBtConnectState = (TextView) findViewById(R.id.tv_connect_state);
		tv_weight_count = (TextView) findViewById(R.id.tv_weight_count);
		tv_weight_count.setText(weigh_data + getResources().getString(R.string.tv_weight_count_tip));

		mBtnBluetoothConnect.setOnClickListener(this);
		mBtnBluetoohDisconnect.setOnClickListener(this);
		mProgressDialog = new ProgressDialog(this);
	}

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
				BluetoothDevice device = mBlthChatUtil.getConnectedDevice();
				if (null != device && null != device.getName()) {
					mBtConnectState.setText(device.getName());
				} else {
					mBtConnectState.setText(R.string.connected_success_tip);
				}
			}
		}
		tv_weight_count.setText(weigh_data + getResources().getString(R.string.tv_weight_count_tip));
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
		case R.id.btn_blth_weight:
			if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
				Toast.makeText(mContext, R.string.bt_connected_xn, Toast.LENGTH_SHORT).show();
			} else {
				discoveryDevices();
			}
			break;
		case R.id.btn_blth_disconnect:
			if (mBlthChatUtil.getState() != BTChatUtil.STATE_CONNECTED) {
				Toast.makeText(mContext, R.string.bt_disconnected_xn, Toast.LENGTH_SHORT).show();
			} else {
				mBlthChatUtil.disconnect();
			}
			break;
		default:
			break;
		}
	}

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
				if ((name != null && name.equals(BLUETOOTH_NAME))
						|| (address != null && address.equals(BLUETOOTH_ADDRESS))) {
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
}
