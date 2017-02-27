package com.velue.distance;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.velue.distance.application.MyApplication;
import com.velue.distance.db.DistanceInfoDao;
import com.velue.distance.model.DistanceInfo;
import com.velue.distance.service.LocationService;
import com.velue.distance.utils.ConfirmDialog;
import com.velue.distance.utils.ConstantValues;
import com.velue.distance.utils.LogUtil;
import com.velue.distance.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //控件
    private TextView mTvDistance;
    private Button mButton;
    private TextView mLng_lat;
    private boolean isStart = true;

    private DistanceInfoDao mDistanceInfoDao;
    private volatile boolean isRefreshUI = true;
    private static final int REFRESH_TIME = 4000;   //4秒刷新一次

    private Handler refreshHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantValues.REFRESH_UI:
                    if (isRefreshUI) {
                        LogUtil.info(MainActivity.class, "refresh ui");
                        DistanceInfo mDistanceInfo = mDistanceInfoDao.getById(MyApplication.orderDealInfoId);
                        LogUtil.info(MainActivity.class, "界面刷新---> " + mDistanceInfo);
                        if (mDistanceInfo != null) {
                            mTvDistance.setText(String.valueOf(Utils.getValueWith2Suffix(mDistanceInfo.getDistance())));
                            mLng_lat.setText("经:" + mDistanceInfo.getLongitude() + " 纬:" + mDistanceInfo.getLatitude());
                            mTvDistance.invalidate();
                            mLng_lat.invalidate();
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private Timer refreshTimer = new Timer(true);
    private TimerTask refreshTask = new TimerTask() {
        @Override
        public void run() {
            if (isRefreshUI) {
                Message msg = refreshHandler.obtainMessage();
                msg.what = ConstantValues.REFRESH_UI;
                refreshHandler.sendMessage(msg);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, LocationService.class));
        Toast.makeText(this, "已启动定位服务...", Toast.LENGTH_SHORT).show();
        init();


        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }
        });

    }

    private void init() {
        mTvDistance = (TextView) findViewById(R.id.tv_drive_distance);
        mDistanceInfoDao = new DistanceInfoDao(this);
        refreshTimer.schedule(refreshTask, 0, REFRESH_TIME);
        mButton = (Button) findViewById(R.id.btn_start_drive);
        mLng_lat = (TextView) findViewById(R.id.longitude_Latitude);
    }


    @Override
    public void onClick(View v) {
//        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_start_drive:
                if (isStart) {
                    mButton.setBackgroundResource(R.drawable.btn_selected);
                    mButton.setText("结束计算");
                    isStart = false;
                    DistanceInfo mDistanceInfo = new DistanceInfo();
                    mDistanceInfo.setDistance(0f);  //距离初始值
                    mDistanceInfo.setLongitude(MyApplication.lng); //经度初始值
                    mDistanceInfo.setLatitude(MyApplication.lat);  //纬度初始值
                    int id = mDistanceInfoDao.insertAndGet(mDistanceInfo);
                    if (id != -1) {
                        MyApplication.orderDealInfoId = id;
                        Toast.makeText(this, "已开始计算...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "id is -1,无法执行距离计算代码块", Toast.LENGTH_SHORT).show();
                    }
                } else {

                            mButton.setBackgroundResource(R.drawable.btn_noselect);
                            mButton.setText("开始计算");
                            isStart = true;
                            //停止界面刷新
                            isRefreshUI = false;
                            if (refreshTimer != null) {
                                refreshTimer.cancel();
                                refreshTimer = null;
                            }
                            mDistanceInfoDao.delete(MyApplication.orderDealInfoId); //删除id对应记录
                            MyApplication.orderDealInfoId = -1; //停止定位计算
                            Toast.makeText(MainActivity.this, "已停止计算...", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
