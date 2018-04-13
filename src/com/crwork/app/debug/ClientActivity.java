package com.crwork.app.debug;

import java.util.Set;

import com.crwork.app.R;
import com.crwork.app.bt.util.BTChatUtil;

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
 * client connection debug
 * 
 * @author xiezhenlin
 *
 */
public class ClientActivity extends Activity implements OnClickListener {
	private final static String TAG = "ClientActivity";
	public String BLUETOOTH_NAME = "HC-06";
	public String BLUETOOTH_ADDRESS = "20:17:08:14:93:15";
	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;
	private Context mContext;

	private Button mBtnBluetoothConnect;
	private Button mBtnBluetoohDisconnect;
	private Button mBtnSendMessage;
	private EditText mEdttMessage;
	private EditText et_blth_name, et_blth_address;

	private TextView mBtConnectState;
	private TextView mTvChat;
	private ProgressDialog mProgressDialog;
	private BTChatUtil mBlthChatUtil;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BTChatUtil.STATE_CONNECTED:
				String deviceName = msg.getData().getString(BTChatUtil.DEVICE_NAME);
				mBtConnectState.setText(getResources().getString(R.string.connected_success_tip) + deviceName);
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
				byte[] buf = msg.getData().getByteArray(BTChatUtil.READ_MSG);
				String str = new String(buf, 0, buf.length);
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.bt_data_received_success) + str, Toast.LENGTH_SHORT).show();
				Log.i(TAG, str);
				mTvChat.setText(getResources().getString(R.string.bt_data_received_show) + str + "\n");

				break;
			}
			case BTChatUtil.MESSAGE_WRITE: {
				byte[] buf = (byte[]) msg.obj;
				String str = new String(buf, 0, buf.length);
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.bt_data_send_success) + str,
						Toast.LENGTH_SHORT).show();
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
		setContentView(R.layout.client);
		mContext = this;
		initView();
		initBluetooth();
		mBlthChatUtil = BTChatUtil.getInstance(mContext);
		mBlthChatUtil.registerHandler(mHandler);
	}

	private void initView() {
		mBtnBluetoothConnect = (Button) findViewById(R.id.btn_blth_connect);
		mBtnBluetoohDisconnect = (Button) findViewById(R.id.btn_blth_disconnect);
		mBtnSendMessage = (Button) findViewById(R.id.btn_sendmessage);
		mEdttMessage = (EditText) findViewById(R.id.edt_message);
		et_blth_name = (EditText) findViewById(R.id.et_blth_name);
		et_blth_address = (EditText) findViewById(R.id.et_blth_address);
		et_blth_name.setText(BLUETOOTH_NAME);
		et_blth_address.setText(BLUETOOTH_ADDRESS);
		mBtConnectState = (TextView) findViewById(R.id.tv_connect_state);
		mTvChat = (TextView) findViewById(R.id.tv_chat);

		mBtnBluetoothConnect.setOnClickListener(this);
		mBtnBluetoohDisconnect.setOnClickListener(this);
		mBtnSendMessage.setOnClickListener(this);
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
					mBtConnectState
							.setText(getResources().getString(R.string.connected_success_tip) + device.getName());
				} else {
					mBtConnectState.setText(R.string.connected_success_tip);
				}
			}
		}
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
		case R.id.btn_sendmessage:
			String messagesend = mEdttMessage.getText().toString();
			if (null == messagesend || messagesend.length() == 0) {
				return;
			}
			mBlthChatUtil.write(messagesend.getBytes());
			break;
		default:
			break;
		}
	}

	private void discoveryDevices() {
		BLUETOOTH_NAME = et_blth_name.getText().toString();
		BLUETOOTH_ADDRESS = et_blth_address.getText().toString();
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
