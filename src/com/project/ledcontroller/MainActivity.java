package com.project.ledcontroller;

import static com.project.ledcontroller.DataBaseHelper.COLUMN_BLUE_1;
import static com.project.ledcontroller.DataBaseHelper.COLUMN_GREEN_1;
import static com.project.ledcontroller.DataBaseHelper.COLUMN_ID_1;
import static com.project.ledcontroller.DataBaseHelper.COLUMN_NAME_1;
import static com.project.ledcontroller.DataBaseHelper.COLUMN_RED_1;
import static com.project.ledcontroller.DataBaseHelper.DATABASE_TABLE_1;

import java.lang.reflect.Field;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener,
		DatabaseFragment.OnDataSelectedListener {

	// Debugging
	private static final String TAG = "BluetoothActivity";
	private static final boolean D = true;

	private BluetoothAdapter mBluetoothAdapter;
	private SparseArray<BluetoothDevice> mDevices;
	private Set<BluetoothDevice> pairedDevices;
	private BluetoothConnectService mConnectService = null;

	private ProgressDialog mProgress;
	public static Dialog mSaveDialog;
	private Boolean isScanning = false;
	public Boolean ClickFromList = false;
	public int red_from_list, green_from_list, blue_from_list;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int MSG_PROGRESS = 1;
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	ActionBar actionBar;
	DataBaseHelper DBhelper;
	public String[] idArray, colorNameArray, RedArray, GreenArray, BlueArray;
	BaseAdapter myAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LinearLayout verLayout = new LinearLayout(MainActivity.this);
			verLayout.setOrientation(LinearLayout.VERTICAL);

			LinearLayout horLayout = new LinearLayout(MainActivity.this);
			horLayout.setOrientation(LinearLayout.HORIZONTAL);

			TextView tv = new TextView(MainActivity.this);
			tv.setText("[" + colorNameArray[position] + "] ");
			tv.setTextSize(20);
			tv.setTextColor(Color.BLACK);
			tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// tv.setGravity(Gravity.CENTER_VERTICAL);

			TextView tv1 = new TextView(MainActivity.this);
			tv1.setText(RedArray[position] + " | ");
			tv1.setTextSize(18);
			tv1.setTextColor(Color.rgb(100, 100, 100));
			tv1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// tv2.setGravity(Gravity.BOTTOM | Gravity.RIGHT); //
			// 設定TextView在父容器的位置

			TextView tv2 = new TextView(MainActivity.this);
			tv2.setText(GreenArray[position] + " | ");
			tv2.setTextSize(18);
			tv2.setTextColor(Color.rgb(100, 100, 100));
			tv2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// tv2.setGravity(Gravity.BOTTOM | Gravity.RIGHT); //
			// 設定TextView在父容器的位置

			TextView tv3 = new TextView(MainActivity.this);
			tv3.setText(BlueArray[position]);
			tv3.setTextSize(18);
			tv3.setTextColor(Color.rgb(100, 100, 100));
			tv3.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			// tv2.setGravity(Gravity.BOTTOM | Gravity.RIGHT); //
			// 設定TextView在父容器的位置

			verLayout.addView(tv);
			verLayout.addView(horLayout);
			horLayout.addView(tv1);
			horLayout.addView(tv2);
			horLayout.addView(tv3);
			return verLayout;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (colorNameArray != null) {
				return colorNameArray.length;
			} else {
				return 0;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		setProgressBarIndeterminate(true);

		// Set up the action bar to show a dropdown list.
		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] { getString(R.string.title_section1),
								getString(R.string.title_section2) }), this);

		/**
		 * 強制顯示overflow menu
		 */
		forceShowActionBarOverflowMenu();

		DBhelper = new DataBaseHelper(this);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mConnectService = new BluetoothConnectService(mHandler);
		mDevices = new SparseArray<BluetoothDevice>();

		mProgress = new ProgressDialog(this);
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new ColorPickerFragment()).commit();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");
		/*
		 * We need to enforce that Bluetooth is first enabled, and take the user
		 * to settings to enable it if they have not done so.
		 */
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			// Bluetooth is disabled
			Log.i(TAG, "Bluetooth is disabled.");
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			Log.i(TAG, "Bluetooth has been force on.");
			return;
		} else {
			getPairedDevice();
		}
	}

	private void getPairedDevice() {
		//取得已經配對過的裝置
		Log.i(TAG, "Check if there are paired devices.");
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.i(TAG, "Device name: " + device.getName());
				mDevices.put(device.hashCode(), device);
				// 更新menu物件
				supportInvalidateOptionsMenu();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		settingChange();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 確認進度條是隱藏的
		mProgress.dismiss();
		// 取消所有掃描
		mHandler.removeCallbacks(mStopRunnable);
		mHandler.removeCallbacks(mStartRunnable);
		mBluetoothAdapter.cancelDiscovery();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// 停止 mConnectService
		if (mConnectService != null)
			mConnectService.stop();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		ColorPickerFragment CPF = new ColorPickerFragment();
		DatabaseFragment DF = new DatabaseFragment();
		switch (position) {
		case 0:
			Bundle args = new Bundle();
			if (ClickFromList) {
				args.putInt(ColorPickerFragment.ARG_COLOR_RED, red_from_list);
				args.putInt(ColorPickerFragment.ARG_COLOR_GREEN, green_from_list);
				args.putInt(ColorPickerFragment.ARG_COLOR_BLUE, blue_from_list);
				ClickFromList = false;
			}
			if (mConnectService.getState() == BluetoothConnectService.STATE_CONNECTED) {
				args.putString(ColorPickerFragment.ARG_STATE, "Connected");
			} else if (mConnectService.getState() == BluetoothConnectService.STATE_NONE) {
				args.putString(ColorPickerFragment.ARG_STATE, "Not connected");
			}
			CPF.setArguments(args);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, CPF).commit();
			break;
		case 1:
			Log.i(TAG, "Click Nav item" + position);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, DF).commit();
			break;
		default:
			Log.i(TAG, "Click Nav item default");
			getSupportFragmentManager().beginTransaction().replace(R.id.container, CPF).commit();
			break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Add any device elements we've discovered to the overflow menu
		//加入任何掃描到的裝置至action bar menu
		for (int i = 0; i < mDevices.size(); i++) {
			BluetoothDevice device = mDevices.valueAt(i);
			Log.i(TAG, "Add device " + device.getName() + " to menu");
			menu.add(0, mDevices.keyAt(i), 0, device.getName());
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// 按下掃描按鈕
		case R.id.action_scan:
			if (isScanning) {
				Log.i("Scan select", "mProgress is scanning, can't do it again.");
				return false;
				//			} else if (!isScanning && isConnect) {
				//				disconnectDevices();
			} else {
				Log.i("Scan select", "mProgress is not scanning. Scan!!!");
				isScanning = true;
				// item.setEnabled(false);
				mDevices.clear();
				startScan();
				return true;
			}
			// 其他(裝置)
		default:
			String address = mDevices.get(item.getItemId()).getAddress();
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			mConnectService.connect(device);
		}
		return super.onOptionsItemSelected(item);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled
				Log.d(TAG, "BT is enabled");
				getPairedDevice();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, "Bluetooth was not enabled. Can't controll device.", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private Runnable mStopRunnable = new Runnable() {
		@Override
		public void run() {
			stopScan();
		}
	};
	private Runnable mStartRunnable = new Runnable() {
		@Override
		public void run() {
			startScan();
		}
	};

	private void startScan() {
		// Register the BroadcastReceiver
		Log.i(TAG, "Start scan");
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);// Don't forget to unregister
											// during onDestroy
		mBluetoothAdapter.startDiscovery();
		setProgressBarIndeterminateVisibility(true);

		mHandler.postDelayed(mStopRunnable, 5000);
	}

	private void stopScan() {
		Log.i(TAG, "Stop scan");
		isScanning = false;
		unregisterReceiver(mReceiver);
		mBluetoothAdapter.cancelDiscovery();
		setProgressBarIndeterminateVisibility(false);

	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			// when discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Internet
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.i(TAG, "Found device: " + device.getName());
				Log.i("Device", "" + device);
				mDevices.put(device.hashCode(), device);
				Log.i("device.hashCode()", "" + device.hashCode());
				// 更新menu物件
				supportInvalidateOptionsMenu();
			}
		}

	};

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothConnectService.MSG_CONNECTED:
				// 無法使用 actionBar.setSelectedNavigationItem(0); 來更改txt_state文字
				ColorPickerFragment CPF = new ColorPickerFragment();
				Bundle args = new Bundle();
				args.putString(ColorPickerFragment.ARG_STATE, "Connected");
				CPF.setArguments(args);
				getSupportFragmentManager().beginTransaction().replace(R.id.container, CPF).commit();
				break;
			case BluetoothConnectService.MSG_NOT_CONNECTED:
				actionBar.setSelectedNavigationItem(0);
				break;
			case MSG_PROGRESS:
				mProgress.setMessage((String) msg.obj);
				if (!mProgress.isShowing()) {
					mProgress.show();
					Log.i(TAG, "Progress is show.");
				}
			}
		}
	};

	private void disconnectDevices() {
		if (mConnectService != null)
			mConnectService.stop();
	}

	public void sendMessage(CharSequence chars) {
		if (chars.length() > 0) {
			if (mConnectService.getState() != BluetoothConnectService.STATE_CONNECTED) {
				Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
				return;
			}
			//			Toast.makeText(this, "connect and write", Toast.LENGTH_SHORT).show();
			String message = chars.toString() + "\n";
			mConnectService.write(message.getBytes());
		}
	}

	private void getBasicInfo() {
		Log.i("getBasicInfo", "getBasicInfo");
		DBhelper.openDataBase(this);
		Cursor c = DBhelper.select(DATABASE_TABLE_1, new String[] { COLUMN_ID_1, COLUMN_NAME_1, COLUMN_RED_1,
				COLUMN_GREEN_1, COLUMN_BLUE_1 }, null, null, COLUMN_ID_1);
		int idArrayIndex = c.getColumnIndex(COLUMN_ID_1);
		int colorNameIndex = c.getColumnIndex(COLUMN_NAME_1);
		int RedIndex = c.getColumnIndex(COLUMN_RED_1);
		int GreenIndex = c.getColumnIndex(COLUMN_GREEN_1);
		int BlueIndex = c.getColumnIndex(COLUMN_BLUE_1);
		idArray = new String[c.getCount()];
		colorNameArray = new String[c.getCount()];
		RedArray = new String[c.getCount()];
		GreenArray = new String[c.getCount()];
		BlueArray = new String[c.getCount()];
		int i = 0;
		for (c.moveToFirst(); !(c.isAfterLast()); c.moveToNext()) {
			idArray[i] = c.getString(idArrayIndex);
			colorNameArray[i] = c.getString(colorNameIndex);
			switch (c.getString(RedIndex).length()) {
			case 1:
				RedArray[i] = "00" + c.getString(RedIndex);
				break;
			case 2:
				RedArray[i] = "0" + c.getString(RedIndex);
				break;
			case 3:
				RedArray[i] = c.getString(RedIndex);
				break;
			}
			switch (c.getString(GreenIndex).length()) {
			case 1:
				GreenArray[i] = "00" + c.getString(GreenIndex);
				break;
			case 2:
				GreenArray[i] = "0" + c.getString(GreenIndex);
				break;
			case 3:
				GreenArray[i] = c.getString(GreenIndex);
				break;
			}
			switch (c.getString(BlueIndex).length()) {
			case 1:
				BlueArray[i] = "00" + c.getString(BlueIndex);
				break;
			case 2:
				BlueArray[i] = "0" + c.getString(BlueIndex);
				break;
			case 3:
				BlueArray[i] = c.getString(BlueIndex);
				break;
			}
			i++;
		}
		c.close();
		DBhelper.close();

	}

	public void settingChange() {
		getBasicInfo();
		myAdapter.notifyDataSetChanged();
	}

	private void forceShowActionBarOverflowMenu() {
		//強制顯示overflow menu
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDataSelect(int position) {
		ClickFromList = true;
		red_from_list = Integer.parseInt(RedArray[position]);
		green_from_list = Integer.parseInt(GreenArray[position]);
		blue_from_list = Integer.parseInt(BlueArray[position]);
		actionBar.setSelectedNavigationItem(0);
	}

}
