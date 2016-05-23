package com.ccweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.ccweather.app.model.City;
import com.ccweather.app.model.County;
import com.ccweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CCWeatherDB {

	// 数据库名
	public static final String DBNAME = "ccweather";

	public static final String PROVINCE_TABLE = "Province";
	public static final String CITY_TABLE = "City";
	public static final String COUNTY_TABLE = "County";
	// 数据库版本
	public static final int VERSION = 1;
	private static CCWeatherDB ccWeatherDB;
	private SQLiteDatabase db;

	// 构造方法私有
	private CCWeatherDB(Context context) {
		CCWeatherOpenHelper dbHelper = new CCWeatherOpenHelper(context, DBNAME,
				null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	// 获取实例
	public synchronized static CCWeatherDB getInstance(Context context) {
		if (ccWeatherDB == null) {
			ccWeatherDB = new CCWeatherDB(context);
		}
		return ccWeatherDB;
	}

	// 将province实例存储到数据库
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("province_name", province.getProvinceName());
			contentValues.put("province_code", province.getProvinceCode());
			db.insert(PROVINCE_TABLE, null, contentValues);
		}
	}

	// 读取所有省份信息
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query(PROVINCE_TABLE, null, null, null, null, null,
				null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	// 将city实例存储到数据库
	public void saveCity(City city) {
		if (city != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("city_name", city.getCityName());
			contentValues.put("city_code", city.getCityCode());
			contentValues.put("province_id", city.getProvinceId());
			db.insert(CITY_TABLE, null, contentValues);
		}
	}

	// 读取某省份的所有城市信息
	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query(CITY_TABLE, null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	// 将county实例存储到数据库
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("county_name", county.getCountyName());
			contentValues.put("county_code", county.getCountyCode());
			contentValues.put("city_id", county.getCityId());
			db.insert(COUNTY_TABLE, null, contentValues);
		}
	}

	// 读取某城市的所有县信息
	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query(COUNTY_TABLE, null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
}
