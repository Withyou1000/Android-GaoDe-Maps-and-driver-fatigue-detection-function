package com.example.myapplication;

import static com.amap.api.maps.MapsInitializer.setTerrainEnable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.amap.api.navi.NaviSetting;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;
import com.example.myapplication.Adapter.MovePoiAdapter;
import com.example.myapplication.Adapter.SearchPoiAdapter;
import com.example.myapplication.Class.Bean;
import com.example.myapplication.Class.MyMarker;
import com.example.myapplication.Class.TestDialogFragment;
import com.example.myapplication.Method.MapMarkDBHelper;
import com.example.myapplication.Method.Show;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.martin.ads.omoshiroilib.debug.teststmobile.MultitrackerActivity;

import java.util.ArrayList;
import java.util.List;


public class GaoDEMap extends AppCompatActivity implements TextWatcher, Inputtips.InputtipsListener, LocationSource, AMapLocationListener, View.OnClickListener, PoiSearch.OnPoiSearchListener, CompoundButton.OnCheckedChangeListener, DistanceSearch.OnDistanceSearchListener, GeocodeSearch.OnGeocodeSearchListener, TestDialogFragment.DialogListener {

    private MapView mMapView = null;//地图容器
    private AMap aMap;//地图对象AMap


    private Inputtips inputtips;
    private GeocodeSearch geocoderSearch;
    private SearchPoiAdapter searchPoiAdapter;
    private MovePoiAdapter movePoiAdapter;
    private ScrollingView recyclerView;
    private TextView et_input;
    private ArrayAdapter<Tip> ada;
    private ArrayAdapter<String> stringArrayAdapter;
    private ArrayAdapter<String> ada1;
    private RecyclerView rv_show1;
    private AMapNavi navi;
    private ScrollView sv_show;
    private TextView poi_name;
    public static TextView poi_adress;
    private TextView poi_pos;
    private Tip tip_instance;
    private Marker marker_pos;
    private ImageView image;
    private MyLocationStyle myLocationStyle;
    public AMapLocationClient mlocationClient = null;
    //声明定位回调监听器
    private OnLocationChangedListener mListener;
    private ImageButton ib_target;
    boolean chosed = false;
    boolean ischeck = false;
    boolean iseye = true;
    boolean is_geo = true;
    private ImageView iv_target;
    private TextView tv_target_pos;
    private TextView tv_cur_pos;
    private PoiSearch poiSearch;
    private String address;
    private Switch aSwitch;
    private String cityCode;
    private ImageView iv_poi_pic;
    private DistanceSearch distanceSearch;
    private LatLonPoint mypoint;
    private TextView tv_poi_distance;
    private String city;
    private LatLng add_target;
    private ActivityResultLauncher<Intent> register;
    private Marker mark_target;
    private Button bt_add;
    private ImageButton ib_add;
    private MapMarkDBHelper helper;
    private ImageButton ib_update;
    private RadioButton rb_eye;
    private ListView rv_show2;
    private final List<Bean> beanList = new ArrayList<>();
    private Bean selectBean;
    private TestDialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        //获取权限
        get_power();
        setTerrainEnable(false);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gao_demap);
        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent intent = result.getData();
                if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
                    String name = intent.getStringExtra("name");
                    if (name.equals("delete")) {
                        mark_target.remove();
                        mMapView.invalidate();
                        return;
                    }
                    mark_target.setTitle(intent.getStringExtra("name"));
                }
            }
        });

        helper = MapMarkDBHelper.getInstance(this);
        helper.openWrite();
        helper.openRead();
        mMapView = (MapView) findViewById(R.id.map);
        //设置输入提示功能
        poi_name = findViewById(R.id.tv_poi_name);
        poi_pos = findViewById(R.id.tv_poi_pos);
        rv_show1 = findViewById(R.id.rv_show1);
        rv_show2 = findViewById(R.id.rv_show2);
        et_input = findViewById(R.id.et_input);
        poi_adress = findViewById(R.id.tv_poi_address);
        ib_target = findViewById(R.id.ib_target);
        iv_target = findViewById(R.id.iv_target);
        tv_target_pos = findViewById(R.id.tv_target_pos);
        tv_cur_pos = findViewById(R.id.tv_cur_pos);
        ib_target.setOnClickListener(this);
        aSwitch = findViewById(R.id.ac_switch_test);
        aSwitch.setOnCheckedChangeListener(this);
        /* iv_poi_pic = findViewById(R.id.iv_poi_pic);*/
        tv_poi_distance = findViewById(R.id.tv_poi_distance);
        ib_add = findViewById(R.id.ib_add_pos);
        rb_eye = findViewById(R.id.rb_eye);
        rb_eye.setOnClickListener(this);
        MapsInitializer.updatePrivacyShow(this, true, true);//隐私合规接口
        MapsInitializer.updatePrivacyAgree(this, true);//隐私合规接口
        AMapLocationClient.updatePrivacyAgree(this, true);
        AMapLocationClient.updatePrivacyShow(this, true, true);
        NaviSetting.updatePrivacyShow(this, true, true);
        NaviSetting.updatePrivacyAgree(this, true);

        et_input.addTextChangedListener(this);
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        //将蓝色圈圈消除
        myLocationStyle.getMyLocationType();
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));

        aMap.addOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
                aMap.setMyLocationStyle(myLocationStyle);
            }
        });
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setLogoBottomMargin(-50);//隐藏logo
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        AMap.OnInfoWindowClickListener listener = new AMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker arg0) {
                mark_target = arg0;
                Intent intent = new Intent(GaoDEMap.this, AddPos.class);
                intent.putExtra("name", arg0.getTitle());
                register.launch(intent);
            }
        };
