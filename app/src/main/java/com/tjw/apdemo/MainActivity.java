package com.tjw.apdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	
	private Button mButton;
	private TextView mTextView;
	private ConnectivityManager mConnectivityManager;
	private WifiManager mWifiManager;
	private int mTime;
	private String mBssid;
	private Handler mHandler;
	private ListView mListView;
	private WifiInfo mWifiInfo;
	private List<ScanResult> mScanList;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mButton = (Button) findViewById(R.id.button);
		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.lv_aps);
		
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mHandler = new Handler();
		mScanList = new ArrayList<>();
		checkNet();
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mHandler.removeCallbacksAndMessages(null);
				checkNet();
				showNotification();
				showMyDialog(1);
			}
		});
	}
	
	private void checkNet() {
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				checkWifi();
				startChecking();
			} else {
				MyToast.show(this, "当前网络" + networkInfo.getTypeName());
			}
		} else {
			MyToast.show(this, "当前没有网络");
		}
	}
	
	private void startChecking() {
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(mBssid) && !mBssid.equals(mWifiManager.getConnectionInfo().getBSSID())) {
					if (!"00:00:00:00:00:00".equals(mWifiManager.getConnectionInfo().getBSSID())) {
						MyToast.show(MainActivity.this, "AP已经切换：" + mWifiManager.getConnectionInfo().getBSSID());
						
						if (mWifiManager.getConnectionInfo().getBSSID().contains("84")) {
							showNotification();
							showMyDialog(1);
						}
						if (mWifiManager.getConnectionInfo().getBSSID().contains("fc")) {
							showNotification();
							showMyDialog(2);
						}
					} else {
						MyToast.show(MainActivity.this, "无线网断开了");
					}
				}
				mBssid = mWifiManager.getConnectionInfo().getBSSID();
				
				mTime++;
				checkWifi();
				mHandler.postDelayed(this, 1000L);
			}
		}, 1000L);
	}
	
	private void showMyDialog(int i) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.show();
		Window window = alertDialog.getWindow();
		View layout = View.inflate(this, R.layout.dialog, null);
		ImageView imageView = (ImageView) layout.findViewById(R.id.image);
		if (i == 1) {
			imageView.setImageResource(R.drawable.img_84);
		} else {
			imageView.setImageResource(R.drawable.img_fc);
		} 
		if (window != null) {
			window.setBackgroundDrawableResource(android.R.color.transparent);
			window.setContentView(layout);
		}
	}
	
	private void checkWifi() {
		ArrayList<ScanResult> scanResults = new ArrayList<>();
		mWifiManager.startScan();
		List<ScanResult> resultList = mWifiManager.getScanResults();
		if (resultList == null || resultList.size() == 0) {
			MyToast.show(this, "没有搜索到wifi");
			return;
		}
		
		System.out.println(resultList.get(0).level);
		
		mWifiInfo = mWifiManager.getConnectionInfo();
		
		String bssid = mWifiInfo.getBSSID();
		String ipAddress = intToIp(mWifiInfo.getIpAddress());
		String ssid = mWifiInfo.getSSID();
		int rssi = mWifiInfo.getRssi();
		String wifis = "";
		
		for (int i = 0; i < resultList.size(); i++) {
			if (ssid.equals("\""+resultList.get(i).SSID+"\"")) {
				scanResults.add(resultList.get(i));
			}
			int count = 0;
			String wifi ="";
			for (int j = 0; j < resultList.size(); j++) {
				if (resultList.get(i).SSID.equals(resultList.get(j).SSID)) {
					count++;
					wifi = resultList.get(j).SSID + count + "个AP\n";
				}
			}
			
			if (!wifis.contains(resultList.get(i).SSID)) {
				wifis = wifis + wifi;
			}
			
		}
		
		
		mTextView.setText("当前所有wifi(" + resultList.size()
				+ "个AP)：\n" + wifis + "\n----------分割线---" + mTime + "s-------\n\n" +
				"当前wifi名：" + ssid + "\nmac地址：" + bssid
				+ "\nip地址：" + ipAddress + "\n信号强度：" + rssi+"\n");

		mListView.setAdapter(new MyAdapter(scanResults));
		
		
		
	}
	
	private String intToIp(int ip) {
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
				+ ((ip >> 24) & 0xFF);
	}
	
	private class MyAdapter extends BaseAdapter {
		List<ScanResult> scanList;
		public MyAdapter(List<ScanResult> scanList) {
			this.scanList = scanList;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return scanList.size();
		}
		
		@Override
		public Object getItem(int i) {
			return null;
		}
		
		@Override
		public long getItemId(int i) {
			return i;
		}
		
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			if (view == null) {
				view = View.inflate(MainActivity.this, R.layout.item_aps, null);
			}
			TextView view1 = ViewHolder.get(view, R.id.tv_ap);
			view1.setText("mac地址："+scanList.get(i).BSSID+"\n强度："+scanList.get(i).level);
			return view;
		}
	}
	
	
	private void showNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this,MainActivity.class), 0);
		
		mBuilder.setContentTitle("ap已改变")
				.setContentText("当前mac："+mWifiManager.getConnectionInfo().getBSSID())
				.setTicker("ap已改变") 
				.setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间  
				.setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级  
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消    
//				.setOngoing(true)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)  
				.setDefaults(Notification.DEFAULT_ALL)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合  
				//Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission  
				.setContentIntent(intent)
				.setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON  
		
		mNotificationManager.notify(0,mBuilder.build());
	}
	
}
