package com.ccweather.app.util;

import java.util.Iterator;

import android.text.TextUtils;

import com.ccweather.app.db.CCWeatherDB;
import com.ccweather.app.model.City;
import com.ccweather.app.model.County;
import com.ccweather.app.model.Province;

public class Utility {

	/**
	 * 解析与存储服务器返回的省份数据
	 * 
	 * @param db
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleProvincesResponse(CCWeatherDB db,
			String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					db.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析与存储服务器返回的城市数据
	 * 
	 * @param db
	 * @param response
	 * @param provinceId
	 * @return
	 */
	public static boolean handleCitiesResponse(CCWeatherDB db, String response,
			int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					db.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析与存储服务器返回的县数据
	 * @param db
	 * @param response
	 * @param cityId
	 * @return
	 */
	public static boolean handleCountiesResponse(CCWeatherDB db,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					db.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
