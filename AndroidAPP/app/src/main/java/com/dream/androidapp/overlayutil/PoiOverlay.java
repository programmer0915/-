package com.dream.androidapp.overlayutil;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.search.poi.PoiResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于显示poi的overly
 */

public class PoiOverlay extends OverlayManager{
    private static final int MAX_POI_SIZE=10;
    private PoiResult mPoiResult=null;
    /**
     * 构造函数
     *该 PoiOverlay 引用的 BaiduMap 对象
     * @param baiduMap
     */
    public PoiOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置POI数据
     * @param poiResult
     */
    public void setData(PoiResult poiResult){
        this.mPoiResult=poiResult;
    }

    /**
     * 覆盖物列表
     * @return
     */
    @Override
    public List<OverlayOptions> getOverlayOptions() {
        if(mPoiResult==null||mPoiResult.getAllPoi()==null){
            return null;
        }
        List<OverlayOptions> markerList=new ArrayList<OverlayOptions>();
        int markerSize=0;
        for(int i=0;i<mPoiResult.getAllPoi().size()&&markerSize<MAX_POI_SIZE;i++){
            if(mPoiResult.getAllPoi().get(i).location==null){
                continue;
            }
            markerSize++;
            Bundle bundle=new Bundle();
            bundle.putInt("index",i);
            markerList.add(new MarkerOptions().icon(BitmapDescriptorFactory.fromAssetWithDpi("Icon_mark"
            +markerSize+".png")).extraInfo(bundle).position(mPoiResult.getAllPoi().get(i).location));
        }
        return markerList;
    }

    /**
     * 获取该PoiOverlay的poi数据
     * @return
     */
    public PoiResult getPoiResult(){
        return mPoiResult;
    }

    /**
     * 覆写此方法以改变默认点击行为
     * @param i
     * @return
     */
    public boolean onPoiClick(int i){
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!mOverlayList.contains(marker)){
            return false;
        }
        if(marker.getExtraInfo()!=null){
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
}
