package com.baydar.netonoff;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
 
public class TimerService extends Service {

	private PendingIntent pendingIntent = null;
	static int state = 1;
	static AlarmManager alarmManager;
	static int startStop = 1;
	static String networkType = "MobileData";
	static int onTime = 15000;
	static int offTime = 15000;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.getStringExtra("Stop").equals("0")) {
			if (intent.getAction().equals(
					"com.baydar.netonoff.TimerService.startforeground")) {
				Intent notificationIntent = new Intent(this, MainActivity.class);
				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent pendingIntent = PendingIntent.getActivity(this,
						0, notificationIntent, 0);

				Intent stopIntent = new Intent(this, this.getClass());
				stopIntent.addCategory(Intent.CATEGORY_HOME);
				stopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				stopIntent.putExtra("Stop", "0");
				stopIntent
						.setAction("com.baydar.netonoff.TimerService.stopforeground");
				PendingIntent stopPendingIntent = PendingIntent.getService(
						this, 0, stopIntent, 0);

				Intent startIntent = new Intent(this, this.getClass());
				stopIntent.addCategory(Intent.CATEGORY_HOME);
				stopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				stopIntent.putExtra("Stop", "0");
				stopIntent
						.setAction("com.baydar.netonoff.TimerService.stopforeground");
				PendingIntent startPendingIntent = PendingIntent.getService(
						this, 0, startIntent, 0);

				Intent pauseIntent = new Intent(this, this.getClass());
				pauseIntent.addCategory(Intent.CATEGORY_HOME);
				pauseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				pauseIntent.putExtra("Stop", "0");
				pauseIntent
						.setAction("com.baydar.netonoff.TimerService.stopforeground");
				PendingIntent pausePendingIntent = PendingIntent.getService(
						this, 0, startIntent, 0);

				Bitmap icon = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_launcher);

				Notification notification = new NotificationCompat.Builder(this)
						.setContentTitle("Network On Off")
						.setContentText("Network On Off")
						.setSmallIcon(R.drawable.ic_launcher)
						.setLargeIcon(
								Bitmap.createScaledBitmap(icon, 128, 128, false))
						.setContentIntent(pendingIntent)
						.setOngoing(true)
						.addAction(R.drawable.ic_start, null,
								startPendingIntent)
						.addAction(R.drawable.ic_pause, null,
								pausePendingIntent)
						.addAction(R.drawable.ic_stop, null, stopPendingIntent)
						.build();
				startForeground(101, notification);
				startStop = 2;
				networkType = intent.getStringExtra("NetworkType");
				deleteLog();
				setTimer(1);
			} else if (intent.getAction().equals(
					"com.baydar.netonoff.TimerService.stopforeground")) {
				stopForeground(true);
				stopSelf();
			}
		} else if (intent.getStringExtra("Stop").equals("1")) {
			if (pendingIntent != null) {
				alarmManager.cancel(pendingIntent);
			}
			startStop = 1;
			stopForeground(true);
			stopSelf();
		} else {
			boolean onOff = false;
			if (intent.getStringExtra("OnOff").equals("On")) {
				onOff = true;
			} else {
				onOff = false;
			}
			// intent.removeExtra("OnOff");
			String networkType = intent.getStringExtra("NetworkType");

			if (networkType.equals("MobileData")) {

				ConnectivityManager dataManager;
				dataManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				Method dataMtd = null;
				try {
					dataMtd = ConnectivityManager.class.getDeclaredMethod(
							"setMobileDataEnabled", boolean.class);
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}
				dataMtd.setAccessible(true);
				try {
					dataMtd.invoke(dataManager, onOff);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (networkType.equals("Wifi")) {
				WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				wifi.setWifiEnabled(onOff);

			} else {
				WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				wifi.setWifiEnabled(onOff);

				ConnectivityManager dataManager;
				dataManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				Method dataMtd = null;
				try {
					dataMtd = ConnectivityManager.class.getDeclaredMethod(
							"setMobileDataEnabled", boolean.class);
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				}
				dataMtd.setAccessible(true);
				try {
					dataMtd.invoke(dataManager, onOff);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (onOff) {
				setTimer(0);
			} else {
				setTimer(1);
			}
		}
		return START_STICKY;
	}

	public void deleteLog() {
		File file = new File(Environment.getExternalStorageDirectory()
				.toString() + "/log/log.txt");
		file.delete();
	}

	public void appendLog(String text) {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();

		File folder = new File(Environment.getExternalStorageDirectory()
				.toString(), "log");
		folder.mkdirs();

		File logFile = new File(folder, "log.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text + " " + today.format("%k:%M:%S"));
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "deprecation" })
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// String stop = intent.getStringExtra("Stop");
		// if (stop.equals("1")) {
		// if (pendingIntent != null) {
		// alarmManager.cancel(pendingIntent);
		// }
		// startStop = 1;
		// stopSelf();
		// } else if (stop.equals("0")) {
		// startStop = 2;
		// String onTimeStr = intent.getStringExtra("OnTime");
		// String offTimeStr = intent.getStringExtra("OffTime");
		// onTime = Integer.parseInt(onTimeStr);
		// offTime = Integer.parseInt(offTimeStr);
		// networkType = intent.getStringExtra("NetworkType");
		// setTimer(1);
		// } else if (stop.equals("2") && startStop == 2) {
		//
		// boolean onOff = false;
		// if (intent.getStringExtra("OnOff").equals("On")) {
		// onOff = true;
		// } else {
		// onOff = false;
		// }
		// // intent.removeExtra("OnOff");
		// String networkType = intent.getStringExtra("NetworkType");
		//
		// if (networkType.equals("MobileData")) {
		//
		// ConnectivityManager dataManager;
		// dataManager = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);
		// Method dataMtd = null;
		// try {
		// dataMtd = ConnectivityManager.class.getDeclaredMethod(
		// "setMobileDataEnabled", boolean.class);
		// } catch (NoSuchMethodException e1) {
		// e1.printStackTrace();
		// }
		// dataMtd.setAccessible(true);
		// try {
		// dataMtd.invoke(dataManager, onOff);
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// } else if (networkType.equals("Wifi")) {
		// WifiManager wifi = (WifiManager)
		// getSystemService(Context.WIFI_SERVICE);
		// wifi.setWifiEnabled(onOff);
		//
		// } else {
		// WifiManager wifi = (WifiManager)
		// getSystemService(Context.WIFI_SERVICE);
		// wifi.setWifiEnabled(onOff);
		//
		// ConnectivityManager dataManager;
		// dataManager = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);
		// Method dataMtd = null;
		// try {
		// dataMtd = ConnectivityManager.class.getDeclaredMethod(
		// "setMobileDataEnabled", boolean.class);
		// } catch (NoSuchMethodException e1) {
		// e1.printStackTrace();
		// }
		// dataMtd.setAccessible(true);
		// try {
		// dataMtd.invoke(dataManager, onOff);
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// if (onOff) {
		// setTimer(0);
		// }else{
		// setTimer(1);
		// }
		//
		// }
	}

	public void setTimer(int mode) {

		String filename = "prefSave.txt";
		StringBuffer stringBuffer = new StringBuffer();
		try {
			BufferedReader inputReader = new BufferedReader(
					new InputStreamReader(openFileInput(filename)));
			String inputString;

			while ((inputString = inputReader.readLine()) != null) {
				stringBuffer.append(inputString);
			}
			String allString = stringBuffer.toString();
			String[] tempArr;
			String delimiter = " ";
			tempArr = allString.split(delimiter);
			offTime = Integer.parseInt(tempArr[0])
					* Integer.parseInt(tempArr[3]) * 1000;
			onTime = Integer.parseInt(tempArr[1])
					* Integer.parseInt(tempArr[4]) * 1000;

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mode == 1) {
			Intent myIntent = new Intent(TimerService.this, this.getClass());
			myIntent.putExtra("OnOff", "On");
			myIntent.putExtra("NetworkType", networkType);
			myIntent.putExtra("Stop", "2");
			appendLog("On timer" + " " + onTime + " " + offTime + "");
			pendingIntent = PendingIntent.getService(TimerService.this, 101,
					myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//						SystemClock.elapsedRealtime() + onTime,1000, pendingIntent); 
//			}else{
				alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						SystemClock.elapsedRealtime() + onTime, pendingIntent);
//			}

		} else {
			Intent myIntent = new Intent(TimerService.this, this.getClass());
			myIntent.putExtra("OnOff", "Off");
			myIntent.putExtra("NetworkType", networkType);
			myIntent.putExtra("Stop", "2");
			appendLog("Oftimer" + " " + onTime + " " + offTime + "");
			pendingIntent = PendingIntent.getService(TimerService.this, 102,
					myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//						SystemClock.elapsedRealtime() + offTime,1000, pendingIntent);
//			}else{
				alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
						SystemClock.elapsedRealtime() + offTime, pendingIntent);
//			}
		}

	}

	// android durdurursa stopself() çaðrýldýðý varsayýlýyor.
	@Override
	public void onDestroy() {
		super.onDestroy();
		appendLog("Stopped Time");
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
		}
		stopForeground(true);
	}

}
