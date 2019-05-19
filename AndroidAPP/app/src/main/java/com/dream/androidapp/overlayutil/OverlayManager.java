package com.dream.androidapp.overlayutil;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类提供了一个能够显示和管理多个Overlay的基类
 */

public abstract class OverlayManager implements BaiduMap.OnMarkerClickListener,BaiduMap.OnPolylineClickListener{

    BaiduMap mBaiduMap=null;
    private List<OverlayOptions> mOverlayOptionList=null;

    List<Overlay> mOverlayList=null;

    /**
     * 通过一个BaiduMap 对象构造
     * @param baiduMap
     */
    public OverlayManager(BaiduMap baiduMap){
        mBaiduMap=baiduMap;
        if(mOverlayOptionList==null){
            mOverlayOptionList=new ArrayList<OverlayOptions>();
        }
        if(mOverlayList==null){
            mOverlayList=new ArrayList<Overlay>();
        }
    }

    /**
     * 复写此方法设置要管理的Overlay列表
     * @return 管理的Overlay列表
     */
    public abstract List<OverlayOptions> getOverlayOptions();

    /**
     * 将所有Overlay 添加到地图上
     */
    public final void addToMap(){
        if(mBaiduMap==null){
            return;
        }
        removeFromMap();
        List<OverlayOptions> overlayOptions=getOverlayOptions();
        if(overlayOptions!=null){
            mOverlayOptionList.addAll(getOverlayOptions());
        }
        for(OverlayOptions options:mOverlayOptionList){
            mOverlayList.add(mBaiduMap.addOverlay(options));
        }

    }
    public final void removeFromMap(){
        if(mBaiduMap==null){
            return;
        }
        for(Overlay marker:mOverlayList){
            marker.remove();
        }
        mOverlayOptionList.clear();
        mOverlayList.clear();
    }

    /**
     * 缩放地图，使所有Overlay都在合适的视野内
     */
    public void zoomToSpan(){
        if(mBaiduMap==null){
            return;
        }
        if(mOverlayList.size()>0){
            LatLngBounds.Builder builder=new LatLngBounds.Builder();
            for(Overlay overlay:mOverlayList){
                //polyline中的点可能太多，只按marker缩放
                if(overlay instanceof Marker){
                    builder.include(((Marker) overlay).getPosition());
                }
            }
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));

        }
    }
}
