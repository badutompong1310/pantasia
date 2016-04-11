package com.ingenico.pcltestappwithlib;

import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.ingenico.pclservice.PclService;
import com.ingenico.pclutilities.PclUtilities;
import com.ingenico.pclutilities.PclUtilities.BluetoothCompanion;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomeActivity extends CommonActivity implements OnClickListener, OnCheckedChangeListener {

	private static final String TAG = "PCLTESTAPP";
	private RadioGroup mRadioGroup;
	private RadioButton[] mRadioButtons = null;
	private PclUtilities mPclUtil;
	private boolean mServiceStarted;

    TextView mtvState;
	TextView mtvSerialNumber;
	CharSequence mCurrentDevice;
	
	static class PclObject {
		PclServiceConnection serviceConnection;
		PclService service;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TextView tvAppVersion;
		TextView tvBuildDate;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		final CharSequence data = (CharSequence) getLastNonConfigurationInstance();
		mCurrentDevice = data;
		
		findViewById(R.id.button_unitary_test).setOnClickListener(this);
		findViewById(R.id.button_loop_test).setOnClickListener(this);
		findViewById(R.id.button_unitary_test).setEnabled(false);
		findViewById(R.id.button_loop_test).setEnabled(false);
		mRadioGroup=(RadioGroup)findViewById(R.id.optionRadioGroup);
		mtvState = (TextView)findViewById(R.id.tvState);
		mtvSerialNumber = (TextView)findViewById(R.id.tvSerialNumber);
		tvAppVersion = (TextView)findViewById(R.id.tvAppVersion);
		try {
			tvAppVersion.setText(getString(R.string.app_version) + getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		tvBuildDate = (TextView)findViewById(R.id.tvBuildDate);
		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            zf.close();
       
            String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(time);

			tvBuildDate.setText(getString(R.string.build_date) + date);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mPclUtil = new PclUtilities(this, "com.ingenico.pcltestappwithlib", "pairing_addr.txt");
		/*
		Set<BluetoothCompanion> btComps = mPclUtil.GetPairedCompanions();
		int i = 0;
		if (btComps != null)
		{
			if (btComps.size() > 0) {
				
				mRadioButtons = new RadioButton[btComps.size()];
				
				// Loop through paired devices
				for (BluetoothCompanion comp : btComps) {  
					Log.d(TAG, comp.getBluetoothDevice().getAddress());
					mRadioButtons[i] = new RadioButton(this);
					mRadioButtons[i].setText(comp.getBluetoothDevice().getAddress() + " - " + comp.getBluetoothDevice().getName());
					mRadioButtons[i].setId(i);
					if (comp.isActivated())
						mRadioButtons[i].setChecked(true);
					else
						mRadioButtons[i].setChecked(false);
					mRadioGroup.addView(mRadioButtons[i]);           
					i++;
				}
			}
		}
		if (i == 0)
		{			
			Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_LONG).show();
		}
		else if (mRadioGroup.getCheckedRadioButtonId() == -1)
		{
			mRadioGroup.setOnCheckedChangeListener(this);
		}
		else
		{
			mRadioGroup.setOnCheckedChangeListener(this);
			findViewById(R.id.button_unitary_test).setEnabled(true);
			findViewById(R.id.button_loop_test).setEnabled(true);
			startPclService();
			initService();
		}
		*/
		
	}
	
	@Override
	protected void onResume() {
		//if (mReleaseService == 1)
		{
			mRadioGroup.removeAllViewsInLayout();
			
			Set<BluetoothCompanion> btComps = mPclUtil.GetPairedCompanions();
			int i = 0;
			if (btComps != null)
			{
				if (btComps.size() > 0) {
					
					mRadioButtons = new RadioButton[btComps.size()];
					
					// Loop through paired devices
					for (BluetoothCompanion comp : btComps) {  
						Log.d(TAG, comp.getBluetoothDevice().getAddress());
						mRadioButtons[i] = new RadioButton(this);
						mRadioButtons[i].setText(comp.getBluetoothDevice().getAddress() + " - " + comp.getBluetoothDevice().getName());
						mRadioButtons[i].setId(i);
						if (comp.isActivated())
							mRadioButtons[i].setChecked(true);
						else
							mRadioButtons[i].setChecked(false);
						mRadioGroup.addView(mRadioButtons[i]);           
						i++;
					}
				}
			}
			if (i == 0)
			{			
				Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_LONG).show();
			}
			else if (mRadioGroup.getCheckedRadioButtonId() == -1)
			{
				mRadioGroup.setOnCheckedChangeListener(this);
			}
			else
			{
				mRadioGroup.setOnCheckedChangeListener(this);
				findViewById(R.id.button_unitary_test).setEnabled(true);
				findViewById(R.id.button_loop_test).setEnabled(true);
				startPclService();
				initService();
			}
		}
		mReleaseService = 1;
		
		if (isCompanionConnected())
        {
			new GetTermInfoTask().execute();
        	mtvState.setText(R.string.str_connected);
        	mtvState.setBackgroundColor(Color.GREEN);
        	mtvState.setTextColor(Color.BLACK);
        }
        else
        {
        	mtvState.setText(R.string.str_not_connected);
        	mtvState.setBackgroundColor(Color.RED);
        	mtvState.setTextColor(Color.BLACK);
        }
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "WelcomeActivity: onDestroy" );
		super.onDestroy();
		releaseService();
		if (mReleaseService == 1)
    	{
			stopPclService();
    	}
		
	}
	
	

	@Override
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		mReleaseService = 0;
		/*
		PclObject obj = new PclObject();
		obj.service = mPclService;
		obj.serviceConnection = mServiceConnection;
		return obj;
		*/
		CharSequence cs;
		int id = mRadioGroup.getCheckedRadioButtonId();
		if (id == -1)
			cs = "";
		else
			cs = ((RadioButton)(mRadioGroup.getChildAt(id))).getText();
		return cs;
	}
	
	
	
	
	@Override
	public void onClick(View v) {
		Intent i;
		int checkedId = mRadioGroup.getCheckedRadioButtonId();
		RadioButton rb = (RadioButton)mRadioGroup.getChildAt(checkedId);
		mPclUtil.ActivateCompanion((String) rb.getText().subSequence(0, 17));
		switch (v.getId()) {
		case R.id.button_unitary_test:
			i = new Intent(WelcomeActivity.this, TestListActivity.class);
			startActivity(i);
			break;
		case R.id.button_loop_test:
			i = new Intent(WelcomeActivity.this, PclLoopTestActivity.class);
			startActivity(i);
			break;
		}
		
	}

	class GetTermInfoTask extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... tmp) {
			Boolean bRet = getTermInfo();
			return bRet;
		}

		protected void onPostExecute(Boolean result) {
			if (result == true)
			{
				mtvSerialNumber.setText(String.format("SN: %08x / PN: %08x", SN, PN));
			}
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		findViewById(R.id.button_unitary_test).setEnabled(true);
		findViewById(R.id.button_loop_test).setEnabled(true);
		Log.d(TAG, String.format("onCheckedChanged id=%d", checkedId));
		if (checkedId != -1)
		{
			RadioButton rb = (RadioButton)group.getChildAt(checkedId);
			if (rb != null) {
				Log.d(TAG, String.format("onCheckedChanged id=%d text=%s", checkedId, rb.getText()));
				if (!rb.getText().equals(mCurrentDevice))
				{
					Log.d(TAG, String.format("current:%s saved:%s", rb.getText(), mCurrentDevice));
					mCurrentDevice = rb.getText();
					mPclUtil.ActivateCompanion((String) rb.getText().subSequence(0, 17));
					releaseService();
					stopPclService();
					startPclService();
					initService();
				}
			}
		}
	}
	
	@Override
	public void onStateChanged(String state) {
		if (state.equals("CONNECTED"))
		{
			new GetTermInfoTask().execute();
			mtvState.setText(R.string.str_connected);
			mtvState.setBackgroundColor(Color.GREEN);
			mtvState.setTextColor(Color.BLACK);
		}
		else
		{
			mtvState.setText(R.string.str_not_connected);
			mtvState.setBackgroundColor(Color.RED);
			mtvState.setTextColor(Color.BLACK);
			mtvSerialNumber.setText("");
		}
	}

	@Override
	void onPclServiceConnected() {
		Log.d(TAG, "onPclServiceConnected");
		mPclService.addDynamicBridgeLocal(6000, 0);
		
		if (isCompanionConnected())
        {
			new GetTermInfoTask().execute();
        	mtvState.setText(R.string.str_connected);
        	mtvState.setBackgroundColor(Color.GREEN);
        	mtvState.setTextColor(Color.BLACK);
        }
        else
        {
        	mtvState.setText(R.string.str_not_connected);
        	mtvState.setBackgroundColor(Color.RED);
        	mtvState.setTextColor(Color.BLACK);
        }
		
	}

	private void startPclService() {
		if (!mServiceStarted)
		{
			Intent i = new Intent(this, PclService.class);
			i.putExtra("PACKAGE_NAME", "com.ingenico.pcltestappwithlib");
			i.putExtra("FILE_NAME", "pairing_addr.txt");
			
			if (getApplicationContext().startService(i) != null)
				mServiceStarted = true;
		}
	}
    
    private void stopPclService() {
		if (mServiceStarted)
		{
			Intent i = new Intent(this, PclService.class);
			if (getApplicationContext().stopService(i))
				mServiceStarted = false;
		}
	}

	@Override
	public void onBarCodeReceived(String barCodeValue, int symbology) {
		// Do nothing		
	}

	@Override
	public void onBarCodeClosed() {
		// Do nothing		
	}
}
