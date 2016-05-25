package com.ccweather.app.activity;

import java.security.PublicKey;

import com.ccweather.app.R;
import com.ccweather.app.service.AutoUpdateService;
import com.ccweather.app.util.HttpCallbackListener;
import com.ccweather.app.util.HttpUtil;
import com.ccweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "WeatherActivity";

	private LinearLayout mLayoutWeatherInfo;
	private TextView mTextCityName;
	private TextView mTextPublish;
	private TextView mTextWeatherDesp;
	private TextView mTextTemp1;
	private TextView mTextTemp2;
	private TextView mTextCurrentDate;
	
	private Button mBtnSwitch;
	private Button mBtnRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);

		mLayoutWeatherInfo = (LinearLayout) findViewById(R.id.layout_weather_info);
		mTextCityName = (TextView) findViewById(R.id.text_city_name);
		mTextPublish = (TextView) findViewById(R.id.text_publish);
		mTextCurrentDate = (TextView)findViewById(R.id.text_current_date);
		mTextWeatherDesp = (TextView) findViewById(R.id.text_weather_desp);
		mTextTemp1 = (TextView) findViewById(R.id.text_temp1);
		mTextTemp2 = (TextView) findViewById(R.id.text_temp2);
		mBtnSwitch = (Button) findViewById(R.id.button_switch_city);
		mBtnRefresh = (Button) findViewById(R.id.button_refresh);
		mBtnSwitch.setOnClickListener(this);
		mBtnRefresh.setOnClickListener(this);
		
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			Log.i(TAG, "county code not null");
			mTextPublish.setText("同步中...");
			mLayoutWeatherInfo.setVisibility(View.INVISIBLE);
			mTextCityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			Log.i(TAG, "county code null");
			showWeather();
		}
	}

	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				Log.i(TAG, "response is .."+response);
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Log.i(TAG, "handle weather response");
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Log.i(TAG, "showWeather");
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mTextPublish.setText("同步失败");
					}
				});
			}
		});
	}

	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mTextCityName.setText(preferences.getString("city_name", ""));
		mTextTemp1.setText(preferences.getString("temp1", ""));
		mTextTemp2.setText(preferences.getString("temp2", ""));
		mTextWeatherDesp.setText(preferences.getString("weather_desp", ""));
		mTextPublish.setText("今天" + preferences.getString("publish_time", "")
				+ "发布");
		mTextCurrentDate.setText(preferences.getString("current_date", ""));
		mLayoutWeatherInfo.setVisibility(View.VISIBLE);
		mTextCityName.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
 
 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.button_refresh:
			mTextPublish.setText("同步中...");
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = preferences.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
}
