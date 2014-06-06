package com.project.ledcontroller;

import static com.project.ledcontroller.DataBaseHelper.DATABASE_TABLE_1;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ColorPickerFragment extends Fragment {

	public ColorPickerFragment() {
	}

	String last_r = null, last_g = null, last_b = null;
	static String ARG_COLOR_RED = "arg_color_red";
	static String ARG_COLOR_GREEN = "arg_color_green";
	static String ARG_COLOR_BLUE = "arg_color_blue";
	static String ARG_STATE = "arg_state";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		final TextView txt_title = (TextView) rootView.findViewById(R.id.txt_title);
		final TextView txt_state = (TextView) rootView.findViewById(R.id.txt_state);
		txt_title.setText("This is LED color controller.");

		final EditText edt_R = (EditText) rootView.findViewById(R.id.edt_R);
		final EditText edt_G = (EditText) rootView.findViewById(R.id.edt_G);
		final EditText edt_B = (EditText) rootView.findViewById(R.id.edt_B);

		final View view_color = (View) rootView.findViewById(R.id.view_color);

		final SeekBar skb_R = (SeekBar) rootView.findViewById(R.id.skb_R);
		final SeekBar skb_G = (SeekBar) rootView.findViewById(R.id.skb_G);
		final SeekBar skb_B = (SeekBar) rootView.findViewById(R.id.skb_B);

		if (getArguments() != null) {
			txt_state.setText(getArguments().getString(ARG_STATE));
			Log.i("getArguments()", "" + getArguments().getString(ARG_STATE));
			edt_R.setText(Integer.toString(getArguments().getInt(ARG_COLOR_RED)));
			edt_G.setText(Integer.toString(getArguments().getInt(ARG_COLOR_GREEN)));
			edt_B.setText(Integer.toString(getArguments().getInt(ARG_COLOR_BLUE)));
			skb_R.setProgress(getArguments().getInt(ARG_COLOR_RED));
			skb_G.setProgress(getArguments().getInt(ARG_COLOR_GREEN));
			skb_B.setProgress(getArguments().getInt(ARG_COLOR_BLUE));
			view_color.setBackgroundColor(Color.rgb(getArguments().getInt(ARG_COLOR_RED),
					getArguments().getInt(ARG_COLOR_GREEN), getArguments().getInt(ARG_COLOR_BLUE)));

		} else {
			txt_state.setText("not Connected");
			edt_R.setText("0");
			edt_G.setText("0");
			edt_B.setText("0");
			skb_R.setProgress(0);
			skb_G.setProgress(0);
			skb_B.setProgress(0);
			view_color.setBackgroundColor(Color.rgb(0, 0, 0));
		}

		Button btn_confirm = (Button) rootView.findViewById(R.id.btn_confirm);

		final ToggleButton tgb_OnOff = (ToggleButton) rootView.findViewById(R.id.tgb_OnOff);

		Button btn_hk1 = (Button) rootView.findViewById(R.id.btn_hk1);
		Button btn_hk2 = (Button) rootView.findViewById(R.id.btn_hk2);
		Button btn_hk3 = (Button) rootView.findViewById(R.id.btn_hk3);
		Button btn_set = (Button) rootView.findViewById(R.id.btn_set);
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

		//顏色條及預覽色跟著輸入做改變，超過255時自動設成255
		class customTextWatcher implements TextWatcher {
			private View view;

			private customTextWatcher(View view) {
				this.view = view;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				Log.i("beforeTextChanged", "CharSequence: " + s + " start: " + start + " count: " + count + " after: "
						+ after);
				// Log.i("beforeTextChanged",
				// "This method is called to notify you that, within "
				// + s
				// + ", the "
				// + count
				// + " characters beginning at "
				// + start
				// + " are about to be replaced by new text with length "
				// + after
				// +
				// ". It is an error to attempt to make changes to s from this callback.");
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.i("onTextChanged", "CharSequence: " + s + " start: " + start + " before: " + before + " count: "
						+ count);
				// Log.i("onTextChanged",
				// "This method is called to notify you that, within "
				// + s
				// + ", the "
				// + count
				// + " characters beginning at "
				// + start
				// + " have just replaced old text that had length "
				// + before
				// +
				// ". It is an error to attempt to make changes to s from this callback.");
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				Log.i("afterTextChanged", "Editable: " + s);
				int num = 0;
				try {
					num = Integer.parseInt(s.toString());
				} catch (Exception e) {
					Log.i("afterTextChanged", "catch");
					num = -1;
				}
				if (!s.equals("")) {
					if (num > 255) {
						// s = "255";
						switch (view.getId()) {
						case R.id.edt_R:
							edt_R.setText("255");
							skb_R.setProgress(255);
							break;
						case R.id.edt_G:
							edt_G.setText("255");
							skb_G.setProgress(255);
							break;
						case R.id.edt_B:
							edt_B.setText("255");
							skb_B.setProgress(255);
							break;
						}
					} else if (num > -1) {
						switch (view.getId()) {
						case R.id.edt_R:
							skb_R.setProgress(num);
							edt_R.setSelection(s.length());
							break;
						case R.id.edt_G:
							skb_G.setProgress(num);
							edt_G.setSelection(s.length());
							break;
						case R.id.edt_B:
							skb_B.setProgress(num);
							edt_B.setSelection(s.length());
							break;
						}
					}
					//if(num == -1) EditText為空值，不做其他事。
				}
			}

		}
		edt_R.addTextChangedListener(new customTextWatcher(edt_R));
		edt_G.addTextChangedListener(new customTextWatcher(edt_G));
		edt_B.addTextChangedListener(new customTextWatcher(edt_B));

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
				txt_title.setText("K&J");
				switch (seekBar.getId()) {
				case R.id.skb_R:// 動到顏色條時，依照顏色條，調整欄位數值和顏色塊
					edt_R.setText(txt_progress);
					//					edt_G.setText("" + skb_G.getProgress());
					//					edt_B.setText("" + skb_B.getProgress());
					view_color.setBackgroundColor(Color.rgb(progress, skb_G.getProgress(), skb_B.getProgress()));
					break;
				case R.id.skb_G:
					//					edt_R.setText("" + skb_R.getProgress());
					edt_G.setText(txt_progress);
					//					edt_B.setText("" + skb_B.getProgress());
					view_color.setBackgroundColor(Color.rgb(skb_R.getProgress(), progress, skb_B.getProgress()));
					break;
				case R.id.skb_B:
					//					edt_R.setText("" + skb_R.getProgress());
					//					edt_G.setText("" + skb_G.getProgress());
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
					last_r = "@001," + r + "#";
					last_g = "@002," + g + "#";
					last_b = "@003," + b + "#";
					// 傳送參數出去
					((MainActivity) getActivity()).sendMessage(last_r);
					((MainActivity) getActivity()).sendMessage(last_g);
					((MainActivity) getActivity()).sendMessage(last_b);

					if (txt_state.getText().equals("Connected")) {
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
				// TODO Auto-generated method stub
				if (isChecked) {
					((MainActivity) getActivity()).sendMessage(last_r);
					((MainActivity) getActivity()).sendMessage(last_g);
					((MainActivity) getActivity()).sendMessage(last_b);
				} else {
					((MainActivity) getActivity()).sendMessage("@001,000#");
					((MainActivity) getActivity()).sendMessage("@002,000#");
					((MainActivity) getActivity()).sendMessage("@003,000#");
				}
			}
		};
		tgb_OnOff.setOnCheckedChangeListener(OnOffListener);

		// 快速鍵開關
		OnClickListener HK_OCL = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SharedPreferences settings = (SharedPreferences) getActivity().getSharedPreferences("Preference", 0);
				int hk_r, hk_g, hk_b;
				switch (v.getId()) {
				case R.id.btn_hk1:
					hk_r = Integer.parseInt(settings.getString("hotkey0_R", "0"));
					hk_g = Integer.parseInt(settings.getString("hotkey0_G", "0"));
					hk_b = Integer.parseInt(settings.getString("hotkey0_B", "0"));
					skb_R.setProgress(hk_r);
					skb_G.setProgress(hk_g);
					skb_B.setProgress(hk_b);
					txt_title.setText("K&J Current state: Hotkey1");
					break;
				case R.id.btn_hk2:
					hk_r = Integer.parseInt(settings.getString("hotkey1_R", "0"));
					hk_g = Integer.parseInt(settings.getString("hotkey1_G", "0"));
					hk_b = Integer.parseInt(settings.getString("hotkey1_B", "0"));
					skb_R.setProgress(hk_r);
					skb_G.setProgress(hk_g);
					skb_B.setProgress(hk_b);
					txt_title.setText("K&J Current state: Hotkey2");
					break;
				case R.id.btn_hk3:
					hk_r = Integer.parseInt(settings.getString("hotkey2_R", "0"));
					hk_g = Integer.parseInt(settings.getString("hotkey2_G", "0"));
					hk_b = Integer.parseInt(settings.getString("hotkey2_B", "0"));
					skb_R.setProgress(hk_r);
					skb_G.setProgress(hk_g);
					skb_B.setProgress(hk_b);
					txt_title.setText("K&J Current state: Hotkey3");
					break;
				case R.id.btn_set:
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
					SharedPreferences settings = (SharedPreferences) getActivity()
							.getSharedPreferences("Preference", 0);
					// 取得(R,G,B)
					String r = null, g = null, b = null;
					r = edt_R.getText().toString();
					g = edt_G.getText().toString();
					b = edt_B.getText().toString();
					// 存入數值
					settings.edit().putString("hotkey" + which + "_R", r).commit();
					settings.edit().putString("hotkey" + which + "_G", g).commit();
					settings.edit().putString("hotkey" + which + "_B", b).commit();
					txt_title.setText("K&J Current state: Hotkey" + (which + 1));
				}
			};
		};
		btn_hk1.setOnClickListener(HK_OCL);
		btn_hk2.setOnClickListener(HK_OCL);
		btn_hk3.setOnClickListener(HK_OCL);
		btn_set.setOnClickListener(HK_OCL);

		// 資料庫開起儲存
		OnClickListener Save_OCL = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.btn_save:
					int save_r = Integer.parseInt(edt_R.getText().toString());
					int save_g = Integer.parseInt(edt_G.getText().toString());
					int save_b = Integer.parseInt(edt_B.getText().toString());

					((MainActivity) getActivity()).mSaveDialog = new Dialog(getActivity());
					((MainActivity) getActivity()).mSaveDialog.setTitle(R.string.dialog_save_title);
					((MainActivity) getActivity()).mSaveDialog.setCancelable(false);
					((MainActivity) getActivity()).mSaveDialog.setContentView(R.layout.dialog_db_save);

					final EditText dialog_s_edt_name = (EditText) ((MainActivity) getActivity()).mSaveDialog
							.findViewById(R.id.dialog_s_edt_name);
					View dialog_s_view_color = (View) ((MainActivity) getActivity()).mSaveDialog
							.findViewById(R.id.dialog_s_view_color);
					TextView dialog_s_txt_rgb = (TextView) ((MainActivity) getActivity()).mSaveDialog
							.findViewById(R.id.dialog_s_txt_rgb);
					Button dialog_s_btn_confirm = (Button) ((MainActivity) getActivity()).mSaveDialog
							.findViewById(R.id.dialog_s_btn_confirm);
					Button dialog_s_btn_cancel = (Button) ((MainActivity) getActivity()).mSaveDialog
							.findViewById(R.id.dialog_s_btn_cancel);

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
								((MainActivity) getActivity()).mSaveDialog.dismiss();
							}
						}
					});
					dialog_s_btn_cancel.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							((MainActivity) getActivity()).mSaveDialog.dismiss();
						}
					});
					((MainActivity) getActivity()).mSaveDialog.show();
				}
			}
		};
		btn_save.setOnClickListener(Save_OCL);

		return rootView;
	}
}
