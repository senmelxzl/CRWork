package com.crwork.app.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Helper class to create plugin instance.
 */
public class PermissionHelper {

	public interface PermissionCallback {
		void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
	};

	private static final int REQEUST_MULITIPLE_PERMISSIONS = 1000;
	private static final String TAG = "PermissionHelper";
	private static PermissionHelper sInstance;
	private List<PermissionCallback> mListeners = new ArrayList<PermissionCallback>();

	private static final String[] ALL_PERMISSIONS = { Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
			Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS };
	// / @ }	

	private Set<String> requestingPermissions = new HashSet<String>();

	private Activity mActivity;

	public static PermissionHelper getInstance() {
		return sInstance;
	}

	public static void init(Activity activity) {
		sInstance = new PermissionHelper(activity);
	}

	private PermissionHelper(Activity activity) {
		mActivity = activity;
	}

	public void addListener(PermissionCallback listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public void removeListener(PermissionCallback listener) {
		mListeners.remove(listener);
	}

	@SuppressLint("NewApi")
	public void requestPermissions(List<String> permissionsRequestList, PermissionCallback listener) {

		Log.d(TAG, " requestBrowserPermission start ......! " + permissionsRequestList.toString());
		if (permissionsRequestList.size() > 0) {
			addListener(listener);
			synchronized (requestingPermissions) {
				// requestingPermissions means there is no permissions is
				// requesting now.
				if (requestingPermissions.size() == 0) {
					mActivity.requestPermissions(
							permissionsRequestList.toArray(new String[permissionsRequestList.size()]),
							REQEUST_MULITIPLE_PERMISSIONS);
				}
				requestingPermissions.addAll(permissionsRequestList);
			}
		}
	}

	public List<String> getAllUngrantedPermissions() {
		List<String> permissionsRequestList = new ArrayList<String>();
		// Check whether the primission need to be requested
		for (int i = 0; i < ALL_PERMISSIONS.length; i++) {
			if (!checkPermission(ALL_PERMISSIONS[i])) {
				permissionsRequestList.add(ALL_PERMISSIONS[i]);
			}
		}
		return permissionsRequestList;
	}

	public List<String> getUngrantedPermissions(String[] permissions) {
		List<String> permissionsRequestList = new ArrayList<String>();
		// Check whether the primission need to be requested
		for (int i = 0; i < permissions.length; i++) {
			if (!checkPermission(permissions[i])) {
				permissionsRequestList.add(permissions[i]);
			}
		}
		return permissionsRequestList;
	}

	@SuppressLint("NewApi")
	public boolean checkPermission(String permission) {
		boolean granted = false;
		if (mActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
			granted = true;
		}
		return granted;
	}

	@SuppressLint("NewApi")
	public void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		Log.d(TAG, " onPermissionsResult .. " + requestCode);
		synchronized (requestingPermissions) {
			for (int i = 0; i < mListeners.size(); i++) {
				mListeners.get(i).onPermissionsResult(requestCode, permissions, grantResults);
				for (String p : permissions) {
					requestingPermissions.remove(p);
				}
			}
			Log.d(TAG, " onPermissionsResult .. requestingPermissions.size() = " + requestingPermissions.size());
			if (requestingPermissions.size() == 0) {
				mListeners.clear();
			} else {
				Log.d(TAG, " onPermissionsResult re-request ");
				mActivity.requestPermissions(requestingPermissions.toArray(new String[requestingPermissions.size()]),
						REQEUST_MULITIPLE_PERMISSIONS);
			}
		}
	}

}
