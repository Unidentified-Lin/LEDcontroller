package com.project.ledcontroller;

import static com.project.ledcontroller.DataBaseHelper.DATABASE_TABLE_1;
import static com.project.ledcontroller.DataBaseHelper.COLUMN_ID_1;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class DatabaseFragment extends Fragment {

	OnDataSelectedListener mCallback;

	// 作為容器的 Activity 必需實作這個界面
	public interface OnDataSelectedListener {
		public void onDataSelect(int position);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// 這裡確保容器 activity 有實作這個界面、否則丟出例外
		try {
			mCallback = (OnDataSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnDataSelectedListener");
		}
	}

	public DatabaseFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View dbView = inflater.inflate(R.layout.fragment_datalist, container, false);

		ListView list_data = (ListView) dbView.findViewById(R.id.list_data);
		list_data.setAdapter(((MainActivity) getActivity()).myAdapter);
		list_data.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				mCallback.onDataSelect(position);
			}
		});
		list_data.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
				deleteDialog.setTitle("Delete");
				deleteDialog.setIcon(android.R.drawable.ic_dialog_alert);
				deleteDialog.setCancelable(false); //關閉 Android 系統的主要功能鍵(menu,home等...)

				((MainActivity) getActivity()).DBhelper.openDataBase(getActivity());
				final String whereClause = COLUMN_ID_1 + " = ?";
				Log.i("onItemLongClick", whereClause);
				final String[] whereArgs = { ((MainActivity) getActivity()).idArray[position] };
				Log.i("onItemLongClick", whereArgs[0]);

				OnClickListener listener = new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case -1: //confirm
							Log.i("onItemLongClick", "confirm");
							((MainActivity) getActivity()).DBhelper.delete(DATABASE_TABLE_1, whereClause, whereArgs);
							((MainActivity) getActivity()).DBhelper.close();
							((MainActivity) getActivity()).settingChange();
							((MainActivity) getActivity()).actionBar.setSelectedNavigationItem(1);
							break;
						case -2: //cancel
							break;
						}
					}
				};
				deleteDialog.setNegativeButton("Cancel", listener);
				deleteDialog.setPositiveButton("Confirm", listener);
				deleteDialog.show();
				return false;
			}
		});

		return dbView;
	}
}
