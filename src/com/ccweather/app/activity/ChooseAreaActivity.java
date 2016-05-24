package com.ccweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.ccweather.app.R;
import com.ccweather.app.db.CCWeatherDB;
import com.ccweather.app.model.City;
import com.ccweather.app.model.County;
import com.ccweather.app.model.Province;
import com.ccweather.app.util.HttpCallbackListener;
import com.ccweather.app.util.HttpUtil;
import com.ccweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	private static final String TAG = "ChooseAreaActivity";
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog mProgressDialog;
	private TextView mTextTitle;
	private ListView mListView;
	private ArrayAdapter<String> mAdapter;
	private CCWeatherDB mDb;
	private List<String> mList = new ArrayList<String>();

	private List<Province> mProvinceList;
	private List<City> mCityList;
	private List<County> mCountyList;

	private Province mSelectedProvince;
	private City mSelectedCity;
	private int mCurrentLevel;

	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_activity", false);
 
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (preferences.getBoolean("city_selected", false)
				&& !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_area);
		mTextTitle = (TextView) findViewById(R.id.text_title);
		mListView = (ListView) findViewById(R.id.list_view);
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mList);
		mListView.setAdapter(mAdapter);
		mDb = CCWeatherDB.getInstance(this);
		mListView.setOnItemClickListener(new OnItemClickListener() {
  
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mCurrentLevel == LEVEL_PROVINCE) {
					mSelectedProvince = mProvinceList.get(position);
					queryCities();
				} else if (mCurrentLevel == LEVEL_CITY) {
					mSelectedCity = mCityList.get(position);
					queryCounties();
				} else if (mCurrentLevel == LEVEL_COUNTY) {
					Log.i(TAG, "level is county level");
					String countyCode = mCountyList.get(position)
							.getCountyCode();
					Log.i(TAG, "countyCode is "+countyCode);
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				} 
			}
		});
		queryProvinces();
	}

	/**
	 * 查询省，优先从数据库查询，若没有从服务器查询
	 */
	private void queryProvinces() {
		mProvinceList = mDb.loadProvinces();
		if (mProvinceList.size() > 0) {
			mList.clear();
			for (Province province : mProvinceList) {
				mList.add(province.getProvinceName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTextTitle.setText("中国");
			mCurrentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * 查询市，优先从数据库查询，若没有从服务器查询
	 */
	private void queryCities() {
		mCityList = mDb.loadCities(mSelectedProvince.getId());
		if (mCityList.size() > 0) {
			mList.clear();
			for (City city : mCityList) {
				mList.add(city.getCityName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTextTitle.setText(mSelectedProvince.getProvinceName());
			mCurrentLevel = LEVEL_CITY;
		} else {
			queryFromServer(mSelectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * 查询县，优先从数据库查询，若没有从服务器查询
	 */
	private void queryCounties() {
		mCountyList = mDb.loadCounties(mSelectedCity.getId());
		if (mCountyList.size() > 0) {
			mList.clear();
			for (County county : mCountyList) {
				mList.add(county.getCountyName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			mTextTitle.setText(mSelectedCity.getCityName());
			mCurrentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(mSelectedCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(mDb, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(mDb, response,
							mSelectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(mDb, response,
							mSelectedCity.getId());
				}
				if (result) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("正在加载...");
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
		mProgressDialog.show();
	}

	private void closeProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		if (mCurrentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (mCurrentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
