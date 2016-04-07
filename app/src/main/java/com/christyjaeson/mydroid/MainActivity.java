package com.christyjaeson.mydroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	CheckBox checkContacts;
	CheckBox checkLogs;
	CheckBox checkLocation;
	CheckBox checkMessages;
	CheckBox checkClipoard;
	CheckBox checkProfile;
	CheckBox checkBeep;
	CheckBox checkLock;
	CheckBox checkAll;
	enum HelpStep{
		START,
		CONTACTS,
		LOGS,
		LOCATION,
		MESSAGES,
		CLIPBOARD,
		SOUND,
		BEEP,
		LOCK,
		END
	};
	HelpStep helpStep;
	Dialog d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkContacts = (CheckBox)findViewById(R.id.check_contact);
        checkContacts.setOnCheckedChangeListener(listener);
        checkLogs = (CheckBox)findViewById(R.id.check_log);
        checkLogs.setOnCheckedChangeListener(listener);
		checkLocation = (CheckBox)findViewById(R.id.check_location);
		checkLocation.setOnCheckedChangeListener(listener);
        checkMessages = (CheckBox)findViewById(R.id.check_msg);
        checkMessages.setOnCheckedChangeListener(listener);
        checkClipoard = (CheckBox)findViewById(R.id.check_clip);
        checkClipoard.setOnCheckedChangeListener(listener);
        checkProfile = (CheckBox)findViewById(R.id.check_profile);
        checkProfile.setOnCheckedChangeListener(listener);
		checkBeep = (CheckBox)findViewById((R.id.check_beep));
		checkBeep.setOnCheckedChangeListener(listener);
		checkLock = (CheckBox)findViewById((R.id.check_lock));
		checkLock.setOnCheckedChangeListener(listener);
        checkAll = (CheckBox)findViewById(R.id.check_all);
        checkAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				toggleSelectAll();
			}
		});
        setCheckState();
        
        if(savedInstanceState!=null){
        	boolean isPopup = savedInstanceState.getBoolean("dialog");
        	if(isPopup){
        		boolean ishelppopup = savedInstanceState.getBoolean("help");
        		if(ishelppopup){
        			String state = savedInstanceState.getString("HelpStep");
        			helpStep = HelpStep.valueOf(state);
        			showHelpPopup();
        		}else{
        			savePasswordPopup();
        		}
        	}
        }
    }
    
    OnCheckedChangeListener listener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if(!arg1)
				checkAll.setChecked(false);
			else{
				if(allChecked())
					checkAll.setChecked(true);
			}
		}
	};
	private boolean isHelpPopup;

	public boolean uncheckAll(){
		checkAll.setChecked(false);
		checkClipoard.setChecked(false);
		checkContacts.setChecked(false);
		checkLogs.setChecked(false);
		checkMessages.setChecked(false);
		checkProfile.setChecked(false);
		checkBeep.setChecked(false);
		if(checkContacts.isChecked()||checkLogs.isChecked()||checkMessages.isChecked()||
				checkProfile.isChecked()||checkClipoard.isChecked()||checkBeep.isChecked())
			return false;
		return true;
	}

	public boolean allChecked(){
		if(checkContacts.isChecked()&&checkLogs.isChecked()&&checkLocation.isChecked()&&checkMessages.isChecked()&&
		checkProfile.isChecked()&&checkClipoard.isChecked()&&checkBeep.isChecked())
			return true;
		return false;
	}
    public void setCheckState() {
    	SharedPreferences sp = getSharedPreferences("com.example.getdata.settings", MODE_PRIVATE);
		checkContacts.setChecked(sp.getBoolean("contacts", false));
		checkLogs.setChecked(sp.getBoolean("logs", false));
		checkLocation.setChecked(sp.getBoolean("location",false));
		checkMessages.setChecked(sp.getBoolean("messages", false));
		checkClipoard.setChecked(sp.getBoolean("clipboard", false));
		checkProfile.setChecked(sp.getBoolean("profile", false));
		checkBeep.setChecked(sp.getBoolean("beep", false));
		checkLock.setChecked(sp.getBoolean("lock", true));
		checkAll.setChecked(sp.getBoolean("selectAll", false));
		
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
            return true;
    }
    
    
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()){
    	case R.id.help: helpStep = HelpStep.START; showHelpPopup(); break;
    	case R.id.save_password: savePasswordPopup();break;
    	default:
    		return false;
    	}
    	return true;
	}

	private void savePasswordPopup() {
		if(d!=null)
			d.dismiss();
		d = new Dialog(this);
		d.setContentView(R.layout.password_popup);
		d.setTitle(R.string.change_password);
		Button cancel = (Button)d.findViewById(R.id.button1);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				d.dismiss();
				d = null;
			}
		});
		
		Button save = (Button)d.findViewById(R.id.button2);
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				EditText pwd = (EditText)d.findViewById(R.id.editText2);
				EditText re_pwd = (EditText)d.findViewById(R.id.editText1);
				if(pwd.getText().toString().equals(re_pwd.getText().toString())){
					savePassword(pwd.getText().toString());
				}else{
					Toast t = Toast.makeText(getApplicationContext(), "Password MisMatch!!!", Toast.LENGTH_LONG);
					t.show();
				}
			}

			private void savePassword(String string) {
				SharedPreferences sp = getSharedPreferences("com.example.getdata.settings", MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.putString("password", string);
				editor.commit();
				d.dismiss();
				d = null;
			}
		});
		
		isHelpPopup = false;
		d.show();
	}
	private void showHelpPopup() {
		if(d!=null){
			d.dismiss();
			d = null;
		}
		d = new Dialog(this);
		d.setContentView(R.layout.popup);
		onHelpButtonClick();
		if(d == null)
			return;
		Button b = (Button)d.findViewById(R.id.button1);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setNextHelpStep();
				onHelpButtonClick();
			}

			private void setNextHelpStep() {
				switch(helpStep){
				case START:
					helpStep = HelpStep.CONTACTS;
					break;
				case CONTACTS:
					helpStep = HelpStep.LOGS;
					break;
				case LOGS:
					helpStep = HelpStep.LOCATION;
					break;
				case LOCATION:
					helpStep = HelpStep.MESSAGES;
					break;
				case MESSAGES:
					helpStep = HelpStep.CLIPBOARD;
					break;
				case CLIPBOARD:
					helpStep = HelpStep.SOUND;
					break;
				case SOUND:
					helpStep = HelpStep.END;
					break;
				case LOCK:
					helpStep = HelpStep.LOCK;
					break;
				default:
					helpStep = HelpStep.END;
					break;
				}

			}
		});
		isHelpPopup = true;
		d.show();
	}
	
	public void onHelpButtonClick(){
		TextView textView = (TextView)d.findViewById(R.id.textView1);
		if(helpStep == null)
			helpStep = HelpStep.START;
		
		if(helpStep == HelpStep.END){
			d.dismiss();
			isHelpPopup = false;
			d = null;
		}else{
			isHelpPopup = true;
			d.setTitle(getHelpTitle());
			textView.setText(getHelpText());
		}
		
				
		if(helpStep == HelpStep.SOUND){
			Button btn = (Button)d.findViewById(R.id.button1);
			btn.setText("Done");
		}
		
	}

	public String getHelpTitle(){
		switch(helpStep){
		case START:
			return "Help";
		case CONTACTS:
			return "Help Contacts";
		case LOGS:
			return "Help Logs";
		case LOCATION:
			return "Help Location";
		case MESSAGES:
			return "Help Messages";
		case CLIPBOARD:
			return "Help Clipboard";			
		case SOUND:
			return "Help Sound Profile";
		case LOCK:
			return "Help Lock";
		default:
			return null;
		}

	}
	
	public String getHelpText(){
		switch(helpStep){
		case START:
			return (getString(R.string.help_popup_text_page1));
		case CONTACTS:
			return (getString(R.string.help_popup_text_page2)+"\n"+getString(R.string.help_popup_text_page2_sms));			
		case LOGS:
			return (getString(R.string.help_popup_text_page3)+"\n"+getString(R.string.help_popup_text_page3_sms));
		case LOCATION:
			return (getString(R.string.help_popup_text_page7)+"\n"+getString(R.string.help_popup_text_page7_sms));
		case MESSAGES:
			return (getString(R.string.help_popup_text_page6)+"\n"+
							 getString(R.string.help_popup_text_page6_sms1)+"\n"+
							 getString(R.string.help_popup_text_page6_sms2)+"\n"+
							 getString(R.string.help_popup_text_page6_sms3)+"\n");
		case CLIPBOARD:
			return (getString(R.string.help_popup_text_page4)+"\n"+getString(R.string.help_popup_text_page4_sms));
		case SOUND:
			return (getString(R.string.help_popup_text_page5)+"\n"+getString(R.string.help_popup_text_page5_sms));
		case LOCK:
			return (getString(R.string.help_popup_text_page8)+"\n"+getString(R.string.help_popup_text_page8_sms));
			//case END:
				//break;
			default:
			return null;
		}

	}
	
	private void toggleSelectAll() {
		boolean state = checkAll.isChecked();
		checkClipoard.setChecked(state);
		checkContacts.setChecked(state);
		checkMessages.setChecked(state);
		checkLogs.setChecked(state);
		checkLocation.setChecked(state);
		checkProfile.setChecked(state);
		checkBeep.setChecked(state);

		//checkLock.setChecked(state);
	}

	public void sendBroadcastRequest(String message){
    	Intent intent = new Intent("com.example.getpersonaldata.ACTION_SEND");
    	Log.i("MessageHandler:", "Send BroadCast");
    	String msg = message;//"Get Logs 5";
    	String num = "";
    	
    	Bundle bundle = new Bundle();
    	bundle.putString("body", msg);
    	bundle.putString("number", num);
    	
    	intent.putExtras(bundle);
    	
    	
    	sendBroadcast(intent);
    }
    
    public void sendMessage(View view){
    	sendBroadcastRequest(view.getContentDescription().toString());
    }
    
    public void saveSettings(View view) {
    	SharedPreferences sp = getSharedPreferences("com.example.getdata.settings", MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean("contacts", checkContacts.isChecked());
		editor.putBoolean("logs", checkLogs.isChecked());
		editor.putBoolean("location", checkLocation.isChecked());
		editor.putBoolean("messages", checkMessages.isChecked());
		editor.putBoolean("clipboard", checkClipoard.isChecked());
		editor.putBoolean("profile", checkProfile.isChecked());
		editor.putBoolean("beep", checkBeep.isChecked());
		editor.putBoolean("beepStatus", false);
		editor.putBoolean("lock", checkLock.isChecked());
		editor.putBoolean("selectAll", checkAll.isChecked());
		editor.commit();
		finish();
	}
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		if (d == null)
			outState.putBoolean("dialog", false);
		else {
			outState.putBoolean("dialog", d.isShowing());
			d.dismiss();
		}
		outState.putBoolean("help", isHelpPopup);
		if (helpStep != null) {
			outState.putString("HelpStep", helpStep.toString());
			Log.i("MessageHandler:", helpStep.toString());
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(checkLock.isChecked()){
			Toast.makeText(getApplicationContext(),"Your Profile is currently Locked",Toast.LENGTH_SHORT).show();
			Toast.makeText(getApplicationContext(),"Change Password & Profile settings before USE",Toast.LENGTH_SHORT).show();
		}
		if(true){
			SharedPreferences sp = getSharedPreferences("com.example.getdata.settings", MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putBoolean("beepStatus",false);
			editor.commit();
		}

	}
}
