package com.project.leccontroller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static String DATABASE_NAME = "LEDcontroller_db";
	private static int DATABASE_VERSON = 1;
	private SQLiteDatabase db;
	private DataBaseHelper DBhelper;

	public static final String DATABASE_TABLE_1 = "Color";
	public static final String COLUMN_ID_1 = "_id";
	public static final String COLUMN_NAME_1 = "color_name";
	public static final String COLUMN_RED_1 = "color_red";
	public static final String COLUMN_GREEN_1 = "color_green";
	public static final String COLUMN_BLUE_1 = "color_blue";

	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSON);
		// TODO Auto-generated constructor stub

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// 建立資料表 Create table.
		db.execSQL("CREATE TABLE  IF NOT EXISTS " + DATABASE_TABLE_1 + " ("
				+ COLUMN_ID_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COLUMN_NAME_1 + " TEXT NOT NULL, " + COLUMN_RED_1
				+ " INTEGER, " + COLUMN_GREEN_1 + " INTEGER, " + COLUMN_BLUE_1
				+ " INTEGER);"

		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public void openDataBase(Context context) throws SQLException {
		DBhelper = new DataBaseHelper(context);
		db = DBhelper.getWritableDatabase();
	}

	public Cursor select(String table, String[] columns, String selection,
			String[] selectionArgs, String orderBy) {
		/*
		 * SELECT [columns] 
		 * FROM [table] 
		 * WHERE [selection] = [selectionArgs]
		 * ORDERBY [orderBy]
		 */
		Cursor c = db.query(table, columns, selection, selectionArgs, null,
				null, orderBy);
		return c;
	}

	public void insert(String table, String[] columnsValue) {
		ContentValues values = new ContentValues();
		if (table == "Color") {
			String[] columns = { COLUMN_NAME_1, COLUMN_RED_1, COLUMN_GREEN_1,
					COLUMN_BLUE_1 };
			for (int i = 0; i < columns.length; i++) {
				values.put(columns[i], columnsValue[i]);
			}
		}
		db.insert(table, null, values);
	}

	public void update(String table, String[] columnsValue, String whereClause,
			String[] whereArgs) {
		try {
			ContentValues values = new ContentValues();
			if (table == "Color") {
				String[] columns = { COLUMN_NAME_1, COLUMN_RED_1,
						COLUMN_GREEN_1, COLUMN_BLUE_1 };
				for (int i = 0; i < columns.length; i++) {
					values.put(columns[i], columnsValue[i]);
				}
			}
			db.update(table, values, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void delete() {

	}

}
