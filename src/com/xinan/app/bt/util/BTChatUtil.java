package com.xinan.app.bt.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 该类的工作:建立和管理蓝牙连接。 共有三个线程。mAcceptThread线程用来监听socket连接（服务端使用）.
 * mConnectThread线程用来连接serversocket（客户端使用）。
 * mConnectedThread线程用来处理socket发送、接收数据。（客户端和服务端共用）
 */
public class BTChatUtil {
	private static final String TAG = "BluetoothChatService";
	private static final boolean D = true;

	// server SDP
	private static final String SERVICE_NAME = "BluetoothChat";
	public static final String BLUETOOTH_NAME = "HC-06";
	public static final String BLUETOOTH_ADDRESS = "20:17:08:14:93:15";
	// uuid SDP
	private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// BT adapter
	private final BluetoothAdapter mAdapter;
	private Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	private static BTChatUtil mBluetoothChatUtil;
	private BluetoothDevice mConnectedBluetoothDevice;
	// 常数，指示当前的连接状态
	public static final int STATE_NONE = 0; // 当前没有可用的连接
	public static final int STATE_LISTEN = 1; // 现在侦听传入的连接
	public static final int STATE_CONNECTING = 2; // 现在开始连接
	public static final int STATE_CONNECTED = 3; // 现在连接到远程设备
	public static final int STATAE_CONNECT_FAILURE = 4; // 连接失败

	public static final int MESSAGE_DISCONNECTED = 5;
	public static final int STATE_CHANGE = 6;
	public static final String DEVICE_NAME = "device_name";
	public static final int MESSAGE_READ = 7;
	public static final int MESSAGE_WRITE = 8;
	public static final String READ_MSG = "read_msg";

	public static final String DATA_HEAD_S = "ST,GS,+";
	public static final String DATA_HEAD_U = "US,GS,+";
	public static final String DATA_END = "kg";

