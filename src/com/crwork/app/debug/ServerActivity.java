package com.crwork.app.debug;

import com.crwork.app.R;
import com.crwork.app.bt.util.BTChatUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
 * server debug activity
 * 
 * @author xiezhenlin
 *
 */
public class ServerActivity extends Activity implements OnClickListener {
	private final static String TAG = "ServerActivity";
	private BluetoothAdapter mBluetoothAdapter;
	private int REQUEST_ENABLE_BT = 1;
	private Context mContext;

	private Button mBtnBluetoothVisibility;
	private Button mBtnBluetoohDisconnect;
	private Button mBtnSendMessage;
	private EditText mEdttMessage;

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
				mBlthChatUtil.startListen();
				break;
			case BTChatUtil.MESSAGE_READ: {
				byte[] buf = msg.getData().getByteArray(BTChatUtil.READ_MSG);
				String str = new String(buf, 0, buf.length);
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.bt_data_received_success) + str, Toast.LENGTH_SHORT).show();
				mTvChat.setText(mTvChat.getText().toString() + "\n" + str);
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
		setContentView(R.layout.server);
		mContext = this;
		initView();
		initBluetooth();
		mBlthChatUtil = BTChatUtil.getInstance(mContext);
		mBlthChatUtil.registerHandler(mHandler);
	}

	private void initView() {

		mBtnBluetoothVisibility = (Button) findViewById(R.id.btn_blth_visiblity);
		mBtnBluetoohDisconnect = (Button) findViewById(R.id.btn_blth_disconnect);
		mBtnSendMessage = (Button) findViewById(R.id.btn_sendmessage);
		mEdttMessage = (EditText) findViewById(R.id.edt_message);
		mBtConnectState = (TextView) findViewById(R.id.tv_connect_state);
		mTvChat = (TextView) findViewById(R.id.tv_chat);

		mBtnBluetoothVisibility.setOnClickListener(this);
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
		if (mBluetoothAdapter.isEnabled()) {
			if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
				startActivity(discoverableIntent);
			}
		}
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
		if (!mBluetoothAdapter.isEnabled())
			return;
		if (mBlthChatUtil != null) {
			if (mBlthChatUtil.getState() == BTChatUtil.STATE_NONE) {
				mBlthChatUtil.startListen();
			} else if (mBlthChatUtil.getState() == BTChatUtil.STATE_CONNECTED) {
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
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btn_blth_visiblity:
			if (mBluetoothAdapter.isEnabled()) {
				if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
					startActivity(discoveryIntent);
				}
			} else {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.bluetooth_unopened),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_blth_disconnect:
			if (mBlthChatUtil.getState() != BTChatUtil.STATE_CONNECTED) {
				Toast.makeText(mContext, getResources().getString(R.string.bt_connected_xn), Toast.LENGTH_SHORT).show();
			} else {
				mBlthChatUtil.disconnect();
			}
			break;
		case R.id.btn_sendmessage:
			String messagesend="ST,GS,+   "+String.valueOf((int)(Math.random()*100))+"."+String.valueOf((int)(10+Math.random()*89))+"kg";
			mEdttMessage.setText(messagesend);
			if (null == messagesend || messagesend.length() == 0) {
				return;
			}
			mBlthChatUtil.write(messagesend.getBytes());
			break;
		default:
			break;
		}
	}

}
