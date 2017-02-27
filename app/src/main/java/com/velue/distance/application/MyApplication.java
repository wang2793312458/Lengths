package com.velue.distance.application;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;

/**
 * Created by win7 on 2017/2/27.
 * 描述:
 * 作者:小智 win7
 */

public class MyApplication extends Application {

    public static int orderDealInfoId = -1;
    public static double lng = 0d;
    public static double lat = 0d;
    public static String driverid = null;
    private static MyApplication mInstance = null;
    public boolean m_bKeyRight = true;
    public BMapManager mBMapManager = null;
    public static final String strKey = "4QeHfgLpzpM0ZwqBnyvwDpyGFEWpU2KN";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initEngineManager(this);
    }

    @Override
    //在app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
    public void onTerminate() {
        if (mBMapManager != null) {
            mBMapManager.destroy();
            mBMapManager = null;
        }
        super.onTerminate();
    }

    /**
     * 初始化BMapManager
     *
     * @param context
     */
    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(strKey, new MyGeneralListener())) {
            Toast.makeText(MyApplication.getInstance().getApplicationContext(),
                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
        }
    }

    public static MyApplication getInstance() {
        return mInstance;
    }


    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), "您的网络出错啦！",
                        Toast.LENGTH_LONG).show();
            } else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                Toast.makeText(MyApplication.getInstance().getApplicationContext(), "输入正确的检索条件！",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onGetPermissionState(int iError) {
            if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
                //授权Key错误：
//                Toast.makeText(MyApplication.getInstance().getApplicationContext(), "请输入正确的授权Key！", Toast.LENGTH_LONG).show();
                MyApplication.getInstance().m_bKeyRight = false;
            }
        }
    }

}