	/**
	 * 构造函数。准备一个新的bluetoothchat会话。
	 * 
	 * @param context
	 */
	private BTChatUtil(Context context) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
	}

	public static BTChatUtil getInstance(Context c) {
		if (null == mBluetoothChatUtil) {
			mBluetoothChatUtil = new BTChatUtil(c);
		}
		return mBluetoothChatUtil;
	}

	public void registerHandler(Handler handler) {
		mHandler = handler;
	}

	public void unregisterHandler() {
		mHandler = null;
	}

	/**
	 * 设置当前状态的聊天连接
	 * 
	 * @param state
	 *            整数定义当前连接状态
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		mHandler.obtainMessage(STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * 返回当前的连接状态。
	 */
	public synchronized int getState() {
		return mState;
	}

	public BluetoothDevice getConnectedDevice() {
		return mConnectedBluetoothDevice;
	}

	/**
	 * 开始聊天服务。特别acceptthread开始 开始服务器模式。
	 */
	public synchronized void startListen() {
		if (D)
			Log.d(TAG, "start");
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		setState(STATE_LISTEN);
	}

	/**
	 * 开始connectthread启动连接到远程设备。
	 * 
	 * @param device
	 *            连接的蓝牙设备
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connect to: " + device);
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * 开始ConnectedThread开始管理一个蓝牙连接,传输、接收数据.
	 * 
	 * @param socket
	 *            socket连接
	 * @param device
	 *            已连接的蓝牙设备
	 */
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
		mConnectedBluetoothDevice = device;
		Message msg = mHandler.obtainMessage(STATE_CONNECTED);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		setState(STATE_CONNECTED);
	}

	/**
	 * 停止所有的线程
	 */
	public synchronized void disconnect() {
		if (D)
			Log.d(TAG, "disconnect");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		ConnectedThread r;
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		Message msg = mHandler.obtainMessage(STATAE_CONNECT_FAILURE);
		mHandler.sendMessage(msg);
		mConnectedBluetoothDevice = null;
		setState(STATE_NONE);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		Message msg = mHandler.obtainMessage(MESSAGE_DISCONNECTED);
		mHandler.sendMessage(msg);
		mConnectedBluetoothDevice = null;
		setState(STATE_NONE);
	}

	/**
	 * 本线程 侦听传入的连接。 它运行直到连接被接受（或取消）。
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID);
			} catch (IOException e) {
				Log.e(TAG, "listen() failed", e);
			}
			mServerSocket = tmp;
		}

		public void run() {
			if (D) {
				Log.d(TAG, "BEGIN mAcceptThread" + this);
			}
			setName("AcceptThread");
			BluetoothSocket socket = null;
			while (mState != STATE_CONNECTED) {
				try {
					socket = mServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "accept() failed", e);
					break;
				}
				if (socket != null) {
					synchronized (BTChatUtil.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread");
		}

		public void cancel() {
			if (D)
				Log.d(TAG, "cancel " + this);
			try {
				mServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of server failed", e);
			}
		}
	}

	/**
	 * 本线程用来连接设备
	 *
	 */
	private class ConnectThread extends Thread {
		private BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			try {
				mmSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
				mmSocket = null;
			}
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			try {
				mmSocket.connect();
			} catch (IOException e) {
				connectionFailed();
				try {
					mmSocket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				return;
			}
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	public static String bytesToHexString(byte[] bytes) {
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			String hexString = Integer.toHexString(bytes[i] & 0xFF);
			if (hexString.length() == 1) {
				hexString = '0' + hexString;
			}
			result += hexString.toUpperCase();
		}
		return result;
	}

	/*
	 * 16进制字符串转字符串
	 */
	public static String hex2String(String hex) throws Exception {
		String r = bytes2String(hexString2Bytes(hex));
		return r;
	}

	/*
	 * 字符转换为字节
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/*
	 * 字节数组转字符串
	 */
	public static String bytes2String(byte[] b) throws Exception {
		String r = new String(b, "UTF-8");
		return r;
	}

	/*
	 * 16进制字符串转字节数组
	 */
	public static byte[] hexString2Bytes(String hex) {

		if ((hex == null) || (hex.equals(""))) {
			return null;
		} else if (hex.length() % 2 != 0) {
			return null;
		} else {
			hex = hex.toUpperCase();
			int len = hex.length() / 2;
			byte[] b = new byte[len];
			char[] hc = hex.toCharArray();
			for (int i = 0; i < len; i++) {
				int p = 2 * i;
				b[i] = (byte) (charToByte(hc[p]) << 4 | charToByte(hc[p + 1]));
			}
			return b;
		}

	}

	public static int hexToInt(byte b) throws Exception {
		if (b >= '0' && b <= '9') {
			return (int) b - '0';
		}
		if (b >= 'a' && b <= 'f') {
			return (int) b + 10 - 'a';
		}
		if (b >= 'A' && b <= 'F') {
			return (int) b + 10 - 'A';
		}
		throw new Exception("invalid hex");
	}

	public static byte[] decodeToBytes(String hexString) {
		byte[] hex = hexString.getBytes();
		if ((hex.length % 2) != 0) {
			return null;
		}
		byte[] ret = new byte[hex.length / 2];
		int j = 0;
		int i = 0;
		try {
			while (i < hex.length) {
				byte hi = hex[i++];
				byte lo = hex[i++];
				ret[j++] = (byte) ((hexToInt(hi) << 4) | hexToInt(lo));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	public static String decodeToString(String hexString) {
		return new String(decodeToBytes(hexString));
	}

	/**
	 * 本线程server 和client共用. 它处理所有传入和传出的数据。
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private StringBuffer sb;
		private String currentsb = "";

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.d(TAG, String.valueOf(e));
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			sb = new StringBuffer();
		}

		public void run() {
			while (true) {
				try {
					byte[] buffer = new byte[64];
					int bytes = mmInStream.read(buffer);
					String result_bt = new String(buffer, 0, bytes);
					Log.i(TAG + "init data from bt server", result_bt);
					if (sb.toString().equals("") && (result_bt.startsWith("S") || result_bt.startsWith("U"))) {
						sb.append(result_bt);
						Log.i(TAG + "filter data from bt server1", sb.toString());
					} else if (!sb.toString().equals("")
							&& (sb.toString().startsWith("S") || sb.toString().startsWith("U"))
							&& !sb.toString().endsWith("g")) {
						sb.append(result_bt);
						Log.i(TAG + "filter data from bt server2", sb.toString());
					} else if ((sb.toString().startsWith(DATA_HEAD_S) || sb.toString().startsWith(DATA_HEAD_U))
							&& sb.toString().endsWith(DATA_END)) {
						Log.i(TAG + "filter data from bt server3", sb.toString());
						Message msg = mHandler.obtainMessage(MESSAGE_READ);
						Bundle bundle = new Bundle();
						bundle.putString(READ_MSG, sb.toString().substring(7, 14).trim());
						msg.setData(bundle);
						mHandler.sendMessage(msg);
						sb = new StringBuffer();
					}
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}

		}

		/**
		 * 向外发送。
		 * 
		 * @param buffer
		 *            data to be send
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
				mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
