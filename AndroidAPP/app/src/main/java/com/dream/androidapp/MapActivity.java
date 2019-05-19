package com.dream.androidapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.dream.androidapp.overlayutil.PoiOverlay;

import java.util.Map;

//定位功能
public class MapActivity extends Activity {
    //初始化控件
    private EditText et_text;
    private Button bt_button;
    private boolean isFirstLocation = true;  //防止每次定位都重新设置中心点和marker
    private MapView mMapView;// 定义百度地图控件
    private BaiduMap mBaiduMap;//定义地图实例
    private LocationClient mLocationClient;// 定位SDK核心类
    private MyLocationListenner myListener = new MyLocationListenner();// 定位监听
    private int radius = 1000;//设置周边范围
    private PoiSearch mPoiSearch;
    private SuggestionSearch mSuggestionSearch;
    // -----------------onCreate-------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        initView();
        initLoaction();
        mLocationClient.start();

    }


    public void initLoaction() {
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(myListener);
        initLoactionOption();
    }

    private void initLoactionOption() {
        LocationClientOption mOption = new LocationClientOption();
        mOption.setCoorType("bd09ll");
        mOption.setIsNeedAddress(true);
        mOption.setOpenGps(true);
        mOption.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(mOption);
    }

    public class MyLocationListenner extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                if (isFirstLocation) {
                    isFirstLocation = false;
                    SharedPreferences location = getSharedPreferences("location", MODE_PRIVATE);
                    SharedPreferences.Editor editor = location.edit();
                    editor.putString("latitude",bdLocation.getLatitude()+"");
                    editor.putString("longitude",bdLocation.getLongitude()+"");
                    editor.putString("city",bdLocation.getCity());
                    editor.apply();

                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(bdLocation.getRadius())
                            .direction(bdLocation.getRadius())
                            .latitude(bdLocation.getLatitude())
                            .longitude(bdLocation.getLongitude())
                            .build();
                    mBaiduMap.setMyLocationData(locData);
                    LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                    mLocationClient.stop();
            }
        }
    }
            //地图初始化
            private void initView () {
                // 初始化关键词输入框和button控件
                et_text = (EditText) findViewById(R.id.et_text);
                bt_button = (Button) findViewById(R.id.bt_button);
                bt_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                public void onClick(View view) {
                        mPoiSearch = PoiSearch.newInstance();
                        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
                        searchNearby();
                    }
                });
                //获取百度地图控件
                mMapView = findViewById(R.id.mMapView);
                //获取地图实例
                mBaiduMap = mMapView.getMap();
                // 开启定位图层
                mBaiduMap.setMyLocationEnabled(true);
            }
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){

        public void onGetPoiResult(PoiResult result) {
            //获取POI检索结果
            if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                Toast.makeText(MapActivity.this, "未找到结果",
                        Toast.LENGTH_LONG).show();
            }
            else if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                mBaiduMap.clear();
                PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result);
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    private void searchNearby() {
        String value = et_text.getText().toString();
        SharedPreferences location = getSharedPreferences("location", MODE_PRIVATE);
        String latitude = location.getString("latitude","");
        String longitude = location.getString("longitude","");
        double dblatitude = Double.valueOf(latitude);
        double dblongitude = Double.valueOf(longitude);
        LatLng latLng  = new LatLng(dblatitude, dblongitude);
        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                .radius(1000)
                .keyword(value)
                .location(latLng));
    }
    private class MyPoiOverlay extends PoiOverlay {

        private MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override

        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            return true;
        }
    }

}