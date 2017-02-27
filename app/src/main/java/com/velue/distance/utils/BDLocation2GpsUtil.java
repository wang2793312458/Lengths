package com.velue.distance.utils;

import com.baidu.location.BDLocation;
import com.velue.distance.model.GpsLocation;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import it.sauronsoftware.base64.Base64;

public class BDLocation2GpsUtil {

    static BDLocation tempBDLocation = new BDLocation();     // 临时变量，百度位置
    static GpsLocation tempGPSLocation = new GpsLocation();  // 临时变量，gps位置

    public static enum Method{
        origin, correct;
    }

    private static final Method method = Method.correct;

    /**
     * 位置转换
     *
     * @param lBdLocation 百度位置
     * @return GPS位置
     */
    public static GpsLocation convertWithBaiduAPI(BDLocation lBdLocation) {
        switch (method) {
        case origin:    //原点
            GpsLocation location = new GpsLocation();
            location.lat = lBdLocation.getLatitude();
            location.lng = lBdLocation.getLongitude();
            return location;

        case correct:   //纠偏
            //同一个地址不多次转换
            if (tempBDLocation.getLatitude() == lBdLocation.getLatitude() && tempBDLocation.getLongitude() == lBdLocation.getLongitude()) {
                return tempGPSLocation;
            }
            String url = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4&"
                    + "x=" + lBdLocation.getLongitude() + "&y="
                    + lBdLocation.getLatitude();
            String result = executeHttpGet(url);
            LogUtil.info(BDLocation2GpsUtil.class, "result:" + result);
            if (result != null) {
                GpsLocation gpsLocation = new GpsLocation();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String lngString = jsonObj.getString("x");
                    String latString = jsonObj.getString("y");
                    // 解码
                    double lng = Double.parseDouble(new String(Base64.decode(lngString)));
                    double lat = Double.parseDouble(new String(Base64.decode(latString)));
                    // 换算
                    gpsLocation.lng = 2 * lBdLocation.getLongitude() - lng;
                    gpsLocation.lat = 2 * lBdLocation.getLatitude() - lat;
                    tempGPSLocation = gpsLocation;
                    LogUtil.info(BDLocation2GpsUtil.class, "result:" + gpsLocation.lat + "||" + gpsLocation.lng);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                tempBDLocation = lBdLocation;
                return gpsLocation;
            }else{
                LogUtil.info(BDLocation2GpsUtil.class, "百度API执行出错,url is:" + url);
                return null;
            }

        default:
            return null;
        }
    }

    private static String executeHttpGet(String requestUrl) {
        String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        try {
            url = new URL(requestUrl);
            connection = (HttpURLConnection) url.openConnection();
            //设置连接超时和读超时
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
