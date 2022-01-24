package com.i_syst.sensortagdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.Manifest;
import android.content.Context;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends Activity {


    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    //private DeviceListAdapter mAdapter;
    private TextView mTempLabel;
    private TextView mHumiLabel;
    private TextView mPressLabel;
    private TextView mDeviceNameLabel;
    private TextView mDeviceAddressLabel;
    private ImageView mLogoImage;
    private LineChart mTemperatureChart;
    private Thread thread;
    private boolean plotData = true;
    private LineData mTemperatureData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // Example of a call to a native method
    //TextView tv = (TextView) findViewById(R.id.sample_text);
    //tv.setText(stringFromJNI());
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        //mTextView = (TextView) findViewById(R.id.text_view);
        mTempLabel = (TextView) findViewById(R.id.tempLabel);
        mHumiLabel = (TextView) findViewById(R.id.humiLabel);
        mPressLabel = (TextView) findViewById(R.id.pressLabel);

        mLogoImage = (ImageView)findViewById(R.id.logo_image);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        //Start scan BLE device
        mLEScanner.startScan(mScanCallback);

        mLogoImage.setImageResource(R.drawable.logo);
        mTemperatureChart = (LineChart) findViewById(R.id.TemperatureChart);
        mTemperatureChart.getDescription().setEnabled(true);
        mTemperatureChart.getDescription().setText("Real time temperature data plot");
        mTemperatureChart.setTouchEnabled(false);
        mTemperatureChart.setDrawBorders(true);
        mTemperatureChart.setDragEnabled(false);
        mTemperatureChart.setScaleEnabled(false);
        mTemperatureChart.setDrawGridBackground(true);
        mTemperatureChart.setPinchZoom(false);
        mTemperatureChart.setBackgroundColor(Color.BLACK);

        mTemperatureData = new LineData();
        mTemperatureData.setValueTextColor(Color.WHITE);
        mTemperatureChart.setData(mTemperatureData);

        Legend legend = mTemperatureChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.WHITE);

        XAxis xl = mTemperatureChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis  = mTemperatureChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        //leftAxis.setAxisMaximum(35f);
        //leftAxis.setAxisMinimum(30f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mTemperatureChart.getAxisRight();
        rightAxis.setEnabled(false);
        mTemperatureChart.getAxisLeft().setDrawGridLines(true);
        mTemperatureChart.getXAxis().setDrawGridLines(true);
        mTemperatureChart.setDrawBorders(false);

        feedMultiple();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    /**  public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    } **/

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "Temperature Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    private void addEntry(float temperature) {
        mTemperatureData = mTemperatureChart.getData();

        if (mTemperatureData != null) {

            ILineDataSet set = mTemperatureData.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                mTemperatureData.addDataSet(set);
            }


            mTemperatureData.addEntry(new Entry(set.getEntryCount(),  temperature), 0);
            mTemperatureData.notifyDataChanged();
            mTemperatureChart.notifyDataSetChanged();
            mTemperatureChart.setMaxVisibleValueCount(100);
            mTemperatureChart.moveViewToX(mTemperatureData.getEntryCount());
        }
    }
    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }

    }
    @Override
    protected void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            //super.onScanResult(callbackType, result);

            //Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_LONG).show();
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();
            //String name = scanRecord.getDeviceName();
            String name = device.getName();
            String address = device.getAddress();
            long deviceID = 0;
            byte[] manuf = scanRecord.getManufacturerSpecificData(0x0177);

            if (manuf == null) {

                //Toast.makeText(getApplicationContext(), "No device found...", Toast.LENGTH_LONG).show();

                return;
            }
            if (manuf[0] == 1) {

                double press = (double)(ByteBuffer.wrap(manuf, 1, 4).order(ByteOrder.LITTLE_ENDIAN).getInt()) / 1000.0;
                double temp = (double)(ByteBuffer.wrap(manuf, 5, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()) / 100.0;
                double humi = (double)(ByteBuffer.wrap(manuf, 7, 2).order(ByteOrder.LITTLE_ENDIAN).getShort()) / 100.0;

                String s = String.format("%.2f", press);
                mPressLabel.setText(s);
                mPressLabel.getRootView().postInvalidate();

                s = String.format("%.2f", temp);
                mTempLabel.setText(s);
                mTempLabel.getRootView().postInvalidate();

                s = String.format("%.0f", humi);
                mHumiLabel.setText(s);
                mHumiLabel.getRootView().postInvalidate();

                if(plotData){
                    addEntry((float) temp);
                    plotData = false;
                }


            }


        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this, "Scan Failed: Please try again...", Toast.LENGTH_LONG).show();
        }
    };
}
