package com.velue.distance.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.velue.distance.application.MyApplication;
import com.velue.distance.db.DistanceInfoDao;
import com.velue.distance.impl.DistanceComputeImpl;
import com.velue.distance.impl.DistanceComputeInterface;
import com.velue.distance.model.DistanceInfo;
import com.velue.distance.model.GpsLocation;
import com.velue.distance.utils.BDLocation2GpsUtil;
import com.velue.distance.utils.FileUtils;
import com.velue.distance.utils.LogUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by win7 on 2017/2/27.
 * 描述:
 * 作者:小智 win7
 */

public class LocationService extends Service {

    public static final String FILE_NAME = "log.txt";

    LocationClient mLocClient;
    private Object lock = new Object();
    private volatile GpsLocation prevGpsLocation = new GpsLocation();       //定位数据
    private volatile GpsLocation currentGpsLocation = new GpsLocation();
    private MyLocationListenner myListener = new MyLocationListenner();
    private volatile int discard = 1;
    private DistanceInfoDao mDistanceInfoDao;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDistanceInfoDao = new DistanceInfoDao(this);
        //LogUtil.info(LocationService.class, "Thread id ----------->:" + Thread.currentThread().getId());
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        //定位参数设置
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll"); //返回的定位结果是百度经纬度，默认值gcj02
        option.setAddrType("all");    //返回的定位结果包含地址信息
        option.setScanSpan(5000);     //设置发起定位请求的间隔时间为5000ms
        option.disableCache(true);    //禁止启用缓存定位
        option.setProdName("app.ui.activity");
        option.setOpenGps(true);
        option.setPriority(LocationClientOption.GpsFirst);  //设置GPS优先
        mLocClient.setLocOption(option);
        mLocClient.start();
        mLocClient.requestLocation();

    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mLocClient) {
            mLocClient.stop();
        }
        startService(new Intent(this, LocationService.class));
    }

    private class Task implements Callable<String> {

        private BDLocation location;

        public Task(BDLocation location) {
            this.location = location;
        }

        /**
         * 检测是否在原地不动
         *
         * @param distance
         * @return
         */
        private boolean noMove(float distance) {
            if (distance < 0.01) {
                return true;
            }
            return false;
        }

        /**
         * 检测是否在正确的移动
         *
         * @param distance
         * @return
         */
        private boolean checkProperMove(float distance) {
            if (distance <= 0.1 * discard) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 检测获取的数据是否是正常的
         *
         * @param location
         * @return
         */
        private boolean checkProperLocation(BDLocation location) {
            if (location != null && location.getLatitude() != 0 && location.getLongitude() != 0) {
                return true;
            }
            return false;
        }

        @Override
        public String call() throws Exception {
            synchronized (lock) {
                if (!checkProperLocation(location)) {
                    LogUtil.info(LocationService.class, "location data is null");
                    discard++;
                    return null;
                }

                if (MyApplication.orderDealInfoId != -1) {

                    DistanceInfo mDistanceInfo = mDistanceInfoDao.getById(MyApplication.orderDealInfoId);

                    LogUtil.info(LocationService.class, mDistanceInfo + "");
                    if (mDistanceInfo != null) {
                        LogUtil.info(LocationService.class, "行驶中......");
                        GpsLocation tempGpsLocation = BDLocation2GpsUtil.convertWithBaiduAPI(location);
                        if (tempGpsLocation != null) {
                            currentGpsLocation = tempGpsLocation;
                        } else {
                            discard++;
                        }
                        //日志
                        String logMsg = "(plat:--->" + prevGpsLocation.lat + "  plgt:--->" + prevGpsLocation.lng + ")\n" +
                                "(clat:--->" + currentGpsLocation.lat + "  clgt:--->" + currentGpsLocation.lng + ")";
                        LogUtil.info(LocationService.class, logMsg);

                        /** 计算距离  */
                        float distance = 0.0f;
                        DistanceComputeInterface distanceComputeInterface = DistanceComputeImpl.getInstance();
                        distance = (float) distanceComputeInterface.getLongDistance(prevGpsLocation.lat, prevGpsLocation.lng, currentGpsLocation.lat, currentGpsLocation.lng);
                        if (!noMove(distance)) {                //是否在移动
                            if (checkProperMove(distance)) {    //合理的移动
                                float drivedDistance = mDistanceInfo.getDistance();
                                mDistanceInfo.setDistance(distance + drivedDistance); //拿到数据库原始距离值， 加上当前值
                                mDistanceInfo.setLongitude(currentGpsLocation.lng);   //经度
                                mDistanceInfo.setLatitude(currentGpsLocation.lat);    //纬度

                                //日志记录
                                FileUtils.saveToSDCard(FILE_NAME, "移动距离--->:" + distance + drivedDistance + "\n" + "数据库中保存的距离" + mDistanceInfo.getDistance());

                                mDistanceInfoDao.updateDistance(mDistanceInfo);
                                discard = 1;
                            }
                        }
                        prevGpsLocation = currentGpsLocation;
                    }
                }
                return null;
            }
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            executor.submit(new Task(location));

            LogUtil.info(LocationService.class, "经度：" + location.getLongitude());
            LogUtil.info(LocationService.class, "纬度：" + location.getLatitude());
            if (MyApplication.lng <= 0 && MyApplication.lat <= 0) {
                MyApplication.lng = location.getLongitude();
                MyApplication.lat = location.getLatitude();
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }

}
