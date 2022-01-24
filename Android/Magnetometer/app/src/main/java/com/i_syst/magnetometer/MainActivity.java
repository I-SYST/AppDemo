package com.i_syst.magnetometer;

import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static String deviceName = "BlueIOThingy";
    private static String BLUEIO_UUID_SERVICE   =  "ef680400-9b35-4933-9b10-52ffa9740042";
    private static String BLE_UUID_TMS_RAW_CHAR   =  "ef680406-9b35-4933-9b10-52ffa9740042";
    private static int MAX_ENTRIES = 20000;
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;
    private static  int REQUEST_CODE_COARSE_PERMISSION = 1;
    private static int REQUEST_CODE_BLUETOOTH_PERMISSION = 2;
    private static long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private Handler mHandler;
    private final static String TAG = Service.class.getSimpleName();
    private LineChart MagXChart;
    private LineChart MagYChart;
    private LineChart MagZChart;
    private Thread thread;
    private boolean plotData = true;
    private LineData MagXData;
    private LineData MagYData;
    private LineData MagZData;
    private BluetoothLeScanner mLEScanner;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mCustomService;
    private BluetoothGattCharacteristic Characteristic;


    private Button start_button;
    private Button stop_button;
    private String device_name;
    private String device_address;
    private boolean Notification_enable = false;
    private int counter = 0;
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
        public void onMtuChanged (BluetoothGatt gatt,
                                  int mtu,
                                  int status){

            Log.i(TAG, "MTU: " + mtu);
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
                    Characteristic = mCustomService.getCharacteristic(UUID.fromString(BLE_UUID_TMS_RAW_CHAR));
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
                int magX = BytesToInt(data[12],data[13]);
                int magY = BytesToInt(data[14],data[15]);
                int magZ = BytesToInt(data[16],data[17]);


                counter += 1;
                Log.d("Raw data " + counter, "MagX: "+ magX + " MagY: " + magY +  " MagZ: " + magZ);
                if (counter%20==0) {
                    addEntry((float)magX, (float)magY, (float)magZ);
                }
            }
        }

    };


    public static short BytesToInt(byte byte1, byte byte2) {
        //return  ((byte1 & 0xFF) << 8) |
        //        ((byte2 & 0xFF) << 0);
        byte[] bytes = {byte2,byte1};
        short i = new BigInteger(bytes).shortValue();
        return i;
    }
    public static byte[] InttoBytes(short value)
    {
        //byte[] result = new byte[2];
        //result[0] = (byte) (i >> 8);
        //result[1] = (byte) (i /*>> 0*/);
        //return result;
        ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.nativeOrder());
        buffer.putShort(value);
        return buffer.array();
    }
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
        else{
            Toast.makeText(this, R.string.ble_supported, Toast.LENGTH_SHORT).show();}
        askCoarsePermission();
        scanBluetoothDevices(true);
        Notification_enable = false;

        start_button = findViewById(R.id.start_button);
        start_button.setEnabled(false);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Notification_enable = true;
                if(mBluetoothGatt==null){

                    final BluetoothDevice bleDevice = mBluetoothAdapter.getRemoteDevice(device_address);
                    if (bleDevice == null) {
                        Toast.makeText(MainActivity.this, "Device not found.  Unable to connect." , Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else{
                        mBluetoothGatt = bleDevice.connectGatt(getBaseContext(), false, mGattCallback);
                        if (mBluetoothGatt==null){
                            Toast.makeText(MainActivity.this, "mBluetoothGatt null "+ mBluetoothGatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            Log.w(TAG, "mBluetoothGatt device: " + mBluetoothGatt.getDevice().getName());
                        }

                    }
                }
                else{

                    Characteristic = mCustomService.getCharacteristic(UUID.fromString(BLE_UUID_TMS_RAW_CHAR));
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
        });

        stop_button = findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Notification_enable = false;
                mBluetoothGatt.setCharacteristicNotification(Characteristic, false);
                mBluetoothGatt.disconnect();
                mBluetoothGatt = null;
            }
        });

        MagXChart = findViewById(R.id.MagXChart);
        MagXData = new LineData();
        MagXData.setValueTextColor(Color.WHITE);
        MagXData.setDrawValues(false);
        MagXChart.setData(MagXData);
        MagXChart.setAutoScaleMinMaxEnabled(true);
        MagXChart.getDescription().setEnabled(false);
        //mTemperatureChart.getDescription().setText("Real time temperature data plot");
        MagXChart.setTouchEnabled(true);
        MagXChart.setPinchZoom(false);
        //mTemperatureChart.setDrawBorders(true);
        MagXChart.setDragEnabled(true);
        MagXChart.setScaleXEnabled(true);
        MagXChart.setScaleYEnabled(true);
        MagXChart.setDrawGridBackground(true);
        MagXChart.setBackgroundColor(Color.BLACK);
        MagXChart.setDrawGridBackground(false);
        MagXChart.setHorizontalScrollBarEnabled(true);
        MagXChart.getAxisLeft().setDrawGridLines(true);
        MagXChart.getXAxis().setDrawGridLines(true);

        Legend legend = MagXChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.WHITE);
        XAxis xl = MagXChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        YAxis leftAxis  = MagXChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        //leftAxis.setDrawGridLines(true);
        YAxis rightAxis = MagXChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        //rightAxis.setEnabled(true);


        MagYChart = findViewById(R.id.MagYChart);
        MagYData = new LineData();
        MagYData.setValueTextColor(Color.WHITE);
        MagYData.setDrawValues(false);
        MagYChart.setData(MagYData);
        MagYChart.setAutoScaleMinMaxEnabled(true);
        MagYChart.getDescription().setEnabled(false);
        //mTemperatureChart.getDescription().setText("Real time temperature data plot");
        MagYChart.setTouchEnabled(true);
        MagYChart.setPinchZoom(false);
        //mTemperatureChart.setDrawBorders(true);
        MagYChart.setDragEnabled(true);
        MagYChart.setScaleXEnabled(true);
        MagYChart.setScaleYEnabled(true);
        MagYChart.setDrawGridBackground(true);
        MagYChart.setBackgroundColor(Color.BLACK);
        MagYChart.setDrawGridBackground(false);
        MagYChart.setHorizontalScrollBarEnabled(true);
        MagYChart.getAxisLeft().setDrawGridLines(true);
        MagYChart.getXAxis().setDrawGridLines(true);

        Legend legend2 = MagYChart.getLegend();
        legend2.setForm(Legend.LegendForm.LINE);
        legend2.setTextColor(Color.WHITE);
        XAxis xl2 = MagYChart.getXAxis();
        xl2.setTextColor(Color.WHITE);
        xl2.setDrawGridLines(true);
        xl2.setAvoidFirstLastClipping(true);
        xl2.setEnabled(true);
        YAxis leftAxis2  = MagYChart.getAxisLeft();
        leftAxis2.setTextColor(Color.WHITE);
        //leftAxis2.setDrawGridLines(false);
        //leftAxis2.setDrawGridLines(true);
        YAxis rightAxis2 = MagYChart.getAxisRight();
        rightAxis2.setTextColor(Color.WHITE);

        MagZChart = findViewById(R.id.MagZChart);
        MagZData = new LineData();
        MagZData.setValueTextColor(Color.WHITE);
        MagZData.setDrawValues(false);
        MagZChart.setData(MagZData);
        MagZChart.setAutoScaleMinMaxEnabled(true);
        MagZChart.getDescription().setEnabled(false);
        //mTemperatureChart.getDescription().setText("Real time temperature data plot");
        MagZChart.setTouchEnabled(true);
        MagZChart.setPinchZoom(false);
        //mTemperatureChart.setDrawBorders(true);
        MagZChart.setDragEnabled(true);
        MagZChart.setScaleXEnabled(true);
        MagZChart.setScaleYEnabled(true);
        MagZChart.setDrawGridBackground(true);
        MagZChart.setBackgroundColor(Color.BLACK);
        MagZChart.setDrawGridBackground(false);
        MagZChart.setHorizontalScrollBarEnabled(true);
        MagZChart.getAxisLeft().setDrawGridLines(true);
        MagZChart.getXAxis().setDrawGridLines(true);

        Legend legend3 = MagZChart.getLegend();
        legend3.setForm(Legend.LegendForm.LINE);
        legend3.setTextColor(Color.WHITE);
        XAxis xl3 = MagZChart.getXAxis();
        xl3.setTextColor(Color.WHITE);
        xl3.setDrawGridLines(true);
        xl3.setAvoidFirstLastClipping(true);
        xl3.setEnabled(true);
        YAxis leftAxis3  = MagZChart.getAxisLeft();
        leftAxis3.setTextColor(Color.WHITE);
        //leftAxis3.setDrawGridLines(true);
        YAxis rightAxis3 = MagZChart.getAxisRight();
        //rightAxis3.setEnabled(false);
        rightAxis3.setTextColor(Color.WHITE);
    }


    private LineDataSet createSet(String datalabel,int color){
        LineDataSet set = new LineDataSet(null, datalabel);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1.5f);
        //set.setColor(Color.MAGENTA);
        set.setColor(color);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setDrawCircles(false);
        return set;
    }
    private void addEntry(float magX, float magY, float magZ) {

        if (MagXData != null && MagYData != null && MagZData != null) {

            LineDataSet setX = (LineDataSet) MagXData.getDataSetByIndex(0);
            LineDataSet setY = (LineDataSet) MagYData.getDataSetByIndex(0);
            LineDataSet setZ = (LineDataSet) MagZData.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (setX == null) {
                String s = "MagX";
                int color = Color.RED;
                setX = createSet(s, color);
                MagXData.addDataSet(setX);
            }

            if (setY == null) {
                String s = "MagY";
                int color = Color.GREEN;
                setY = createSet(s, color);
                MagYData.addDataSet(setY);
            }
            if (setZ == null) {
                String s = "MagZ";
                int color = Color.BLUE;
                setZ = createSet(s, color);
                MagZData.addDataSet(setZ);
            }

            MagXData.addEntry(new Entry(setX.getEntryCount(),  magX), 0);
            MagYData.addEntry(new Entry(setY.getEntryCount(),  magY), 0);
            MagZData.addEntry(new Entry(setZ.getEntryCount(),  magZ), 0);

            /*MagXData.addEntry(new Entry(counter,  magX), 0);
            MagYData.addEntry(new Entry(counter,  magY), 0);
            MagZData.addEntry(new Entry(counter,  magZ), 0);
            counter+=1;*/

            MagXData.notifyDataChanged();
            MagYData.notifyDataChanged();
            MagZData.notifyDataChanged();

            MagXData.setDrawValues(false);
            MagYData.setDrawValues(false);
            MagZData.setDrawValues(false);

            MagXChart.highlightValue(null);
            MagYChart.highlightValue(null);
            MagZChart.highlightValue(null);

            MagXChart.setData(MagXData);
            MagXChart.setAutoScaleMinMaxEnabled(true);
            MagXChart.setBackgroundColor(Color.BLACK);
            MagXChart.setDrawGridBackground(false);
            MagXChart.notifyDataSetChanged();
            MagXChart.invalidate();
            MagXChart.refreshDrawableState();
            MagXChart.setMaxVisibleValueCount(500);
            MagXChart.moveViewToX(MagXData.getEntryCount());

            MagYChart.setData(MagYData);
            MagYChart.setAutoScaleMinMaxEnabled(true);
            MagYChart.setBackgroundColor(Color.BLACK);
            MagYChart.setDrawGridBackground(false);
            MagYChart.notifyDataSetChanged();
            MagYChart.invalidate();
            MagYChart.refreshDrawableState();
            MagYChart.setMaxVisibleValueCount(500);
            MagYChart.moveViewToX(MagYData.getEntryCount());

            MagZChart.setData(MagZData);
            MagZChart.setAutoScaleMinMaxEnabled(true);
            MagZChart.setBackgroundColor(Color.BLACK);
            MagZChart.setDrawGridBackground(false);
            MagZChart.notifyDataSetChanged();
            MagZChart.invalidate();
            MagZChart.refreshDrawableState();
            MagZChart.setMaxVisibleValueCount(500);
            MagZChart.moveViewToX(MagZData.getEntryCount());

            /*
            if(setX.getEntryCount() == MAX_ENTRIES) {

                setX.removeEntry(0);
                setY.removeEntry(0);
                setZ.removeEntry(0);
                for (Entry entry : setX.getValues()) {
                    entry.setX(entry.getX() - 1);
                }
                for (Entry entry : setY.getValues()) {
                    entry.setX(entry.getX() - 1);
                }
                for (Entry entry : setZ.getValues()) {
                    entry.setX(entry.getX() - 1);
                }
            }*/


        }
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
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLEScanner.stopScan(mScanCallback);
                    if(device_address!=null){
                        start_button.setEnabled(true);
                    }
                }
            }, SCAN_PERIOD);
            mLEScanner.startScan(mScanCallback);
        } else {
            mLEScanner.stopScan(mScanCallback);
        }
    }
}