//绑定信息窗点击事件
        aMap.setOnInfoWindowClickListener(listener);
        //搜索监听
        poiSearch = null;
        try {
            poiSearch = new PoiSearch(this, null);
        } catch (com.amap.api.services.core.AMapException e) {
            throw new RuntimeException(e);
        }
        poiSearch.setOnPoiSearchListener(this);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        /*  aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);*/

        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                add_target = cameraPosition.target;
                if (chosed == true) {
                    String a = "(目标) " + "纬度：" + cameraPosition.target.latitude + "  经度：" + cameraPosition.target.longitude;
                    tv_target_pos.setText("(准心) " + "纬度：" + cameraPosition.target.latitude + "  经度：" + cameraPosition.target.longitude);

                }
                float zoomLevel = aMap.getCameraPosition().zoom;

                // 根据缩放级别显示 Marker
                if (zoomLevel > 17) {

                }
            }


            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                if (is_geo) {
                    if (movePoiAdapter != null) {
                        movePoiAdapter.setSelectPosition(0);
                    }
                    try {
                        getGeocodeSearch(cameraPosition.target);
                    } catch (AMapException e) {
                        throw new RuntimeException(e);
                    }
                    rv_show2.smoothScrollToPosition(0);
                }
                is_geo = true;
            }
        });

        movePoiAdapter = new MovePoiAdapter(beanList, this);
        rv_show2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                movePoiAdapter.setSelectPosition(position);
                movePoiAdapter.notifyDataSetChanged();
                selectBean = beanList.get(position);
                is_geo = false;//当是点击地址条目触发的地图移动时，不进行逆地理解析
                aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(selectBean.getLatitude(), selectBean.getLongitude()))); //设置地图中心点
            }
        });
        rv_show2.setAdapter(movePoiAdapter);
        rv_show1.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        searchPoiAdapter = new SearchPoiAdapter(this, new ArrayList<>());
        searchPoiAdapter.setItemClickListener(new SearchPoiAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(int position, Tip tip) {
                if (marker_pos != null) {
                    marker_pos.remove();
                }
                tip_instance = tip;
                LatLonPoint point = tip.getPoint();
                //隐藏搜索提示列表
                rv_show1.setVisibility(View.GONE);
                //回调onPoiItemSearched
                poiSearch.searchPOIIdAsyn(tip.getPoiID());// 异步搜索
                MarkerOptions markerOption = new MarkerOptions();

                poi_name.setText(tip.getName());
                address = tip.getAddress();
                poi_adress.setText("地址：" + address);

                //防止空值获取经纬度时导致闪退
                if (null != tip.getPoint()) {
                    //移动镜头
                    aMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(point.getLatitude(), point.getLongitude())));
                    //设置标点位置
                    markerOption.position(new LatLng(point.getLatitude(), point.getLongitude()));
                    //显示经纬度
                    String s = "纬度：" + tip.getPoint().getLatitude() + "  " + "经度：" + tip_instance.getPoint().getLongitude();
                    poi_pos.setText(s);
                    //测量距离
                    DistanceSearch.DistanceQuery distanceQuery = new DistanceSearch.DistanceQuery();
                    distanceQuery.setDestination(tip.getPoint());
                    List<LatLonPoint> latLonPoints = new ArrayList<LatLonPoint>();
                    latLonPoints.add(mypoint);
                    distanceQuery.setOrigins(latLonPoints);
                    //设置测量方式，驾车
                    distanceQuery.setType(DistanceSearch.TYPE_DISTANCE);
                    //回调
                    distanceSearch.calculateRouteDistanceAsyn(distanceQuery);
                } else {
                    poi_adress.setText("地址：该位置没有信息");
                    poi_pos.setText("error");
                    tv_poi_distance.setText("");
                }
                //添加marker
                markerOption.title(tip.getName()).snippet(tip.getPoiID());
                markerOption.draggable(false);//设置Marker可拖动
                markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                        .decodeResource(getResources(), R.mipmap.location)));
                marker_pos = aMap.addMarker(markerOption);

            }
        });
        rv_show1.setAdapter(searchPoiAdapter);
        rv_show1.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        inputtips = new Inputtips(this, (InputtipsQuery) null);
        inputtips.setInputtipsListener(this);

        try {
            navi = AMapNavi.getInstance(this);
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        navi.setUseInnerVoice(true, false);

        try {
            distanceSearch = new DistanceSearch(this);
        } catch (com.amap.api.services.core.AMapException e) {
            throw new RuntimeException(e);
        }

        //初始化数据库里的marker
        init_mark();

        distanceSearch.setDistanceSearchListener(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 逆地理编码获取当前位置信息
     **/
    private void getGeocodeSearch(LatLng target) throws AMapException {
        if (geocoderSearch == null) {
            try {
                geocoderSearch = new GeocodeSearch(this);
            } catch (com.amap.api.services.core.AMapException e) {
                throw new RuntimeException(e);
            }
            geocoderSearch.setOnGeocodeSearchListener(this);
        }
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(target.latitude, target.longitude), 1000, GeocodeSearch.AMAP);
        query.setExtensions("all");
        geocoderSearch.getFromLocationAsyn(query);
    }

    public void get_power() {
        XXPermissions.with(this)
                /* // 申请单个权限
                 .permission(Permission.RECORD_AUDIO)*/
                // 申请多个权限
                .permission(Permission.ACCESS_FINE_LOCATION, Permission.CAMERA, Permission.CALL_PHONE)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                .unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            Show.show(GaoDEMap.this, "获取部分权限成功，但部分权限未正常授予,请手动设置");
                            return;
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            Show.show(GaoDEMap.this, "被拒绝授权，请手动授予权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(GaoDEMap.this, permissions);
                        } else {
                            Show.show(GaoDEMap.this, "获取权限失败");
                        }
                    }
                });
    }

    public void init_mark() {
        List<MyMarker> list;
        list = helper.queryall();
        for (MyMarker marker : list) {
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.position(new LatLng(marker.lat, marker.lon));
            markerOption.draggable(false);//设置Marker可拖动
            markerOption.title(marker.name);
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.flag);
// 指定新的宽度和高度
            int newWidth = 100;
            int newHeight = 100;
// 调整大小后的 Bitmap 对象
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            markerOption.icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
            aMap.addMarker(markerOption);
        }

    }

    public void goto_add(View view) {
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(add_target);
        markerOption.draggable(false);//设置Marker可拖动
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.flag);
// 指定新的宽度和高度
        int newWidth = 100;
        int newHeight = 100;
