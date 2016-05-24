package com.ccweather.app.activity;

import java.security.PublicKey;

import com.ccweather.app.R;
import com.ccweather.app.R.id;
import com.ccweather.app.R.layout;
import com.ccweather.app.R.menu;
import com.ccweather.app.util.HttpCallbackListener;
import com.ccweather.app.util.HttpUtil;
import com.ccweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {

	private LinearLayout mLayoutWeatherInfo;
	private TextView mTextCityName;
	private TextView mTextPublish;
	private TextView mTextWeatherDesp;
	private TextView mTextTemp1;
	private TextView mTextTemp2;
	private TextView mTextCurrentDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_weather);

		mLayoutWeatherInfo = (LinearLayout) findViewById(R.id.layout_weather_info);
		mTextCityName = (TextView) findViewById(R.id.text_city_name);
		mTextPublish = (TextView) findViewById(R.id.text_publish);
		mTextWeatherDesp = (TextView) findViewById(R.id.text_weather_desp);
		mTextTemp1 = (TextView) findViewById(R.id.text_temp1);
		mTextTemp2 = (TextView) findViewById(R.id.text_temp2);
		mTextCurrentDate = (TextView) findViewById(R.id.text_current_date);
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			mTextPublish.setText("同步中...");
			mLayoutWeatherInfo.setVisibility(View.INVISIBLE);
			mTextCityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
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
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
