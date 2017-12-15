package com.xinan.app.debug;

import java.util.Arrays;

import com.xinan.app.R;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * NFC debug activity
 * 
 * @author xiezhenlin
 *
 */
public class NFCActivity extends BaseNfcActivity {
	private TextView nfc_received_data;
	private String mTagText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.nfc);
		initview();
	}

	private void initview() {
		// TODO Auto-generated method stub
		nfc_received_data = (TextView) findViewById(R.id.nfc_data);
	}

	@Override
	public void onNewIntent(Intent intent) {
		// 1.��ȡTag����
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		// 2.��ȡNdef��ʵ��
		Ndef ndef = Ndef.get(detectedTag);
		if (ndef != null) {
			mTagText = ndef.getType() + "\nmaxsize:" + ndef.getMaxSize() + "bytes\n\n";
			readNfcTag(intent);
		} else {
			mTagText = "can not received data from card";
		}
		nfc_received_data.setText(mTagText);
	}

	/**
	 * ��ȡNFC��ǩ�ı�����
	 */
	private void readNfcTag(Intent intent) {
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage msgs[] = null;
			int contentSize = 0;
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
					contentSize += msgs[i].toByteArray().length;
				}
			}
			try {
				if (msgs != null) {
					NdefRecord record = msgs[0].getRecords()[0];
					String textRecord = parseTextRecord(record);
					mTagText += textRecord + "\n\ntext\n" + contentSize + " bytes";
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * ����NDEF�ı����ݣ��ӵ������ֽڿ�ʼ��������ı�����
	 * 
	 * @param ndefRecord
	 * @return
	 */
	public static String parseTextRecord(NdefRecord ndefRecord) {
		/**
		 * �ж������Ƿ�ΪNDEF��ʽ
		 */
		// �ж�TNF
		if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
			return null;
		}
		// �жϿɱ�ĳ��ȵ�����
		if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
			return null;
		}
		try {
			// ����ֽ����飬Ȼ����з���
			byte[] payload = ndefRecord.getPayload();
			// ���濪ʼNDEF�ı����ݵ�һ���ֽڣ�״̬�ֽ�
			// �ж��ı��ǻ���UTF-8����UTF-16�ģ�ȡ��һ���ֽ�"λ��"��16���Ƶ�80��16���Ƶ�80Ҳ�������λ��1��
			// ����λ����0�����Խ���"λ��"�����ͻᱣ�����λ
			String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
			// 3f�����λ��0������λ��1�����Խ���"λ��"������õ���λ
			int languageCodeLength = payload[0] & 0x3f;
			// ���濪ʼNDEF�ı����ݵڶ����ֽڣ����Ա���
			// ������Ա���
			String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
			// ���濪ʼNDEF�ı����ݺ�����ֽڣ��������ı�
			String textRecord = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1,
					textEncoding);
			return textRecord;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}

}