// 调整大小后的 Bitmap 对象
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
        mark_target = aMap.addMarker(markerOption);
        double lat = add_target.latitude;
        double lon = add_target.longitude;
        Intent intent = new Intent(this, AddPos.class);
        intent.putExtra("lat", lat);
        intent.putExtra("lon", lon);
        register.launch(intent);
    }

    public void goto_weather(View view) {
        if (tip_instance == null) {
            poi_name.setText("亲，还没有选择地点哦");
            return;
        }
    }

    public void goto_sleep(View view) {
        Intent intent = new Intent(this, MultitrackerActivity.class);
        startActivity(intent);
    }

    public void goto_navi(View view) {
        if (tip_instance == null) {
            poi_name.setText("亲，还没有选择地点哦");
            return;
        }
        if (dialogFragment == null) {
            dialogFragment = new TestDialogFragment();
//            dialogFragment.setCancelable(false);
        }
        dialogFragment.show(getSupportFragmentManager(), "dialogFragment");

      /*  LatLonPoint point = tip_instance.getPoint();
        Poi poi = new Poi(tip_instance.getName(), new LatLng(point.getLatitude(), point.getLongitude()), tip_instance.getPoiID());
        AmapNaviParams params = new AmapNaviParams(null, null, poi, AmapNaviType.RIDE, AmapPageType.ROUTE);
        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, null);*/
    }

    public void goto_zhishi(View view) {
        Intent intent = new Intent(this, ZhiShiActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        helper.closelink();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        aMap.setMyLocationStyle(myLocationStyle);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (et_input.getText().toString().isEmpty()) {
            rv_show1.setVisibility(View.GONE);
        } else {
            rv_show1.setVisibility(View.VISIBLE);
        }
        if (ischeck == true) {
            InputtipsQuery query = new InputtipsQuery(String.valueOf(s), cityCode);
            query.setCityLimit(true);
            inputtips.setQuery(query);
        } else {
            InputtipsQuery query = new InputtipsQuery(String.valueOf(s), null);
            inputtips.setQuery(query);
        }
        inputtips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        searchPoiAdapter.SetData(list);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            try {
                mlocationClient = new AMapLocationClient(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //初始化定位参数
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setOnceLocationLatest(true);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }

    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    //定位回调  在回调方法中调用“mListener.onLocationChanged(amapLocation);”可以在地图上显示系统小蓝点。
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                mypoint = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                cityCode = aMapLocation.getCityCode();
                tv_cur_pos.setText("我的位置: " + aMapLocation.getProvince() + aMapLocation.getCity() + aMapLocation.getDistrict()
                        + aMapLocation.getStreet() + aMapLocation.getStreetNum() + aMapLocation.getDescription());

            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("定位AmapErr", errText);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_target) {
            if (chosed == false) {
                rv_show2.setVisibility(View.VISIBLE);
                ib_add.setVisibility(View.VISIBLE);
                iv_target.setVisibility(View.VISIBLE);
                ib_target.setBackground(getResources().getDrawable(R.drawable.target_chose, null));
                chosed = true;
            } else {
                rv_show2.setVisibility(View.GONE);
                ib_add.setVisibility(View.GONE);
                iv_target.setVisibility(View.GONE);
                ib_target.setBackground(getResources().getDrawable(R.drawable.target_normal, null));
                chosed = false;
            }
        }
        if (v.getId() == R.id.rb_eye) {
            if (iseye == true) {
                List<Marker> markers = aMap.getMapScreenMarkers();
                rb_eye.setBackground(getResources().getDrawable(R.drawable.eye_open, null));
                for (Marker marker : markers) {
                    marker.setVisible(false);
                }
                iseye = false;
            } else {
                List<Marker> markers = aMap.getMapScreenMarkers();
                rb_eye.setBackground(getResources().getDrawable(R.drawable.eye_close, null));
                for (Marker marker : markers) {
                    marker.setVisible(true);
                }
                iseye = true;
            }
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        city = poiItem.getCityName();
        poi_adress.setText("地址：" + poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getAdName() + address + "  " + poiItem.getPoiExtension().getOpentime());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.ac_switch_test && isChecked == true) {
            ischeck = true;
        } else {
            ischeck = false;
        }
    }

    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int i) {
        List<DistanceItem> results = distanceResult.getDistanceResults();
        float distance = results.get(0).getDistance();
        tv_poi_distance.setText("直线距离：" + String.format("%.2f", distance / 1000.00) + "公里");
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i != 1000) return;
        beanList.clear();
        List<PoiItem> poiItems = regeocodeResult.getRegeocodeAddress().getPois();
        beanList.add(new Bean("当前位置", regeocodeResult.getRegeocodeAddress().getFormatAddress(), regeocodeResult.getRegeocodeQuery().getPoint().getLatitude(), regeocodeResult.getRegeocodeQuery().getPoint().getLongitude()));
        if (poiItems.size() != 0) {
            for (PoiItem poiItem : poiItems) {
                beanList.add(new Bean(poiItem.getTitle(), poiItem.getSnippet(), poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude()));
            }
            movePoiAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onDialogWalkClick() {
        LatLonPoint point = tip_instance.getPoint();
        Poi poi = new Poi( tip_instance.getName(),new LatLng(point.getLatitude(), point.getLongitude()),tip_instance.getPoiID());
        AmapNaviParams params = new AmapNaviParams(null, null, poi, AmapNaviType.RIDE, AmapPageType.ROUTE);
        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, null);
    }

    @Override
    public void onDialogCarClick() {
        LatLonPoint point = tip_instance.getPoint();
        Poi poi = new Poi(tip_instance.getName(), new LatLng(point.getLatitude(), point.getLongitude()), tip_instance.getPoiID());
        AmapNaviParams params = new AmapNaviParams(null, null, poi, AmapNaviType.DRIVER, AmapPageType.ROUTE);
        AmapNaviPage.getInstance().showRouteActivity(getApplicationContext(), params, null);
    }
}