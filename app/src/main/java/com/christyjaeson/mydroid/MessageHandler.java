package com.christyjaeson.mydroid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

public class MessageHandler extends BroadcastReceiver {

	Context mContext;
	String mText;
	String phoneNumber;
	private boolean mProfile;
	private boolean mClipboard;
	private boolean mMessage;
	private boolean mLog;
	private boolean mLocation;
	private boolean mContact;
	private boolean mLock;
	private String mPwd;
	private boolean testApp;
	Intent mIntent;
	private String message;
	public MessageHandler(Context context){
		mContext = context;
	}
	public MessageHandler(){}
	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		mIntent = intent;
		// String requestCmd = intent.getStringExtra("request");

		// Parse message
		Log.i("MessageHandler:", "Recieve BroadCast");
		if (isAllowedAccessAny()) {
			parseMessage(intent);
		}
		return;
		//
		// if(requestCmd.equals("Contacts")){
		// sendContacts();
		// }else if(requestCmd.equals("Logs")){
		// sendLogs();
		// }else if(requestCmd.equals("Messages")){
		// sendMessages();
		// }else if(requestCmd.equals("Clipboard")){
		// sendClipboard();
		// }

	}

	private boolean isAllowedAccessAny() {
		SharedPreferences sp = mContext.getSharedPreferences(
				"com.example.getdata.settings", Context.MODE_PRIVATE);
		mContact = sp.getBoolean("contacts", false);
		mLog = sp.getBoolean("logs", false);
		mLocation = sp.getBoolean("location", false);
		mMessage = sp.getBoolean("messages", false);
		mClipboard = sp.getBoolean("clipboard", false);
		mProfile = sp.getBoolean("profile", false);
		mLock = sp.getBoolean("lock",true);
		mPwd = sp.getString("password", "pwd");
		Log.i("MessageHandler", "Password is :: " + mPwd);
		if ((  !mLock ) &&(mContact || mLog ||  mMessage || mClipboard || mProfile || mLocation)) {
			return true;
		}
		return false;
	}

	public boolean parseMessage(Intent intent) {
		Bundle bundle = intent.getExtras();
		Log.i("CJ", "parseMsg()");
		String text;
		try {
			if (bundle != null) {
				if (intent.getAction().equals(
						"com.example.getpersonaldata.ACTION_SEND")) {
					phoneNumber = bundle.getString("number");
					text = bundle.getString("body");
					Log.i("Cool:", "testApp = " + testApp);
					testApp = true;
				} else {
					Object[] pdusObj = (Object[]) bundle.get("pdus");
					SmsMessage currentMessage = SmsMessage
							.createFromPdu((byte[]) pdusObj[0]);
					phoneNumber = currentMessage.getOriginatingAddress();
					text = currentMessage.getDisplayMessageBody();
					testApp = false;
				}
				// for(int i = 0;i<pdusObj.length;i++){

				Log.i("MessageHandler:", text + "," + text.endsWith(mPwd));
				if (!text.endsWith(mPwd))
					return false;
				else {
					text = text.substring(0, text.length() - mPwd.length() - 1);
				}
				/*if (text.startsWith("Get")) {*/
					mText = text;//.substring(4, text.length());
					Log.i("MessageHandler:", mText);
					if (mText.startsWith("Contacts") && mContact) {
						sendContacts();
						return true;
					} else if (mText.startsWith("Logs") && mLog) {
						sendLogs();
						return true;
					}  else if (mText.startsWith("Clipboard") && mClipboard ) {
						sendClipboard();
						return true;
					} else if (mText.startsWith("Messages") && mMessage) {
						sendMessages();
						return true;
					} else if (mText.startsWith("Notifications")) {
						sendNotifications();
						return true;
					}else if (mText.startsWith("Location") && mLocation){
						sendLocation();
						return true;
					}else if (mText.startsWith("Lock")){
						setLock();
						return true;
					}else if (mText.startsWith("Beep")){
						beep();
						return true;
					}
					//return true;
				//} else
			if (text.startsWith("Change") && mProfile) {
					mText = text.substring(7, text.length());
					Log.i("MessageHandler:", mText + ", mProfile = " + mProfile);
					if (mText.startsWith("Sound")) {
						changeSoundProfile();
						return true;
					}
					return false;
				}
			}
			return false;
		} catch (Exception e) {
			Log.i("MessageHandler:", "Error in parsing message");
			e.printStackTrace();
			return false;
		}
	}

	public void changeSoundProfile() {
		mText = mText.substring(6);
		AudioManager manager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		if (mText.equals("Silent")) {
			manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

		} else if (mText.equals("Vibrate")) {
			manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		} else if (mText.equals("Normal")) {
			manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			int maxVolume = manager
					.getStreamMaxVolume(AudioManager.STREAM_RING);
			manager.setStreamVolume(AudioManager.STREAM_RING, maxVolume,
					AudioManager.FLAG_ALLOW_RINGER_MODES
							| AudioManager.FLAG_PLAY_SOUND);
		}
		if (testApp) {
			message = "Sound Profile is Changed";
			return;
		}

	}

	private void sendClipboard() {
		Log.i("MessageHandler:", "Clipboard");
		SmsManager manager = SmsManager.getDefault();
		ClipboardManager cManager = (ClipboardManager) mContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
		if (cManager.hasPrimaryClip()) {
			ClipData data = cManager.getPrimaryClip();
			if (data.getItemCount() > 0) {
				Log.i("MessageHandler:", data.getItemCount() + ","
						+ data.getItemAt(0).getText().toString());
				if (testApp) {
					message = data.getItemAt(0).getText().toString();
					return;
				}
				manager.sendTextMessage(phoneNumber, null, data.getItemAt(0)
						.getText().toString(), null, null);
			} else {
				if (testApp) {
					message = "No Clipoard Data";
					return;
				}
			}
		}

	}

	public void sendLocation(){
		Log.i("MessageHandler:", "Location : " + mText);
		String location = "Your phone is at:\n"
				+ "http://maps.google.com/maps?q=";

		LocationManager locationManager = (LocationManager)
				mContext.getSystemService(Context.LOCATION_SERVICE);
		Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(loc!=null) {
			location = location + loc.getLatitude() + "," + loc.getLongitude();
		}
		SmsManager manager = SmsManager.getDefault();
		SharedPreferences sp = mContext.getSharedPreferences("com.example.getdata.location", Context.MODE_PRIVATE);
		/*String notification =sp.getString("text", null);*/

		manager.sendTextMessage(phoneNumber, null, location, null, null);

	}
	public void setLock(){
		SharedPreferences sp = mContext.getSharedPreferences("com.example.getdata.settings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("contacts", false);
		editor.putBoolean("logs", false);
		editor.putBoolean("location", false);
		editor.putBoolean("messages", false);
		editor.putBoolean("clipboard", false);
		editor.putBoolean("profile", false);
		editor.putBoolean("beep", false);
		editor.putBoolean("lock", true);
		editor.putBoolean("selectAll", false);
		editor.commit();
	}

	public void beep(){
		/*Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		MediaPlayer mMediaPlayer = new MediaPlayer();
		SharedPreferences sp = mContext.getSharedPreferences("com.example.getdata.settings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=sp.edit();
		editor.putBoolean("beepStatus", true);
		editor.commit();

		try {
			mMediaPlayer.setDataSource(mContext, soundUri);
			final AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(sp.getBoolean("beepStatus", true));
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}
		}catch(Exception e){

		}*/

		/*SharedPreferences sp = mContext.getSharedPreferences("com.example.getdata.settings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=sp.edit();
		editor.putBoolean("beepStatus", true);
		editor.commit();
		MediaPlayer mp = MediaPlayer.create(mContext, R.raw.bark);
		mp.setLooping(true);
		mp.start();

		for(;;){
			if(sp.getBoolean("beepStatus", true))
				continue;
			else
				mp.stop();
		}*/
		SharedPreferences sp = mContext.getSharedPreferences("com.example.getdata.settings", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor=sp.edit();
		editor.putBoolean("beepStatus", true);
		editor.commit();
		final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.bark);
		//mp.setVolume(SyncStateContract.Constants, 1.0f);
		mp.setLooping(true);
		mp.start();
		new Thread(new Runnable() {
			public void run() {
				SharedPreferences sp = mContext.getSharedPreferences("com.example.getdata.settings", Context.MODE_PRIVATE);
				for(;;){
					if(sp.getBoolean("beepStatus", true))
						continue;
					else
						mp.stop();
				}
			}
		}).start();


	}

	private void sendNotifications() {
		Log.i("MessageHandler:", "Clipboard");
		SmsManager manager = SmsManager.getDefault();
		SharedPreferences sp = mContext.getSharedPreferences(
				"com.example.getdata.notification", Context.MODE_PRIVATE);
		String notification = sp.getString("text", null);

		manager.sendTextMessage(phoneNumber, null, notification, null, null);

	}

	private void sendMessages() {
		Log.i("MessageHandler:", "Messages : " + mText);
		int count;
		String text = "";
		ContentResolver resolver = mContext.getContentResolver();
		// String[] projection =
		// {Phone.DISPLAY_NAME,Phone.NUMBER,Phone.HAS_PHONE_NUMBER};
		// String selection = Phone.DISPLAY_NAME + " LIKE ?";
		// String selectionArgs[] = {"%"+mText+"%"};
		Cursor c;
		mText = mText.substring(9);
		Log.i("MessageHandler:", "Messages : " + mText);
		if (mText.startsWith("Unread")) {
			mText = mText.substring(7);
			Log.i("MessageHandler:", "Messages : " + mText);
			try {
				count = Integer.parseInt(mText);
				Uri sms_uri = Uri.parse("content://sms");
				Uri inbox_uri = Uri.withAppendedPath(sms_uri, "inbox");
				c = resolver.query(inbox_uri, null, "read=0", null, null);
				if (c.getCount() == 0)
					text = "No Unread Message";
				Log.i("MessageHandler : ", "" + c.getCount());
				c.moveToFirst();
				int max = c.getCount();
				for (int i = 0; i < count && i < max; i++) {
					int index = c.getColumnIndex("address");
					String address = c.getString(index);
					index = c.getColumnIndex("body");
					String body = c.getString(index);
					text = text + address + "\n" + body + "\n\n";
					c.moveToNext();
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			} catch (Exception e) {
				Log.i("MessageHandler::", e.toString());
				text = "No Unread Message";
			}
		} else if (mText.startsWith("Last")) {
			mText = mText.substring(5);
			Log.i("MessageHandler:", "Messages : " + mText);
			try {
				count = Integer.parseInt(mText);
				Uri sms_uri = Uri.parse("content://sms");
				Uri inbox_uri = Uri.withAppendedPath(sms_uri, "inbox");
				c = resolver.query(inbox_uri, null, null, null, null);
				Log.i("MessageHandler : ", "" + c.getCount());
				if (c.getCount() == 0)
					text = "No Message in Inbox";
				c.moveToFirst();
				int max = c.getCount();
				for (int i = 0; i < count && i < max; i++) {
					int index = c.getColumnIndex("address");
					String address = c.getString(index);
					index = c.getColumnIndex("body");
					String body = c.getString(index);
					text = text + address + "\n" + body + "\n\n";
					c.moveToNext();
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			} catch (Exception e) {
				Log.i("MessageHandler::", e.toString());
				text = "No Message in Inbox";
			}
		} else if (mText.startsWith("Number")) {
			mText = mText.substring(7);
			Log.i("MessageHandler:", "Messages : " + mText);
			try {
				count = Integer.parseInt(mText);
				Uri sms_uri = Uri.parse("content://sms");
				Uri inbox_uri = Uri.withAppendedPath(sms_uri, "inbox");
				c = resolver.query(inbox_uri, null, null, null, null);
				if (c.getCount() == 0)
					text = "No Message in Inbox";
				Log.i("MessageHandler : ", "" + c.getCount());
				c.moveToFirst();
				int max = c.getCount();
				for (int i = 0; i < count && i < max; i++) {
					int index = c.getColumnIndex("address");
					String address = c.getString(index);

					text = text + address + "\n";
					c.moveToNext();
				}

			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			} catch (Exception e) {
				Log.i("MessageHandler::", e.toString());
				text = "No Message in Inbox";
			}
		} else if (mText.startsWith("From")) {
			mText = mText.substring(5);
			Log.i("MessageHandler:", "Messages : " + mText);
			try {

				Uri sms_uri = Uri.parse("content://sms");
				Uri inbox_uri = Uri.withAppendedPath(sms_uri, "inbox");
				c = resolver.query(inbox_uri, null, "address LIKE " + mText,
						null, null);
				if (c.getCount() == 0)
					text = "No message from this address";
				Log.i("MessageHandler : ", "" + c.getCount());
				c.moveToFirst();
				int max = c.getCount();
				for (int i = 0; i < 5 && i < max; i++) {
					int index = c.getColumnIndex("address");
					String address = c.getString(index);
					index = c.getColumnIndex("body");
					String body = c.getString(index);
					text = text + address + "\n" + body + "\n\n";
					c.moveToNext();
				}

			} catch (Exception e) {
				Log.i("MessageHandler::", e.toString());
				text = "No message from this address";
			}
		}
		Log.i("MessageHandler::", text);
		if (testApp) {
			message = text;
			Log.i("Cool:", message);
			return;
		}
		int len_send = 0;
		while (len_send != text.length()) {
			String pending_text = "";
			if (text.length() > 140) {
				pending_text = text.substring(140, text.length());
				text = text.substring(0, 140);
			}
			SmsManager manager = SmsManager.getDefault();
			manager.sendTextMessage(phoneNumber, null, text, null, null);
			len_send += text.length();
			text = pending_text;
		}
	}

	private void sendLogs() {
		Log.i("MessageHandler:", "Logs");
		int i = 0;
		int count;
		mText = mText.substring(5, mText.length());
		try {
			count = Integer.parseInt(mText);
		} catch (NumberFormatException e) {
			return;
		}
		String text = new String("");
		ArrayList<String> list = new ArrayList<String>();
		SmsManager manager = SmsManager.getDefault();
		ContentResolver contentResolver = mContext.getContentResolver();
		String[] projection = { CallLog.Calls.NUMBER,
				CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE };
		Cursor c = contentResolver.query(CallLog.Calls.CONTENT_URI, projection,
				null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				String num = c
						.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
				String name = c.getString(c
						.getColumnIndex(CallLog.Calls.CACHED_NAME));
				String type = c.getString(c.getColumnIndex(CallLog.Calls.TYPE));
				if (list.size() == 0 || !list.contains(num)) {
					list.add(num);
					i++;
					if (name == null)
						name = "No Name";
					text = text + name + "," + num + "\n";
					Log.i("MessageHandler:", num + "," + name + "," + type);
				}
			} while (c.moveToNext() && i < count);
		}
		Log.i("MessageHandler:", phoneNumber + "," + text);
		if (testApp) {
			message = text;
			return;
		}
		int len_send = 0;
		while (len_send != text.length()) {
			String pending_text = "";
			if (text.length() > 140) {
				pending_text = text.substring(140, text.length());
				text = text.substring(0, 140);
			}
			manager.sendTextMessage(phoneNumber, null, text, null, null);
			len_send += text.length();
			text = pending_text;
		}
	}

	public void sendContacts() {
		Log.i("MessageHandler:", "Contacts");
		mText = mText.substring(9, mText.length());
		String text = new String("");
		int i = 0;
		SmsManager manager = SmsManager.getDefault();
		ContentResolver contentResolver = mContext.getContentResolver();
		String[] projection = { Phone.DISPLAY_NAME, Phone.NUMBER,
				Phone.HAS_PHONE_NUMBER };
		String selection = Phone.DISPLAY_NAME + " LIKE ?";
		String selectionArgs[] = { "" + mText + "%" };
		Cursor c = contentResolver.query(Phone.CONTENT_URI, projection,
				selection, selectionArgs, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			do {
				String name = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
				String num = c.getString(c.getColumnIndex(Phone.NUMBER));
				int nCount = Integer.parseInt(c.getString(c
						.getColumnIndex(Phone.HAS_PHONE_NUMBER)));
				if (nCount > 0) {
					Log.i("MessageHandler:", name + "," + num);
					i++;
					text = text + name + "," + num + "\n";
				}
			} while (c.moveToNext() && i < 10);
		}
		if (testApp) {
			message = text;
			return;
		}
		int len_send = 0;
		while (len_send != text.length()) {
			String pending_text = "";
			if (text.length() > 140) {
				pending_text = text.substring(140, text.length());
				text = text.substring(0, 140);
			}
			manager.sendTextMessage(phoneNumber, null, text, null, null);
			len_send += text.length();
			text = pending_text;
		}
	}

	void sendNotification(String msg, Intent intent) {

		NotificationCompat.Builder notification = new NotificationCompat.Builder(
				mContext)
				.setSmallIcon(android.R.drawable.stat_notify_chat)
				.setContentTitle("Message")
				.setContentText(msg)
				.setAutoCancel(true)
				.setProgress(100, 1, false)
				.setContentIntent(
						PendingIntent.getActivity(mContext, 0, intent, 0));

		NotificationManager nm = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify("message", R.id.interstitial_notification,
				notification.getNotification());

	}

}
