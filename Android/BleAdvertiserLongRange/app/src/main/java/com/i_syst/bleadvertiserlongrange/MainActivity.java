package com.i_syst.bleadvertiserlongrange;

import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    private static final long MIN_TIME_BW_UPDATES = 0;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    public static String deviceName = "Advertiser";
    public static String deviceAddress = "EB:67:8C:69:AD:5B";
    public static String deviceAddress1 = "C4:82:6F:0E:80:18"; //840
    public static String deviceAddress2 = "EB:67:8C:69:AD:5B"; //832
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;
    private static int REQUEST_CODE_COARSE_PERMISSION = 1;
    private static int REQUEST_CODE_BLUETOOTH_PERMISSION = 2;
    private static long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private Handler mHandler;
    private final static String TAG = Service.class.getSimpleName();
    private BluetoothLeScanner mLEScanner;

    private BluetoothGatt mBluetoothGatt;

    private String device_name;
    private String device_address;
    private boolean Notification_enable = false;
    private int counter = 0;
    private TextView tv;

    private double currentLon = 0;
    private double currentLat = 0;
    private double lastLon = 0;
    private double lastLat = 0;
    private double distance;
    private LocationManager locationManager;
    private LocationListener locList;
    private TextView distanceText;
    private TextView latitude;
    private TextView longitude;
    private Button startBtn;
    private Button stopBtn;
    private Switch mSwitch;
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);
            super.onScanResult(callbackType, result);
            //Toast.makeText(MainActivity.this, "Scanning...", Toast.LENGTH_LONG).show();
            //Log.i("callbackType", String.valueOf(callbackType));
            //Log.i("result", result.toString());
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();
            //String name = scanRecord.getDeviceName();
            String name = device.getName();
            String address = device.getAddress();
            //Log.i("Device address: ", address);
            //Device Badger_device = new Device(name,address);
            if (address != null) {
                if (address.equals(deviceAddress)) {
                    //device_name = name;
                    //device_address = address;
                    byte[] manuf = scanRecord.getManufacturerSpecificData(0x0177);

                    if (manuf == null) {

                        return;
                    }

                    int counter = ByteBuffer.wrap(manuf, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();

                    String s = String.format("%d", counter);
                    //Log.i("Raw data " + counter, s);
                    tv.setText(s);

                }
            }


        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //Toast.makeText(MainActivity.this, "Scan Failed: Please try again...", Toast.LENGTH_LONG).show();
        }
    };


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
        } else {
            Toast.makeText(this, R.string.ble_supported, Toast.LENGTH_SHORT).show();
        }
        askCoarsePermission();

        tv = findViewById(R.id.textView);
        locList = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                float[] results = new float[1];
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        currentLat, currentLon, results);

                System.out.println("Distance is: " + results[0]);
                String s = String.format("%.2f m", results[0]);
                distanceText.setText(s);
                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));

                /*Location locationB = new Location("point B");
                locationB.setLatitude(currentLat);
                locationB.setLongitude(currentLon);

                double distanceMeters = location.distanceTo(locationB);

                double distanceKm = distanceMeters / 1000f;

                distanceText.setText(String.format("%.2f Km",distanceKm ));*/
            }
        };
        distanceText = (TextView) findViewById(R.id.distance);
        latitude = (TextView) findViewById(R.id.Latitude);
        longitude = (TextView) findViewById(R.id.Longitude);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locList);
        Log.d("GPS Enabled", "GPS Enabled");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);


        startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String provider = locationManager.getBestProvider(criteria, true);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                Location location = locationManager.getLastKnownLocation(provider);

                latitude.setText(String.valueOf(location.getLatitude()));
                longitude.setText(String.valueOf(location.getLongitude()));
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                scanBluetoothDevices(true);
            }
        });

        stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanBluetoothDevices(false);
            }
        });

        mSwitch = findViewById(R.id.mSwitch);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    deviceAddress = deviceAddress1; //840
                }else{
                    deviceAddress = deviceAddress2; //832
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locList);

    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    private void askCoarsePermission() {
        final String locationPermission;
        if (SDK_INT >= 29)
            locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
        else
            locationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        if (this.checkSelfPermission(locationPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{locationPermission}, REQUEST_CODE_COARSE_PERMISSION);
        }
    }

    private void askBluetoothPermission() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CODE_BLUETOOTH_PERMISSION);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_COARSE_PERMISSION){
            Toast.makeText(this, "Coarse Permission Granted...", Toast.LENGTH_LONG).show();
            askBluetoothPermission();
        }


    }
    private void scanBluetoothDevices(boolean enable) {

        if (enable) {
            mLEScanner.startScan(mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }
}