package com.baydar.netonoff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity {

	private static int activeTime = 10;
	private static int deactiveTime = 10;
	static final String ACTIVE_TIME = "activeTime";
	static final String DEACTIVE_TIME = "deactiveTime";
	static int state = 1;
	static int startStop = 1;
	static String networkType = "MobileData";
	static int timeFactorActive = 1;
	static int timeFactorDeactive = 1;

	public void onBackPressed() {
		moveTaskToBack(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		final Button close_button = (Button) findViewById(R.id.button2);
		final Button start_button = (Button) findViewById(R.id.button1);
		final EditText active_time = (EditText) findViewById(R.id.editText1);
		final EditText deactive_time = (EditText) findViewById(R.id.editText2);
		final CheckBox mobileDataBox = (CheckBox) findViewById(R.id.checkBox1);
		final CheckBox wifiBox = (CheckBox) findViewById(R.id.checkBox2);
		final Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		final Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);

		if (isMyServiceRunning()) {
			start_button.setText(getResources().getString(R.string.stop));

			String filename = "prefSave.txt";
			StringBuffer stringBuffer = new StringBuffer();
			try {
				// Attaching BufferedReader to the FileInputStream by the help
				// of InputStreamReader
				BufferedReader inputReader = new BufferedReader(
						new InputStreamReader(openFileInput(filename)));
				String inputString;
				// Reading data line by line and storing it into the
				// stringbuffer
				while ((inputString = inputReader.readLine()) != null) {
					stringBuffer.append(inputString + "\n");
				}
				String allString = stringBuffer.toString();
				String[] tempArr;
				String delimiter = " ";
				tempArr = allString.split(delimiter);
				active_time.setText(tempArr[0]);
				deactive_time.setText(tempArr[1]);
				if (tempArr[2].equals("Wifi")) {
					wifiBox.setChecked(true);
				} else if (tempArr[2].equals("MobileData")) {
					mobileDataBox.setChecked(true);
				} else {
					wifiBox.setChecked(true);
					mobileDataBox.setChecked(true);
				}
				if (tempArr[3].equals("1")) {
					spinner1.setSelection(1);
				} else if (tempArr[3].equals("60")) {
					spinner1.setSelection(0);
				} else {
					spinner1.setSelection(2);
				}

				if (tempArr[4].equals("1")) {
					spinner2.setSelection(1);
				} else if (tempArr[4].equals("60")) {
					spinner2.setSelection(0);
				} else {
					spinner2.setSelection(2);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			startStop = 2;
		} else {
			deleteFile();
		}

		start_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (startStop == 1) {
					// Add input check if exist and add integer check
					String activeTimeText = active_time.getText().toString();
					String deactiveTimeText = deactive_time.getText()
							.toString();

					String val = String.valueOf(spinner1.getSelectedItem());
					if (val.equals(getResources().getString(R.string.min))) {
						timeFactorActive = 60;
					} else if (val.equals(getResources()
							.getString(R.string.sec))) {
						timeFactorActive = 1;
					} else {
						timeFactorActive = 3600;
					}

					String val2 = String.valueOf(spinner2.getSelectedItem());
					if (val2.equals(getResources().getString(R.string.min))) {
						timeFactorDeactive = 60;
					} else if (val2.equals(getResources().getString(
							R.string.sec))) {
						timeFactorDeactive = 1;
					} else {
						timeFactorDeactive = 3600;
					}

					if (activeTimeText.length() != 0) {
						activeTime = Integer.parseInt(activeTimeText);
					} else {
						activeTime = 0;
					}
					if (deactiveTimeText.length() != 0) {
						deactiveTime = Integer.parseInt(deactiveTimeText);
					} else {
						deactiveTime = 0;
					}

					if (mobileDataBox.isChecked() && wifiBox.isChecked()) {
						networkType = "Both";
					} else if (mobileDataBox.isChecked()) {
						networkType = "MobileData";
					} else if (wifiBox.isChecked()) {
						networkType = "Wifi";
					}

					if (!mobileDataBox.isChecked() && !wifiBox.isChecked()) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.non_checkbox_selected),
								Toast.LENGTH_LONG).show();
					} else if (activeTime == 0 || deactiveTime == 0) {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.zero_input),
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(
										R.string.application_starting),
								Toast.LENGTH_LONG).show();
						state = 2;
						setTimer(1);
						start_button.setText(getResources().getString(
								R.string.stop));
						startStop = 2;
					}
				} else {
					// alarmManager.cancel(pendingIntent);
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.application_stopping),
							Toast.LENGTH_LONG).show();
					// alarmManager.cancel(pendingIntent);
					startStop = 1;
					start_button.setText(getResources().getString(
							R.string.start));
					Intent myIntent = new Intent(MainActivity.this,
							TimerService.class);
					myIntent.addCategory(Intent.CATEGORY_HOME);
					myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					myIntent.putExtra("Stop", "1");
					myIntent.setAction("com.baydar.netonoff.TimerService.stopforeground");
					startService(myIntent);
				}
			}
		});

		close_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// stopService(new Intent(MainActivity.this,
				// NetworkService.class));
				if (startStop == 2) {
					Intent myIntent = new Intent(MainActivity.this,
							TimerService.class);
					myIntent.putExtra("Stop", 1);
					myIntent.setAction("com.baydar.netonoff.TimerService.stopforeground");
					// myIntent.addCategory(Intent.CATEGORY_HOME);
					// myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// myIntent.putExtra("Stop", "1");
					// pendingIntent = PendingIntent.getBroadcast(
					// MainActivity.this, 0, myIntent,
					// PendingIntent.FLAG_UPDATE_CURRENT);
					// alarmManager = (AlarmManager)
					// getSystemService(ALARM_SERVICE);
					// alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					// SystemClock.elapsedRealtime() + 10, pendingIntent);
				}
				finish();
				// android.os.Process.killProcess(android.os.Process.myPid());
				// calisiyormu bilinmiyor
				// ((NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1);
				// uygulama kapandýktan sonra son bir iþlem yapýyor bu nasýl
				// engellenebilir?
			}
		});

	}// end onCreate

	@Override
	public void onResume() {
		super.onResume();
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.baydar.netonoff.TimerService".equals(service.service
					.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void setTimer(int mode) {

		Intent myIntent = new Intent(MainActivity.this, TimerService.class);
		myIntent.addCategory(Intent.CATEGORY_HOME);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myIntent.putExtra("NetworkType", networkType);
		myIntent.putExtra("Stop", "0");
		myIntent.setAction("com.baydar.netonoff.TimerService.startforeground");
		deleteFile();
		writeVariablesToFile();
		startService(myIntent);
	}

	public void deleteFile() {
		if (fileExistance("prefSave.txt")) {
			File dir = getFilesDir();
			File file = new File(dir, "prefSave.txt");
			file.delete();
		}
	}

	public boolean fileExistance(String fname) {
		File file = getBaseContext().getFileStreamPath(fname);
		return file.exists();
	}

	public void writeVariablesToFile() {
		String sBody = activeTime + " " + deactiveTime + " " + networkType
				+ " " + timeFactorActive + " " + timeFactorDeactive;
		writeFileOnInternalStorage(getApplicationContext(), "prefSave.txt",
				sBody);
	}

	public void writeFileOnInternalStorage(Context mcoContext,
			String sFileName, String sBody) {
		FileOutputStream fos;
		try {
			fos = openFileOutput(sFileName, Context.MODE_PRIVATE);
			// default mode is PRIVATE, can be APPEND etc.
			fos.write(sBody.getBytes());
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		// niye bole yapmisim ki
		// stopService(new Intent(MainActivity.this, NetworkService.class));
		// if (pendingIntent != null) {
		// alarmManager.cancel(pendingIntent);
		// }
		super.onDestroy();

	}

	public static class PlaceholderFragment extends Fragment {
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_my, container,
					false);
			return rootView;
		}
	}

	/**
	 * This class makes the ad request and loads the ad.
	 */
	public static class AdFragment extends Fragment {
		private AdView mAdView;

		public AdFragment() {
		}

		@Override
		public void onActivityCreated(Bundle bundle) {
			super.onActivityCreated(bundle);
			// Gets the ad view defined in layout/ad_fragment.xml with ad unit
			// ID set in
			// values/strings.xml.
			mAdView = (AdView) getView().findViewById(R.id.adView);
			// Create an ad request. Check logcat output for the hashed device
			// ID to
			// get test ads on a physical device. e.g.
			// "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
			AdRequest adRequest = new AdRequest.Builder().addTestDevice(
					AdRequest.DEVICE_ID_EMULATOR).build();
			// Start loading the ad in the background.
			mAdView.loadAd(adRequest);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_ad, container, false);
		}

		/** Called when leaving the activity */
		@Override
		public void onPause() {
			if (mAdView != null) {
				mAdView.pause();
			}
			super.onPause();
		}

		/** Called when returning to the activity */
		@Override
		public void onResume() {
			super.onResume();
			if (mAdView != null) {
				mAdView.resume();
			}
		}

		/** Called before the activity is destroyed */
		@Override
		public void onDestroy() {
			if (mAdView != null) {
				mAdView.destroy();
			}
			super.onDestroy();
		}
	}

}