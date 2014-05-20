package com.project.leccontroller;

import static com.project.leccontroller.DataBaseHelper.COLUMN_BLUE_1;
import static com.project.leccontroller.DataBaseHelper.COLUMN_GREEN_1;
import static com.project.leccontroller.DataBaseHelper.COLUMN_ID_1;
import static com.project.leccontroller.DataBaseHelper.COLUMN_NAME_1;
import static com.project.leccontroller.DataBaseHelper.COLUMN_RED_1;
import static com.project.leccontroller.DataBaseHelper.DATABASE_TABLE_1;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends ActionBarActivity {

	// Debugging
	private static final String TAG = "BluetoothActivity";
	private static final boolean D = true;

	private static final String DEVICE_NAME = "SensorTag";
	public static final String TOAST = "toast";

	private static final int REQUEST_ENABLE_BT = 1;

	private BluetoothAdapter mBluetoothAdapter;
	private SparseArray<BluetoothDevice> mDevices;
	private Set<BluetoothDevice> pairedDevices;
	private BluetoothConnectService mConnectService = null;

	private ProgressDialog mProgress;
	private static Dialog mSaveDialog, mOpenDialog;
	private Boolean isScanning = false;
	private Boolean isConnect = false;

	DataBaseHelper DBhelper;
	private String[] colorNameArray, RedArray, GreenArray, BlueArray;
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

		DBhelper = new DataBaseHelper(this);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mConnectService = new BluetoothConnectService(this, mHandler);

		mDevices = new SparseArray<BluetoothDevice>();

		mProgress = new ProgressDialog(this);
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
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
		// Check if there are paired devices
		Log.i(TAG, "Check if there are paired devices.");
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.i(TAG, "Device name: " + device.getName());
				mDevices.put(device.hashCode(), device);
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
		// Make sure dialog is hidden
		mProgress.dismiss();
		// Cancel any scans in progress
		mHandler.removeCallbacks(mStopRunnable);
		mHandler.removeCallbacks(mStartRunnable);
		mBluetoothAdapter.cancelDiscovery();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Disconnect from any active tag connection
		// if (mConnectedGatt != null) {
		// mConnectedGatt.disconnect();
		// mConnectedGatt = null;
		// }
		if (mConnectService != null)
			mConnectService.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		// Add any device elements we've discovered to the overflow menu
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
		case R.id.action_scan:
			if (isScanning) {
				Log.i("Scan select", "mProgress is scanning, can't do it again.");
				return false;
			} else {
				Log.i("Scan select", "mProgress is not scanning. Scan!!!");
				isScanning = true;
				// item.setEnabled(false);
				mDevices.clear();
				startScan();
				return true;
			}

		default:
			// // Obtain the discovered device to connect with
			BluetoothDevice device = mDevices.get(item.getItemId());
			Log.i(TAG, "Connecting to " + device.getName());

			// Make a connection with the device
			mConnectService.connect(device);
			// Display progress UI
			mHandler.sendMessage(Message.obtain(null, MSG_PROGRESS, "Connecting to " + device.getName() + "..."));
			return super.onOptionsItemSelected(item);
		}
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

	// Create a BroadcastReceiver for ACTION_FOUND
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
				/*
				 * We are looking for SensorTag devices only, so validate the
				 * name that each device reports before adding it to our
				 * collection
				 */
				// if (DEVICE_NAME.equals(device.getName())) {
				Log.i("Device", "" + device);
				mDevices.put(device.hashCode(), device);
				Log.i("device.hashCode()", "" + device.hashCode());
				// Update the overflow menu
				supportInvalidateOptionsMenu();
			}
		}

	};

	// Message types sent from the BluetoothConnectService Handler
	public static final int MSG_STATE_CHANGE = 1;
	public static final int MSG_TOAST = 2;
	private static final int MSG_PROGRESS = 3;
	public static final int MSG_DISMISS = 4;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				ActionBar ab = getSupportActionBar();
				switch (msg.arg1) {
				case BluetoothConnectService.STATE_NONE:
					ab.setTitle("LED");
					break;
				case BluetoothConnectService.STATE_CONNECTED:
					ab.setTitle("LED (Connected)");
					isConnect = true;
					break;
				case BluetoothConnectService.STATE_CONNECTIONLOST:
					ab.setTitle("LED (Connection Lost)");
					isConnect = false;
					break;
				}
				break;
			case MSG_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			case MSG_PROGRESS:
				mProgress.setMessage((String) msg.obj);
				if (!mProgress.isShowing()) {
					mProgress.show();
					Log.i(TAG, "Progress is show.");
				}
				break;
			case MSG_DISMISS:
				mProgress.dismiss();
				Log.i(TAG, "Progress is hide.");
				break;

			}
		}
	};

	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mConnectService.getState() != BluetoothConnectService.STATE_CONNECTED) {
			Toast.makeText(this, "You are not connected to a device.", Toast.LENGTH_SHORT).show();
			isConnect = false;
			return;
		}

		// Get the message bytes and tell the BluetoothConnectService to write
		byte[] send = message.getBytes();
		mConnectService.write(send);
		Log.i(TAG, "Sending message... ");

	}

	private void getBasicInfo() {
		Log.i("getBasicInfo", "getBasicInfo");
		DBhelper.openDataBase(this);
		Cursor c = DBhelper.select(DATABASE_TABLE_1, new String[] { COLUMN_NAME_1, COLUMN_RED_1, COLUMN_GREEN_1,
				COLUMN_BLUE_1 }, null, null, COLUMN_ID_1);
		int colorNameIndex = c.getColumnIndex(COLUMN_NAME_1);
		int RedIndex = c.getColumnIndex(COLUMN_RED_1);
		int GreenIndex = c.getColumnIndex(COLUMN_GREEN_1);
		int BlueIndex = c.getColumnIndex(COLUMN_BLUE_1);
		colorNameArray = new String[c.getCount()];
		RedArray = new String[c.getCount()];
		GreenArray = new String[c.getCount()];
		BlueArray = new String[c.getCount()];
		int i = 0;
		for (c.moveToFirst(); !(c.isAfterLast()); c.moveToNext()) {
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

	private void settingChange() {
		getBasicInfo();
		myAdapter.notifyDataSetChanged();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		String last_r = null, last_g = null, last_b = null;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			final TextView txt_title = (TextView) rootView.findViewById(R.id.txt_title);
			txt_title.setText("This is LED color controller.");

			final EditText edt_R = (EditText) rootView.findViewById(R.id.edt_R);
			final EditText edt_G = (EditText) rootView.findViewById(R.id.edt_G);
			final EditText edt_B = (EditText) rootView.findViewById(R.id.edt_B);
			edt_R.setText("0");
			edt_G.setText("0");
			edt_B.setText("0");

			final View view_color = (View) rootView.findViewById(R.id.view_color);
			view_color.setBackgroundColor(Color.rgb(0, 0, 0));

			final SeekBar skb_R = (SeekBar) rootView.findViewById(R.id.skb_R);
			final SeekBar skb_G = (SeekBar) rootView.findViewById(R.id.skb_G);
			final SeekBar skb_B = (SeekBar) rootView.findViewById(R.id.skb_B);
			skb_R.setProgress(0);
			skb_G.setProgress(0);
			skb_B.setProgress(0);

			Button btn_confirm = (Button) rootView.findViewById(R.id.btn_confirm);

			final ToggleButton tgb_OnOff = (ToggleButton) rootView.findViewById(R.id.tgb_OnOff);

			Button btn_hk1 = (Button) rootView.findViewById(R.id.btn_hk1);
			Button btn_hk2 = (Button) rootView.findViewById(R.id.btn_hk2);
			Button btn_hk3 = (Button) rootView.findViewById(R.id.btn_hk3);
			Button btn_set = (Button) rootView.findViewById(R.id.btn_set);

			Button btn_open = (Button) rootView.findViewById(R.id.btn_open);
			Button btn_save = (Button) rootView.findViewById(R.id.btn_save);

			// 點選文字欄位清除內容
			OnFocusChangeListener OFCL = new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if (hasFocus == true) {
						switch (v.getId()) {
						case R.id.edt_R:
							edt_R.setText("");
							break;
						case R.id.edt_G:
							edt_G.setText("");
							break;
						case R.id.edt_B:
							edt_B.setText("");
							break;
						}
					}
				}
			};
			edt_R.setOnFocusChangeListener(OFCL);
			edt_G.setOnFocusChangeListener(OFCL);
			edt_B.setOnFocusChangeListener(OFCL);

			// 輸入欄按下Done後，偵測輸入內容及改變顏色拉條
			OnEditorActionListener OEAL = new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					// TODO Auto-generated method stub
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						int red = 0, green = 0, blue = 0;
						try {
							red = Integer.parseInt(edt_R.getText().toString());
						} catch (Exception e) {
							// edt_R.setText("0");
							skb_R.setProgress(red);
						}
						try {
							green = Integer.parseInt(edt_G.getText().toString());
						} catch (Exception e) {
							// edt_G.setText("0");
							skb_G.setProgress(green);
						}
						try {
							blue = Integer.parseInt(edt_B.getText().toString());
						} catch (Exception e) {
							// edt_B.setText("0");
							skb_B.setProgress(blue);
						}

						if (red > 255)
							red = 255;
						if (green > 255)
							green = 255;
						if (blue > 255)
							blue = 255;
						skb_R.setProgress(red);
						skb_G.setProgress(green);
						skb_B.setProgress(blue);
						view_color.setBackgroundColor(Color.rgb(red, green, blue));
					}
					return false;
				}
			};
			edt_R.setOnEditorActionListener(OEAL);
			edt_G.setOnEditorActionListener(OEAL);
			edt_B.setOnEditorActionListener(OEAL);

			// 顏色條控制
			OnSeekBarChangeListener OSBCL = new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					String txt_progress = String.valueOf(progress);
					txt_title.setText("This is LED color controller.");
					switch (seekBar.getId()) {
					case R.id.skb_R:// 動到顏色條時，依照顏色條，調整欄位數值和顏色塊
						edt_R.setText(txt_progress);
						edt_G.setText("" + skb_G.getProgress());
						edt_B.setText("" + skb_B.getProgress());
						view_color.setBackgroundColor(Color.rgb(progress, skb_G.getProgress(), skb_B.getProgress()));
						break;
					case R.id.skb_G:
						edt_R.setText("" + skb_R.getProgress());
						edt_G.setText(txt_progress);
						edt_B.setText("" + skb_B.getProgress());
						view_color.setBackgroundColor(Color.rgb(skb_R.getProgress(), progress, skb_B.getProgress()));
						break;
					case R.id.skb_B:
						edt_R.setText("" + skb_R.getProgress());
						edt_G.setText("" + skb_G.getProgress());
						edt_B.setText(txt_progress);
						view_color.setBackgroundColor(Color.rgb(skb_R.getProgress(), skb_G.getProgress(), progress));
						break;
					}

				}
			};
			skb_R.setOnSeekBarChangeListener(OSBCL);
			skb_G.setOnSeekBarChangeListener(OSBCL);
			skb_B.setOnSeekBarChangeListener(OSBCL);

			// 送出顏色
			OnClickListener confirmListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String r = null, g = null, b = null;
					boolean goodToGo = true;
					int rr = 000, bb = 000, gg = 000;
					// 取得要送出的(R,G,B)
					try {
						r = edt_R.getText().toString();
						g = edt_G.getText().toString();
						b = edt_B.getText().toString();
						rr = Integer.parseInt(r);
						bb = Integer.parseInt(b);
						gg = Integer.parseInt(g);
					} catch (Exception e) {
						goodToGo = false;
						Toast toast = Toast.makeText((MainActivity) getActivity(), "尚未填妥顏色", Toast.LENGTH_LONG);
						toast.show();

					}
					if (goodToGo) {
						// 補滿三位數
						if (rr < 10) {
							r = "00" + r;
						} else if (rr > 10 && rr < 100) {
							r = "0" + r;
						}
						if (gg < 10) {
							g = "00" + g;
						} else if (gg > 10 && gg < 100) {
							g = "0" + g;
						}
						if (bb < 10) {
							b = "00" + b;
						} else if (bb > 10 && bb < 100) {
							b = "0" + b;
						}
						// 儲存最後的顏色
						last_r = "@001." + r + "#";
						last_g = "@002." + g + "#";
						last_b = "@003." + b + "#";
						// 傳送參數出去
						String messageToSend = last_r + "\n" + last_g + "\n" + last_b;
						((MainActivity) getActivity()).sendMessage(messageToSend);
						Toast toDevice = Toast
								.makeText((MainActivity) getActivity(), messageToSend, Toast.LENGTH_SHORT);
						toDevice.show();
						if (((MainActivity) getActivity()).isConnect) {
							tgb_OnOff.setChecked(true);
							tgb_OnOff.setClickable(true);
						} else {
							tgb_OnOff.setChecked(false);
							tgb_OnOff.setClickable(false);
						}
					}
				}
			};
			btn_confirm.setOnClickListener(confirmListener);

			// 亮暗開關
			OnCheckedChangeListener OnOffListener = new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					String messageToSend = last_r + "\n" + last_g + "\n" + last_b;
					// TODO Auto-generated method stub
					if (isChecked) {
						Toast toDevice = Toast
								.makeText((MainActivity) getActivity(), messageToSend, Toast.LENGTH_SHORT);
						toDevice.show();
					} else {
						((MainActivity) getActivity()).sendMessage(messageToSend);
						Toast toDevice = Toast
								.makeText((MainActivity) getActivity(), messageToSend, Toast.LENGTH_SHORT);
						toDevice.show();
					}
				}
			};
			tgb_OnOff.setOnCheckedChangeListener(OnOffListener);

			// 快速鍵開關
			OnClickListener HK_OCL = new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SharedPreferences settings = (SharedPreferences) getActivity()
							.getSharedPreferences("Preference", 0);
					int hk_r, hk_g, hk_b;
					switch (v.getId()) {
					case R.id.btn_hk1:
						hk_r = Integer.parseInt(settings.getString("hotkey0_R", "0"));
						hk_g = Integer.parseInt(settings.getString("hotkey0_G", "0"));
						hk_b = Integer.parseInt(settings.getString("hotkey0_B", "0"));
						skb_R.setProgress(hk_r);
						skb_G.setProgress(hk_g);
						skb_B.setProgress(hk_b);
						txt_title.setText("Current state: Hotkey1");
						break;
					case R.id.btn_hk2:
						hk_r = Integer.parseInt(settings.getString("hotkey1_R", "0"));
						hk_g = Integer.parseInt(settings.getString("hotkey1_G", "0"));
						hk_b = Integer.parseInt(settings.getString("hotkey1_B", "0"));
						skb_R.setProgress(hk_r);
						skb_G.setProgress(hk_g);
						skb_B.setProgress(hk_b);
						txt_title.setText("Current state: Hotkey2");
						break;
					case R.id.btn_hk3:
						hk_r = Integer.parseInt(settings.getString("hotkey2_R", "0"));
						hk_g = Integer.parseInt(settings.getString("hotkey2_G", "0"));
						hk_b = Integer.parseInt(settings.getString("hotkey2_B", "0"));
						skb_R.setProgress(hk_r);
						skb_G.setProgress(hk_g);
						skb_B.setProgress(hk_b);
						txt_title.setText("Current state: Hotkey3");
						break;
					case R.id.btn_set:
						Log.i("SetButton", "it's work");
						String[] items = { "Hotkey1", "Hotkey2", "Hotkey3" };
						AlertDialog.Builder adBuilder = new AlertDialog.Builder((MainActivity) getActivity());
						adBuilder.setTitle("Set Hotkey");
						adBuilder.setItems(items, adSetListener);
						adBuilder.setNegativeButton("Cancel", null);
						adBuilder.create().show();
						break;
					}
				}

				DialogInterface.OnClickListener adSetListener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// 取得SharedPreference設定("Preference"為設定檔的名稱)
						SharedPreferences settings = (SharedPreferences) getActivity().getSharedPreferences(
								"Preference", 0);
						// 取得(R,G,B)
						String r = null, g = null, b = null;
						r = edt_R.getText().toString();
						g = edt_G.getText().toString();
						b = edt_B.getText().toString();
						// 存入數值
						settings.edit().putString("hotkey" + which + "_R", r).commit();
						settings.edit().putString("hotkey" + which + "_G", g).commit();
						settings.edit().putString("hotkey" + which + "_B", b).commit();
						txt_title.setText("Current state: Hotkey" + (which + 1));
					}
				};
			};
			btn_hk1.setOnClickListener(HK_OCL);
			btn_hk2.setOnClickListener(HK_OCL);
			btn_hk3.setOnClickListener(HK_OCL);
			btn_set.setOnClickListener(HK_OCL);

			// 資料庫開起儲存
			OnClickListener OpenSave_OCL = new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch (v.getId()) {
					case R.id.btn_open:
						mOpenDialog = new Dialog(getActivity());
						mOpenDialog.setTitle(R.string.dialog_open_title);
						mOpenDialog.setCancelable(false);
						mOpenDialog.setContentView(R.layout.dialog_db_open);

						ListView dialog_o_list = (ListView) mOpenDialog.findViewById(R.id.dialog_o_list);
						Button dialog_o_btn_cancel = (Button) mOpenDialog.findViewById(R.id.dialgo_o_btn_cancel);

						dialog_o_list.setAdapter(((MainActivity) getActivity()).myAdapter);
						dialog_o_list.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
								// TODO Auto-generated method stub
								String name = ((MainActivity) getActivity()).colorNameArray[position];
								int red = Integer.parseInt(((MainActivity) getActivity()).RedArray[position]);
								int green = Integer.parseInt(((MainActivity) getActivity()).GreenArray[position]);
								int blue = Integer.parseInt(((MainActivity) getActivity()).BlueArray[position]);

								skb_R.setProgress(red);
								skb_G.setProgress(green);
								skb_B.setProgress(blue);
								txt_title.setText("Current state: " + name);
								mOpenDialog.dismiss();
							}
						});
						dialog_o_btn_cancel.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								mOpenDialog.dismiss();
							}
						});

						mOpenDialog.show();
						break;
					case R.id.btn_save:
						int save_r = Integer.parseInt(edt_R.getText().toString());
						int save_g = Integer.parseInt(edt_G.getText().toString());
						int save_b = Integer.parseInt(edt_B.getText().toString());

						mSaveDialog = new Dialog(getActivity());
						mSaveDialog.setTitle(R.string.dialog_save_title);
						mSaveDialog.setCancelable(false);
						mSaveDialog.setContentView(R.layout.dialog_db_save);

						final EditText dialog_s_edt_name = (EditText) mSaveDialog.findViewById(R.id.dialog_s_edt_name);
						View dialog_s_view_color = (View) mSaveDialog.findViewById(R.id.dialog_s_view_color);
						TextView dialog_s_txt_rgb = (TextView) mSaveDialog.findViewById(R.id.dialog_s_txt_rgb);
						Button dialog_s_btn_confirm = (Button) mSaveDialog.findViewById(R.id.dialog_s_btn_confirm);
						Button dialog_s_btn_cancel = (Button) mSaveDialog.findViewById(R.id.dialog_s_btn_cancel);

						dialog_s_view_color.setBackgroundColor(Color.rgb(save_r, save_g, save_b));
						dialog_s_txt_rgb.setText("R: " + save_r + " | G: " + save_g + " | B: " + save_b);

						dialog_s_btn_confirm.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								switch (v.getId()) {
								case R.id.dialog_s_btn_confirm:
									String name = dialog_s_edt_name.getText().toString();
									if (name.equals("")) {
										Toast emptyName = Toast.makeText(getActivity(), "Have to input name!",
												Toast.LENGTH_SHORT);
										emptyName.show();
										break;
									}
									String red = edt_R.getText().toString();
									String green = edt_G.getText().toString();
									String blue = edt_B.getText().toString();

									String[] columnsValue = { name, red, green, blue };

									DataBaseHelper DBhelper = new DataBaseHelper(getActivity());
									DBhelper.openDataBase(getActivity());
									try {
										DBhelper.insert(DATABASE_TABLE_1, columnsValue);
										Toast SuccessToast = Toast.makeText(getActivity(), "Saving Successful!",
												Toast.LENGTH_SHORT);
										SuccessToast.show();
									} catch (Exception e) {
										e.printStackTrace();
										Toast ErrorToast = Toast.makeText(getActivity(), "Saving DB Error!",
												Toast.LENGTH_SHORT);

										ErrorToast.show();
									}
									DBhelper.close();
									((MainActivity) getActivity()).settingChange();
									mSaveDialog.dismiss();
								}
							}
						});
						dialog_s_btn_cancel.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								mSaveDialog.dismiss();
							}
						});
						mSaveDialog.show();
					}
				}
			};
			btn_open.setOnClickListener(OpenSave_OCL);
			btn_save.setOnClickListener(OpenSave_OCL);

			return rootView;
		}
	}
}
