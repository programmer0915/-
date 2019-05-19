package com.dream.androidapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BluetoothActivity extends Activity {

    private ListView deviceListView = null;

    private ArrayAdapter<String> adapter = null;

    private List<String> deviceList = new ArrayList<>();

    private Button btnSearch = null;

    private BluetoothAdapter bluetoothAdapter = null;

    private DeviceReceiver mydevice = new DeviceReceiver();

    private boolean hasRegistered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initView();
        initBluetooth();
    }

    @Override
    protected void onStart() {
        if (!hasRegistered) {
            hasRegistered = true;
            IntentFilter filterStart = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mydevice, filterStart);
            registerReceiver(mydevice, filterEnd);
        }
        super.onStart();
    }
    //权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200://刚才的识别码
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
                    Toast.makeText(BluetoothActivity.this, "已获取定位权限", Toast.LENGTH_LONG).show();
                }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(BluetoothActivity.this,"未开启定位权限,请手动到设置去开启权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:break;
        }
    }

    private void initView() {
        if(ContextCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(BluetoothActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }else{
            Toast.makeText(BluetoothActivity.this, "已获取定位权限", Toast.LENGTH_LONG).show();
        }
        deviceListView = findViewById(R.id.lstBluetooth);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final String bluetoothInfo = deviceList.get(i);

                if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    btnSearch.setText("继续搜索");
                }
                AlertDialog.Builder dlg = new AlertDialog.Builder(BluetoothActivity.this);
                dlg.setTitle("确定连接设备？");
                dlg.setMessage(bluetoothInfo);

                dlg.setPositiveButton("连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BluetoothUtil.bluetoothAddress = bluetoothInfo.substring(bluetoothInfo.length() - 17);
                        Intent intent = new Intent(BluetoothActivity.this, ChatActivity.class);
                        BluetoothActivity.this.startActivity(intent);
                    }
                });

                dlg.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BluetoothUtil.bluetoothAddress = null;
                    }
                });
                dlg.show();
            }
        });

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                    btnSearch.setText("继续搜索");
                } else {
                    getAvailableDevices();
                    bluetoothAdapter.startDiscovery();
                    btnSearch.setText("停止搜索");
                }
            }
        });


    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RESULT_FIRST_USER);

                Intent intent1 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
                startActivity(intent1);

                bluetoothAdapter.enable();
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("没有蓝牙设备");
            dialog.setMessage("不支持蓝牙设备哦，请检查");

            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            dialog.show();
        }
    }

    private void getAvailableDevices() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();

        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            deviceList.clear();
            adapter.notifyDataSetChanged();
        } else {
            if (devices.size() > 0) {
                for (BluetoothDevice device : devices) {
                    if (!deviceList.contains(device.getName() + '\n' + device.getAddress())) {
                        deviceList.add(device.getName() + '\n' + device.getAddress());
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                deviceList.add("没有找到匹配的蓝牙");
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                getAvailableDevices();
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class DeviceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {

                    deviceList.add(bluetoothDevice.getName() + '\n' + bluetoothDevice.getAddress());
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (deviceListView.getCount() == 0) {
                    deviceList.add("没有蓝牙设备");
                    adapter.notifyDataSetChanged();
                }
                btnSearch.setText("继续搜索");
            }
        }
    }
}