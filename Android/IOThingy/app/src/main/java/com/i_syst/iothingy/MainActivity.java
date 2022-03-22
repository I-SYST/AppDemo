package com.i_syst.iothingy;

import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static String deviceName = "BlueIOThingy";
    private static String BLUEIO_UUID_SERVICE   =  "ef680400-9b35-4933-9b10-52ffa9740042";
    private static String BLE_UUID_QUATERNION_CHAR   =  "ef680404-9b35-4933-9b10-52ffa9740042";
    private static long SCAN_PERIOD = 5000;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static  int REQUEST_CODE_COARSE_PERMISSION = 1;
    private static int REQUEST_CODE_BLUETOOTH_PERMISSION = 2;
    private Handler mHandler;
    final String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    //private GradientDrawable gd;
    private ActivityResultContracts.RequestMultiplePermissions requestMultiplePermissionsContract;
    private ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattService mCustomService;
    private BluetoothGattCharacteristic Characteristic;
    private BluetoothLeScanner mLEScanner;
    private BluetoothGatt mBluetoothGatt;
    private String device_name;
    private String device_address;
    private GLSurfaceView glSurfaceView;
    private OpenGLRenderer openGLRenderer;
    private boolean Notification_enable = false;


    private ScanCallback mScanCallback = new ScanCallback(){
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);
            super.onScanResult(callbackType, result);
            //Toast.makeText(MainActivity.this, "Scanning...", Toast.LENGTH_LONG).show();
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();
            //String name = scanRecord.getDeviceName();
            String name = device.getName();
            String address = device.getAddress();
            //Device Badger_device = new Device(name,address);
            if (name!= null ) {
                if(name.equals(deviceName)){
                    device_name = name;
                    device_address = address;

                }
            }


        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //Toast.makeText(MainActivity.this, "Scan Failed: Please try again...", Toast.LENGTH_LONG).show();
        }
    };
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //gatt.requestMtu(511);
                gatt.discoverServices();
                //mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //gatt.requestMtu(20);
                mCustomService = gatt.getService(UUID.fromString(BLUEIO_UUID_SERVICE));
                //mCustomService = gatt.getService(UUID.fromString("00000001-2a76-4901-a32a-db0eea85d0e5"));
                if(mCustomService == null) {
                    Log.w(TAG, "Custom BLE Service not found");
                }


                if(Notification_enable == true){
                    Characteristic = mCustomService.getCharacteristic(UUID.fromString(BLE_UUID_QUATERNION_CHAR));
                    //Characteristic = mCustomService.getCharacteristic(UUID.fromString("00000003-2a76-4901-a32a-db0eea85d0e5"));
                    //gatt.setCharacteristicNotification(Characteristic, true);
                    boolean registered = false;
                    if (Characteristic != null) {
                        registered = gatt.setCharacteristicNotification(Characteristic, true);
                        if (registered) {
                            for (BluetoothGattDescriptor descriptor : Characteristic.getDescriptors()) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                            }
                        }
                    }
                }


            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //mBluetoothGatt.disconnect();
                Log.d("onCharacteristicRead", characteristic.getValue().toString());
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status != BluetoothGatt.GATT_SUCCESS){
                Log.d("onCharacteristicWrite", "Failed write, retrying");
                gatt.writeCharacteristic(characteristic);
            }
            Log.d("onCharacteristicWrite", characteristic.getValue().toString());
            super.onCharacteristicWrite(gatt, characteristic, status);


        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            byte[] data = characteristic.getValue();
            String s = Arrays.toString(data);

            if (data != null && data.length > 0) {

                Log.d("onCharacteristicChanged", "Data Value: " + s);
                byte[] Wdata = Arrays.copyOfRange(data, 0,4);
                float W = convertByteArrayToInt32(Wdata)/(float)(1<<30);
                byte[] Xdata = Arrays.copyOfRange(data, 4,8);
                float X = convertByteArrayToInt32(Xdata)/(float)(1<<30);
                byte[] Ydata = Arrays.copyOfRange(data, 8,12);
                float Y = convertByteArrayToInt32(Ydata)/(float)(1<<30);
                byte[] Zdata = Arrays.copyOfRange(data, 12,16);
                float Z = convertByteArrayToInt32(Zdata)/(float)(1<<30);
                Log.d("onCharacteristicChanged", "Data Value: X: " + X + "; Y: " + Y + "; Z: " + Z + "; W: " + W);
                double angle = 2*Math.acos(W);
                openGLRenderer.angle = (float) angle;
                openGLRenderer.rotateX = (float) (X/Math.sin(angle/2));
                openGLRenderer.rotateY = (float) (Y/Math.sin(angle/2));
                openGLRenderer.rotateZ = (float) (Z/Math.sin(angle/2));
            }
        }

    };
    public static int convertByteArrayToInt32(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8) |
                ((bytes[0] & 0xFF) << 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        //Check if Bluetooth Low Energy is supported by the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        requestMultiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionActivityResultLauncher = registerForActivityResult(requestMultiplePermissionsContract, isGranted -> {
            //Log.d("PERMISSIONS", "Launcher result: " + isGranted.toString());
            if (isGranted.containsValue(false)) {
                //Log.d("PERMISSIONS", "At least one of the permissions was not granted, launching again...");
                multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
            }
        });
        requestBlePermissions(MainActivity.this,REQUEST_CODE_BLUETOOTH_PERMISSION);
        //multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
        askCoarsePermission();
        glSurfaceView = new GLSurfaceView(this);
        openGLRenderer = new OpenGLRenderer();

        glSurfaceView.setRenderer(openGLRenderer);
        setContentView(glSurfaceView);
        scanBluetoothDevices(true);
    }
    private void scanBluetoothDevices(boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    if(device_name.equals(deviceName)) {
                        SetNotificationChange();
                    }
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }
    void SetNotificationChange(){
        Notification_enable = true;
        if(mBluetoothGatt==null){

            final BluetoothDevice bleDevice = mBluetoothAdapter.getRemoteDevice(device_address);
            if (bleDevice == null) {
                //Toast.makeText(MainActivity.this, "Device not found.  Unable to connect." , Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Device not found.  Unable to connect. " + mBluetoothGatt.getDevice().getName());
                return;
            }
            else{
                mBluetoothGatt = bleDevice.connectGatt(getBaseContext(), false, mGattCallback);
                if (mBluetoothGatt==null){
                    //Toast.makeText(MainActivity.this, "mBluetoothGatt null "+ mBluetoothGatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "mBluetoothGatt null: " + mBluetoothGatt.getDevice().getName());
                    return;
                }else{
                    Log.w(TAG, "mBluetoothGatt device: " + mBluetoothGatt.getDevice().getName());
                }

            }
        }else {
            Characteristic = mCustomService.getCharacteristic(UUID.fromString(BLE_UUID_QUATERNION_CHAR));
            //Characteristic = mCustomService.getCharacteristic(UUID.fromString("00000003-2a76-4901-a32a-db0eea85d0e5"));
            //gatt.setCharacteristicNotification(Characteristic, true);
            boolean registered = false;
            if (Characteristic != null) {
                registered = mBluetoothGatt.setCharacteristicNotification(Characteristic, true);
                if (registered) {
                    for (BluetoothGattDescriptor descriptor : Characteristic.getDescriptors()) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void askCoarsePermission() {
        final String locationPermission;

        if (SDK_INT >= 29){
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                    Toast.makeText(MainActivity.this,  "Precise location access granted..", Toast.LENGTH_SHORT).show();
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                    Toast.makeText(MainActivity.this,  "Only approximate location access granted..", Toast.LENGTH_SHORT).show();
                                } else {
                                    // No location access granted.
                                    Toast.makeText(MainActivity.this,  "No location access granted..", Toast.LENGTH_SHORT).show();
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_COARSE_PERMISSION);
                                }
                            }
                    );

            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }else{
            locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
            if (this.checkSelfPermission(locationPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{locationPermission}, REQUEST_CODE_COARSE_PERMISSION);
            }
        }

    }

    private void askBluetoothPermission() {
        if (!mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
            }else{
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_CODE_BLUETOOTH_PERMISSION);

            }
        }
    }
    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_COARSE_PERMISSION){

            //askBluetoothPermission();
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location Permission Granted!", Toast.LENGTH_LONG).show();
                mLEScanner.stopScan(mScanCallback);
            }  else {
                Toast.makeText(this, "Location Permission Denied!", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Enable Location Access in\nSetting/Apps/Permission", Toast.LENGTH_LONG).show();
            }
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_BLUETOOTH_PERMISSION){
            //askBluetoothPermission();
            //askCoarsePermission();
        }
    }
}