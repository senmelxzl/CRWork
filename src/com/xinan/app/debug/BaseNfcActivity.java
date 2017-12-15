package com.xinan.app.debug;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class BaseNfcActivity extends Activity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    /**
     * ����Activity������ɼ�ʱ
     */
    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //һ���ػ�NFC��Ϣ���ͻ�ͨ��PendingIntent���ô���
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }
    /**
     * ��ý��㣬��ť���Ե��
     */
    @Override
    public void onResume() {
        super.onResume();
        //���ô���������������NFC�Ĵ���
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }
    /**
     * ��ͣActivity�������ȡ���㣬��ť���Ե��
     */
    @Override
    public void onPause() {
        super.onPause();
        //�ָ�Ĭ��״̬
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}