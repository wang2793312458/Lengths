package com.velue.distance.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.velue.distance.model.DistanceInfo;
import com.velue.distance.utils.LogUtil;


public class DistanceInfoDao {
    private DBOpenHelper helper;
    private SQLiteDatabase db;

    public DistanceInfoDao(Context context) {
        helper = new DBOpenHelper(context);
    }

    public void insert(DistanceInfo mDistanceInfo) {
        if (mDistanceInfo == null) {
            return;
        }
        db = helper.getWritableDatabase();
        String sql = "INSERT INTO milestone(distance,longitude,latitude) VALUES('"+ mDistanceInfo.getDistance() + "','"+ mDistanceInfo.getLongitude() + "','"+ mDistanceInfo.getLatitude() + "')";
        LogUtil.info(DistanceInfoDao.class, sql);
        db.execSQL(sql);
        db.close();
    }

    public int getMaxId() {
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(id) as id from milestone",null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("id"));
        }
        return -1;
    }

    /**
     * 添加数据
     * @param orderDealInfo
     * @return
     */
    public synchronized int insertAndGet(DistanceInfo mDistanceInfo) {
        int result = -1;
        insert(mDistanceInfo);
        result = getMaxId();
        return result;
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    public DistanceInfo getById(int id) {
        db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * from milestone WHERE id = ?",new String[] { String.valueOf(id) });
        DistanceInfo mDistanceInfo = null;
        if (cursor.moveToFirst()) {
            mDistanceInfo = new DistanceInfo();
            mDistanceInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
            mDistanceInfo.setDistance(cursor.getFloat(cursor.getColumnIndex("distance")));
            mDistanceInfo.setLongitude(cursor.getFloat(cursor.getColumnIndex("longitude")));
            mDistanceInfo.setLatitude(cursor.getFloat(cursor.getColumnIndex("latitude")));
        }
        cursor.close();
        db.close();
        return mDistanceInfo;
    }

    /**
     * 更新距离
     * @param orderDealInfo
     */
    public void updateDistance(DistanceInfo mDistanceInfo) {
        if (mDistanceInfo == null) {
            return;
        }
        db = helper.getWritableDatabase();
        String sql = "update milestone set distance="+ mDistanceInfo.getDistance() +",longitude="+mDistanceInfo.getLongitude()+",latitude="+mDistanceInfo.getLatitude()+" where id = "+ mDistanceInfo.getId();
        LogUtil.info(DistanceInfoDao.class, sql);
        db.execSQL(sql);
        db.close();
    }


    /**
     * 删除对应记录
     * @param id
     */
    public void delete(int id) {
        if (id == 0 || id < 0) {
            return;
        }
        db = helper.getWritableDatabase();
        String sql = "delete from milestone where id = " + id;
        LogUtil.info(DistanceInfoDao.class, sql);
        db.execSQL(sql);
        db.close();
    }
}
