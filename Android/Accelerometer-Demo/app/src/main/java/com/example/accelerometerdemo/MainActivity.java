package com.example.accelerometerdemo; // Keep your package name

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "BlueIO_Accelerometer";

    // BlueIO device information
    public static String targetDeviceName = "BlueIOThingy";
    private static final String BLUEIO_UUID_SERVICE = "ef680400-9b35-4933-9b10-52ffa9740042";
    private static final String ACCELEROMETER_CHAR_UUID_STRING = "ef680406-9b35-4933-9b10-52ffa9740042"; // Raw data characteristic
    private static final UUID ACCELEROMETER_CHAR_UUID = UUID.fromString(ACCELEROMETER_CHAR_UUID_STRING);
    private static final String MOTION_CONFIG_CHAR_UUID_STRING = "ef680401-9b35-4933-9b10-52ffa9740042";
    private static final UUID MOTION_CONFIG_CHAR_UUID = UUID.fromString(MOTION_CONFIG_CHAR_UUID_STRING);
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // Standard CCCD
    private static final float accelFSR = 2.0f; // ±2g
    private static final float scaleFactor = 32768.0f;

    // Bluetooth Variables
    private BluetoothManager mBluetoothManager; // Used to access BluetoothAdapter and manage Bluetooth Profiles
    private BluetoothAdapter mBluetoothAdapter; // Used to check if bluetooth is supported, enable/disable it and access BluetoothLeScanner


    // Bluetooth Permissions
    private static final int REQUEST_CODE_COARSE_PERMISSION = 1;
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSION = 2;
    private ActivityResultContracts.RequestMultiplePermissions requestMultiplePermissionsContract;
    private ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher;
    private final String[] ANDROID_12_BLE_PERMISSIONS = new String[] {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private final String[] BLE_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // Bluetooth Scan Variables
    private BluetoothLeScanner mLEScanner; // Scanner for BLE devices
    private static long SCAN_PERIOD = 5000; // Defines a fixed 5 seconds scanning period
    private Handler mHandler;

    // Bluetooth Discovery Variables
    private String discoveredDeviceName;
    private String discoveredDeviceAddress;

    // Bluetooth Connection and Interacting with devices Variables
    private boolean isConnecting = false;
    private boolean notificationEnabled = false;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mBlueIOService;
    private BluetoothGattCharacteristic mAccelerometerCharacteristic;

    // UI elements in the app
    private LinearLayout chartLayout;
    private TextView statusTextView;
    private LineChart accelerometerChart;
    private Button connectButton;
    private Spinner chartOptionsDropDown, weightUnitDropDown;
    private ToggleButton showXAxisButton, showYAxisButton, showZAxisButton;

    // UI elements in the dialogbox
    Dialog dialog;
    LineChart resultChart;
    TextView avgForceTxt, maxForceTxt, avgAccelTxt, maxAccelTxt;

    // Chart Data
    private long dataPointCount = 0;

    // Data Saving Variables
    private Button recordButton, resultButton;
    private boolean isRecording = false;
    private final String filename = "accelerometer_data.csv";
    private FileOutputStream fileOutputStream;
    private long firstLoggedTime = 0, lastLoggedTime = 0;
    private float xSum = 0, ySum = 0, zSum = 0;
    private int dataCount = 0;
    private float userWeight;


    /*█████╗████████╗░█████╗░██████╗░████████╗
    ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗╚══██╔══╝
    ╚█████╗░░░░██║░░░███████║██████╔╝░░░██║░░░
    ░╚═══██╗░░░██║░░░██╔══██║██╔══██╗░░░██║░░░
    ██████╔╝░░░██║░░░██║░░██║██║░░██║░░░██║░░░
    ╚═════╝░░░░╚═╝░░░╚═╝░░╚═╝╚═╝░░╚═╝░░░╚═╝░*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize variables and bluetooth services
        mHandler = new Handler();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Check for BLE support and initialize BLE Scanner if it does
        if (mBluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // Initialize UI elements
        chartLayout = findViewById(R.id.chartLayout);
        chartLayout.setVisibility(View.GONE);

        statusTextView = findViewById(R.id.statusTextView); // You'll need to add this to your layout
        chartOptionsDropDown = findViewById(R.id.chartOptionsDropDown);
        final List<String> states = Arrays.asList("Accelerometer (g)", "Force Meter (N)");
        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), R.layout.custom_spinner_item, states);
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        chartOptionsDropDown.setAdapter(adapter);

        accelerometerChart = findViewById(R.id.accelerometerChart);
        setupChart(accelerometerChart, "Accelerometer Data (X, Y, Z)");

        showXAxisButton = findViewById(R.id.showXAxisButton);
        showYAxisButton = findViewById(R.id.showYAxisButton);
        showZAxisButton = findViewById(R.id.showZAxisButton);

        connectButton = findViewById(R.id.btnStartDiscovery); // Use your existing button
        connectButton.setOnClickListener(v -> {
            if (mBluetoothGatt != null) {
                // A connection exists, so disconnect
                disconnectFromDevice();
            } else {
                // No connection, start the search/connect process
                connectButton.setText("Searching...");
                startScan();
            }
        });

        weightUnitDropDown = findViewById(R.id.weightUnit);
        final List<String> units = Arrays.asList("kg", "lb");
        ArrayAdapter unitAdapter = new ArrayAdapter(getApplicationContext(), R.layout.custom_spinner_item, units);
        unitAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        weightUnitDropDown.setAdapter(unitAdapter);

        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                closeFileOutputStream();
                recordButton.setText("Record \uD83D\uDD34");
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.lightblue, null));
            }
            else {
                getFileOutputStream();
                recordButton.setText("Recording... Press again to pause ||");
                recordButton.setBackgroundTintList(getResources().getColorStateList(R.color.lightgray, null));
            }
            isRecording = !isRecording;
        });

        resultButton = findViewById(R.id.exportButton);
        resultButton.setOnClickListener(v -> {
            showDialog();
        });

        // Permission handling setup (from BlueIOThingy demo code)
        requestMultiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionActivityResultLauncher = registerForActivityResult(requestMultiplePermissionsContract, isGranted -> {
            Log.d(TAG, "Permissions Launcher result: " + isGranted.toString());

            // Check if all necessary permissions are granted (Seperated by asking based on build version of device
            boolean allGranted = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (Boolean.FALSE.equals(isGranted.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false)) || Boolean.FALSE.equals(isGranted.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false))) {
                    allGranted = false;
                }
            }
            else {
                if (Boolean.FALSE.equals(isGranted.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false))) { // Coarse is usually enough for pre-S scanning
                    allGranted = false;
                }
            }

            // If the device hasn't granted permission, ask user to do so
            if (!allGranted) {
                Toast.makeText(this, "Required Bluetooth/Location permissions not granted. App may not function correctly.", Toast.LENGTH_LONG).show();
            }
            else {
                Log.d(TAG, "All necessary BLE permissions granted.");
                // Permissions are good, proceed with asking to enable Bluetooth if needed
                askBluetoothPermission();
            }
        });

        // Request permissions on startup
        requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION);
        askCoarsePermission();

        // Reset the data saved from previous performances on the app
        boolean deleted = deleteFile(filename);
        Log.d(TAG, (deleted) ? "Successfully deleted file: " + filename : "Failed to delete file: " + filename);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop scanning for devices if the scanCallBack method is running
        if (mLEScanner != null && mBluetoothAdapter.isEnabled() && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            mLEScanner.stopScan(mScanCallback);
        }

        // Close GATT connection
        if (mBluetoothGatt != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        // Clean up any pending handler messages
        mHandler.removeCallbacksAndMessages(null);
    }

    /*████╗░███████╗██████╗░███╗░░░███╗██╗░██████╗░██████╗██╗░█████╗░███╗░░██╗░██████╗
    ██╔══██╗██╔════╝██╔══██╗████╗░████║██║██╔════╝██╔════╝██║██╔══██╗████╗░██║██╔════╝
    ██████╔╝█████╗░░██████╔╝██╔████╔██║██║╚█████╗░╚█████╗░██║██║░░██║██╔██╗██║╚█████╗░
    ██╔═══╝░██╔══╝░░██╔══██╗██║╚██╔╝██║██║░╚═══██╗░╚═══██╗██║██║░░██║██║╚████║░╚═══██╗
    ██║░░░░░███████╗██║░░██║██║░╚═╝░██║██║██████╔╝██████╔╝██║╚█████╔╝██║░╚███║██████╔╝
    ╚═╝░░░░░╚══════╝╚═╝░░╚═╝╚═╝░░░░░╚═╝╚═╝╚═════╝░╚═════╝░╚═╝░╚════╝░╚═╝░░╚══╝╚═════*/

    // Helper Method for Future Bluetooth Request
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    // Request bluetooth permission to device
    public void requestBlePermissions(Activity activity, int requestCode) {
        if (SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }

    // A more general location permission request
    private void askCoarsePermission() {
        if (SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (Q) and above, ACCESS_FINE_LOCATION implies COARSE
            ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted)
                    Toast.makeText(MainActivity.this, "Precise location access granted.", Toast.LENGTH_SHORT).show();

                else if (coarseLocationGranted != null && coarseLocationGranted)
                    Toast.makeText(MainActivity.this, "Only approximate location access granted.", Toast.LENGTH_SHORT).show();

                else
                    Toast.makeText(MainActivity.this, "No location access granted. BLE scanning may not work.", Toast.LENGTH_SHORT).show();
            });

            locationPermissionRequest.launch(BLE_PERMISSIONS);
        }
        // For devices below Android version 10
        else {
            if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_COARSE_PERMISSION);
            }
        }
    }

    // A more general bluetooth permission request
    private void askBluetoothPermission() {
        if (!mBluetoothAdapter.isEnabled()) {
            // Check BLUETOOTH_CONNECT permission before launching enable BT intent on Android 12+
            if (SDK_INT >= Build.VERSION_CODES.S && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Toast.makeText(this, "BLUETOOTH_CONNECT permission needed to enable Bluetooth.", Toast.LENGTH_SHORT).show();
                requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION);
                return;
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        }
    }

    // ActivityResultLauncher for enabling Bluetooth (similar to BlueIOThingy app demo code)
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            // Bluetooth is now enabled, device can try starting discovery
            Toast.makeText(this, "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show();
            // Restart scan after Bluetooth enabled
            startScan();
        }
        else {
            Toast.makeText(this, "Bluetooth not enabled.", Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> statusTextView.setText("Status: Bluetooth Disabled"));
        }
    });


    /*█████╗░█████╗░░█████╗░███╗░░██╗
    ██╔════╝██╔══██╗██╔══██╗████╗░██║
    ╚█████╗░██║░░╚═╝███████║██╔██╗██║
    ░╚═══██╗██║░░██╗██╔══██║██║╚████║
    ██████╔╝╚█████╔╝██║░░██║██║░╚███║
    ╚═════╝░░╚════╝░╚═╝░░╚═╝╚═╝░░╚═*/

    // Start scanning the devices nearby and start the mScanCallBack method when everything is working
    @SuppressLint("MissingPermission")
    private void startScan() {
        // Check if BluetoothAdapter works
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not enabled. Asking for permission...", Toast.LENGTH_SHORT).show();
            askBluetoothPermission();
            return;
        }

        // Check if mLEScanner works
        if (mLEScanner == null) {
            Toast.makeText(this, "BLE Scanner not initialized. Bluetooth may be off or unsupported.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if bluetooth scan permissions are actually allowed before scanning
        if (SDK_INT >= Build.VERSION_CODES.S && !hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Toast.makeText(this, "BLUETOOTH_SCAN permission required to start scan.", Toast.LENGTH_SHORT).show();
            requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION);
            return;
        }
        else if (SDK_INT < Build.VERSION_CODES.S && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this,"Location permission required to start scan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inform/log that the scanning process is starting
        Log.d(TAG, "Starting BLE scan for device: " + targetDeviceName);
        runOnUiThread(() -> statusTextView.setText("Status: Scanning for " + targetDeviceName + "..."));
        Toast.makeText(this, "Scanning for " + targetDeviceName + "...", Toast.LENGTH_SHORT).show();

        // Reset current discovered device name
        discoveredDeviceName = null;
        discoveredDeviceAddress = null;

        // Stop the scan (running after this declaration) after a duration of "SCAN_PERIOD" if no device is found
        mHandler.postDelayed(() -> {
            if (mBluetoothAdapter.isEnabled() && mLEScanner != null) {
                // Stop mLEScanner from running if no device can be found
                if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return;
                mLEScanner.stopScan(mScanCallback);

                // Inform/log about scan failure
                Log.d(TAG, "Scan stopped by timeout.");
                if (discoveredDeviceAddress == null) {
                    runOnUiThread(() -> {
                        statusTextView.setText("Status: Scan finished, device not found.");
                        Toast.makeText(MainActivity.this, "Device " + targetDeviceName + "not found.", Toast.LENGTH_SHORT).show();
                    });
                }


            }
        }, SCAN_PERIOD);

        // Start scan using the callback method below
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return;
        mLEScanner.startScan(mScanCallback);
    }

    // BLE Scan Callback
    private ScanCallback mScanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // Run default behaviour of the scan result
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            // Gets the name of current found device and log it
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.d(TAG, "Check Bluetooth permissions again");
                return;
            }

            String name = device.getName();
            String address = device.getAddress();
            List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();

            Log.d(TAG, "device: " + name + ", address: " + address + ", UUIDs " + (uuids != null ? uuids.toString() : "null"));

            // If a device is found with the same name (BlueIOThingy), attempt to establish a GATT connection
            // Note : Perhaps find a better way than just rely on the device name
            if (name != null && name.equals(targetDeviceName)) {
                // Log device to make sure
                if (SDK_INT >= Build.VERSION_CODES.O)
                    Log.d(TAG, "Found target device: " + name + ", address: " + address + ", UUIDs " + (uuids != null ? uuids.toString() : "null"));

                discoveredDeviceName = name;
                discoveredDeviceAddress = address;

                // Stop the scanning process and the "SCAN_PERIOD" stopper method
                if (mBluetoothAdapter.isEnabled()) {
                    if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return;
                    mLEScanner.stopScan(this);
                    mHandler.removeCallbacksAndMessages(null);
                    connectToDevice(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan Failed: " + errorCode);
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Scan Failed: " + errorCode, Toast.LENGTH_LONG).show());
            isConnecting = false;
        }
    };

    /*████╗░░█████╗░███╗░░██╗███╗░░██╗███████╗░█████╗░████████╗
    ██╔══██╗██╔══██╗████╗░██║████╗░██║██╔════╝██╔══██╗╚══██╔══╝
    ██║░░╚═╝██║░░██║██╔██╗██║██╔██╗██║█████╗░░██║░░╚═╝░░░██║░░░
    ██║░░██╗██║░░██║██║╚████║██║╚████║██╔══╝░░██║░░██╗░░░██║░░░
    ╚█████╔╝╚█████╔╝██║░╚███║██║░╚███║███████╗╚█████╔╝░░░██║░░░
    ░╚════╝░░╚════╝░╚═╝░░╚══╝╚═╝░░╚══╝╚══════╝░╚════╝░░░░╚═╝░*/

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        // Avoid doing it twice
        if (isConnecting) {
            Log.d(TAG, "Already attempting to connect. Ignoring new request.");
            return;
        }

        // If the required device hasn't been scanned, stop
        if (mBluetoothAdapter == null || device == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified device.");
            return;
        }

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Toast.makeText(this, "BLUETOOTH_CONNECT permission required to connect.", Toast.LENGTH_SHORT).show();
            requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION); // Re-request
            return;
        }

        // Attempt to establish the GATT connection
        isConnecting = true;
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);

        // Inform/log of connection attempt
        Log.d(TAG, "Attempting to create a new GATT connection.");
        runOnUiThread(() -> statusTextView.setText("Status: Connecting to " + (device.getName() != null ? device.getName() : device.getAddress()) + "..."));
    }

    // GATT Callbacks (runs when the device is connected to the server and sends information such as connection status and further Gatt operations)
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // GATT operation succeeded, now checking if the device is connected to the server
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        // Log connection state
                        Log.d(TAG, "Connected to GATT server.");
                        isConnecting = false;
                        firstLoggedTime = System.currentTimeMillis();

                        // Inform user that the connection is successfuly established
                        runOnUiThread(() -> {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            statusTextView.setText("Status: Connected to " + (gatt.getDevice().getName() != null ? gatt.getDevice().getName() : gatt.getDevice().getAddress()));
                            Toast.makeText(MainActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                            connectButton.setText("Disconnect");
                            chartLayout.setVisibility(View.VISIBLE);
                        });

                        // Discover services after successful connection
                        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                            Log.w(TAG, "BLUETOOTH_CONNECT permission missing for discoverServices.");
                            return;
                        }
                        gatt.discoverServices();
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED:
                        // Log connection state
                        Log.d(TAG, "Disconnected from GATT server.");
                        isConnecting = false;

                        // Inform user of this disconnection
                        runOnUiThread(() -> {
                            statusTextView.setText("Status: Disconnected");
                            Toast.makeText(MainActivity.this, "Disconnected.", Toast.LENGTH_SHORT).show();

                            // Clear chart data upon disconnection
                            accelerometerChart.clear();
                            dataPointCount = 0;
                            notificationEnabled = false; // Reset notification flag

                            // Restart scan here if auto-reconnect is needed
                            // scanBluetoothDevices(true);
                            connectButton.setText("Search for Device");
                            chartLayout.setVisibility(View.GONE);
                        });

                        // Close the GATT client
                        gatt.close();
                        mBluetoothGatt = null;
                        break;
                    default:
                        // Something really wrong has happened
                        Log.d(TAG, "Something really wrong has happened when attempting to establish GATT connection.");
                        break;
                }
            }

            // GATT operations failed, connection issues
            else {
                // Log connection state
                Log.w(TAG, "GATT connection failed with status: " + status);
                isConnecting = false;

                // Inform user
                runOnUiThread(() -> {
                    statusTextView.setText("Status: Connection Failed (" + status + ")");
                    Toast.makeText(MainActivity.this, "Connection failed (Status: " + status + ")", Toast.LENGTH_LONG).show();
                });

                // Close the GATT client
                gatt.close();
                mBluetoothGatt = null;
            }
        }

        /*████╗░██╗░██████╗░█████╗░░█████╗░██╗░░░██╗███████╗██████╗░██╗░░░██╗
        ██╔══██╗██║██╔════╝██╔══██╗██╔══██╗██║░░░██║██╔════╝██╔══██╗╚██╗░██╔╝
        ██║░░██║██║╚█████╗░██║░░╚═╝██║░░██║╚██╗░██╔╝█████╗░░██████╔╝░╚████╔╝░
        ██║░░██║██║░╚═══██╗██║░░██╗██║░░██║░╚████╔╝░██╔══╝░░██╔══██╗░░╚██╔╝░░
        ██████╔╝██║██████╔╝╚█████╔╝╚█████╔╝░░╚██╔╝░░███████╗██║░░██║░░░██║░░░
        ╚═════╝░╚═╝╚═════╝░░╚════╝░░╚════╝░░░░╚═╝░░░╚══════╝╚═╝░░╚═╝░░░╚═╝░*/

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered.");

                // Finds if a service with the provided UUID above exists in the device
                mBlueIOService = gatt.getService(UUID.fromString(BLUEIO_UUID_SERVICE));
                if (mBlueIOService == null) {
                    Log.w(TAG, "BlueIO Service (UUID: " + BLUEIO_UUID_SERVICE + ") not found!");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "BlueIO Service not found!", Toast.LENGTH_LONG).show());
                    return;
                }

                // Get the configuration characteristic
                BluetoothGattCharacteristic configCharacteristic = mBlueIOService.getCharacteristic(MOTION_CONFIG_CHAR_UUID);
                if (configCharacteristic == null) {
                    Log.w(TAG, "Motion Configuration Characteristic not found.");
                    return;
                }

                // Write the command to turn on the sensors
                Log.d(TAG, "Found Motion Config Characteristic. Writing configuration...");

                /* COMMAND STRUCTURE
                   Feature to Configure             |   Op-code (Hex)   | Value to Send
                   Operating Mode                   |       0x01        | 0x01 (Wake-on-Motion)
                   Accelerometer Sample Rate        |       0x03        | A value from about 5 to 1000 Hz (in hex)
                   Accelerometer Full-Scale Range   |       0x00        | 0x00 (±2g), 0x01 (±4g), 0x02 (±8g), 0x03 (±16g)
                   Gyroscope Sample Rate            |       0.07        | A value from about 5 to 1000 Hz (in hex)
                   etc...
                */

                // In this case Sample Rate is set to 50 Hz

                configCharacteristic.setValue(new byte[]{0x01, 0x03, 0x03, 0x32, 0x04, 0x02, 0x08, 0x00, 0x0A, (byte) 0xC8, 0x00});
                if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return;
                gatt.writeCharacteristic(configCharacteristic);

                /* If that service exist, find its characteristics
                mAccelerometerCharacteristic = mBlueIOService.getCharacteristic(ACCELEROMETER_CHAR_UUID);
                if (mAccelerometerCharacteristic != null) {
                    Log.d(TAG, "Found Accelerometer Characteristic!");
                    // Enable notifications
                    setCharacteristicNotification(gatt, mAccelerometerCharacteristic, true);
                }
                // Otherwise, log the failure
                else {
                    Log.w(TAG, "Accelerometer Characteristic (UUID: " + ACCELEROMETER_CHAR_UUID_STRING + ") not found.");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Accelerometer characteristic not found.", Toast.LENGTH_LONG).show());
                }*/
            }
            // In the case the GATT connection failed, send a warning message in console
            else Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Successfully wrote to config characteristic: " + characteristic.getUuid());

                // Now that the sensor is configured and turned on, we can enable notifications
                // on the raw data characteristic.
                mAccelerometerCharacteristic = mBlueIOService.getCharacteristic(ACCELEROMETER_CHAR_UUID);
                if (mAccelerometerCharacteristic != null) {
                    Log.d(TAG, "Enabling notifications for Raw Accelerometer Data.");
                    setCharacteristicNotification(gatt, mAccelerometerCharacteristic, true);
                } else {
                    Log.w(TAG, "Raw Accelerometer characteristic not found after config write.");
                }
            } else {
                Log.e(TAG, "Failed to write to config characteristic. Status: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mOnCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mOnDescriptorWrite(gatt, descriptor, status);
        }
    };

    /*█╗░░██╗░█████╗░████████╗██╗███████╗██╗░░░██╗
    ████╗░██║██╔══██╗╚══██╔══╝██║██╔════╝╚██╗░██╔╝
    ██╔██╗██║██║░░██║░░░██║░░░██║█████╗░░░╚████╔╝░
    ██║╚████║██║░░██║░░░██║░░░██║██╔══╝░░░░╚██╔╝░░
    ██║░╚███║╚█████╔╝░░░██║░░░██║██║░░░░░░░░██║░░░
    ╚═╝░░╚══╝░╚════╝░░░░╚═╝░░░╚═╝╚═╝░░░░░░░░╚═╝░*/

    // Enables or disables notification/indication for a given characteristic.
    @SuppressLint("MissingPermission")
    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enable) {
        // Enable Notifications on the GATT Layer after checking permissions
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission missing for setCharacteristicNotification.");
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Permission missing to enable notifications.", Toast.LENGTH_SHORT).show());
            return;
        }
        gatt.setCharacteristicNotification(characteristic, enable);

        // Find the configuration descriptor (specifically CCCD) that controls notifications and notfications
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCCD_UUID);
        if (descriptor == null) {
            Log.e(TAG, "CCCD descriptor not found for characteristic: " + characteristic.getUuid().toString());
            return;
        }

        // Set the Correct Value
        byte[] value;

        // Checks if notification is enabled, and if so, set the value later for the descriptor
        if (enable) {
            // Characteristic supports Notifications
            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                Log.d(TAG, "Enabling notifications for " + characteristic.getUuid().toString());
            }
            // Characteristic supports Indications
            else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
                Log.d(TAG, "Enabling indications for " + characteristic.getUuid().toString());
            }
            // Characteristic supports Neither
            else {
                Log.w(TAG, "Characteristic does not support notifications or indications: " + characteristic.getUuid().toString());
                return;
            }
        }
        // If notifications are not enabled, stop the flow of data
        else {
            value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            Log.d(TAG, "Disabling notifications for " + characteristic.getUuid().toString());
        }

        // Write the descriptor (sends updated value to the BlueIOThingy's CCCD)
        descriptor.setValue(value);
        gatt.writeDescriptor(descriptor);
    }

    public void mOnCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (ACCELEROMETER_CHAR_UUID.equals(characteristic.getUuid())) {
            byte[] data = characteristic.getValue();

            if (data != null && data.length >= 18) {
                int xRaw = BytesToInt(data[0],data[1]);
                int yRaw = BytesToInt(data[2],data[3]);
                int zRaw = BytesToInt(data[4],data[5]);

                float xAccel = xRaw / 256.0f;
                float yAccel = yRaw / 256.0f;
                float zAccel = zRaw / 256.0f;

                /*long currentTime = System.currentTimeMillis();

                xSum += xAccel;
                ySum += yAccel;
                zSum += zAccel;
                dataCount++;

                if (currentTime - lastLoggedTime >= 10) { */
                    // Log.d(TAG, String.format("X Raw: %d, Y Raw: %d, Z Raw: %d", xRaw, yRaw, zRaw));
                    // Log.d(TAG, String.format("Accel (g): X=%.4f, Y=%.4f, Z=%.4f", xAccel, yAccel, zAccel));
                    addAccelerometerEntry(xAccel, yAccel, zAccel);

                    /* Reset for next interval
                    xSum = 0;
                    ySum = 0;
                    zSum = 0;
                    dataCount = 0;
                    lastLoggedTime = currentTime; // Update the last logged time
                } */
            } else {
                Log.w(TAG, "Received empty or null data packet.");
            }
        }
    }

    public static short BytesToInt(byte byte1, byte byte2) {
        byte[] bytes = {byte2,byte1};
        return new BigInteger(bytes).shortValue();
    }

    public void mOnDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        // Checks if descriptor write operation is successful
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Descriptor written successfully: " + descriptor.getUuid().toString());

            if (CCCD_UUID.equals(descriptor.getUuid())) {
                notificationEnabled = true;
                Log.d(TAG, "Notifications enabled for Accelerometer Characteristic.");
            }
        }
        else Log.e(TAG, "Descriptor write failed: " + status);
    }

    /*████╗░██╗░██████╗░█████╗░░█████╗░███╗░░██╗███╗░░██╗███████╗░█████╗░████████╗
    ██╔══██╗██║██╔════╝██╔══██╗██╔══██╗████╗░██║████╗░██║██╔════╝██╔══██╗╚══██╔══╝
    ██║░░██║██║╚█████╗░██║░░╚═╝██║░░██║██╔██╗██║██╔██╗██║█████╗░░██║░░╚═╝░░░██║░░░
    ██║░░██║██║░╚═══██╗██║░░██╗██║░░██║██║╚████║██║╚████║██╔══╝░░██║░░██╗░░░██║░░░
    ██████╔╝██║██████╔╝╚█████╔╝╚█████╔╝██║░╚███║██║░╚███║███████╗╚█████╔╝░░░██║░░░
    ╚═════╝░╚═╝╚═════╝░░╚════╝░░╚════╝░╚═╝░░╚══╝╚═╝░░╚══╝╚══════╝░╚════╝░░░░╚═╝░*/

    @SuppressLint("MissingPermission")
    private void disconnectFromDevice() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or not connected.");
            return;
        }

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission missing for disconnect.");
            return;
        }

        Log.d(TAG, "Disconnecting from GATT server.");
        // This call will trigger the onConnectionStateChange callback.
        mBluetoothGatt.disconnect();
        runOnUiThread(() -> {
            chartLayout.setVisibility(View.GONE);
        });
    }

    /*████╗░██╗░░██╗░█████╗░██████╗░████████╗
    ██╔══██╗██║░░██║██╔══██╗██╔══██╗╚══██╔══╝
    ██║░░╚═╝███████║███████║██████╔╝░░░██║░░░
    ██║░░██╗██╔══██║██╔══██║██╔══██╗░░░██║░░░
    ╚█████╔╝██║░░██║██║░░██║██║░░██║░░░██║░░░
    ░╚════╝░╚═╝░░╚═╝╚═╝░░╚═╝╚═╝░░╚═╝░░░╚═╝░*/

    // --- Chart Setup (retained from BlueIOThingy demo code) ---
    private void setupChart(LineChart chart, String description) {
        chart.getDescription().setText(description);
        chart.setNoDataText("No data yet.");
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        // Set up axes
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false); // Disable right axis if not needed

        // Set axis titles
        chart.getDescription().setText("X-axis: Time (ms) | Y-axis: Acceleration (g)");
        chart.getLegend().setEnabled(false);

        // Add initial empty data
        chart.setData(new LineData());
        chart.invalidate();
    }

    private void addAccelerometerEntry(float x, float y, float z) {
        LineData data = accelerometerChart.getData();

        if (data != null) {
            // Ensure datasets for all three axes are created and available from the start
            if (data.getDataSetCount() == 0) {
                data.addDataSet(createDataSet("X-Axis", getResources().getColor(android.R.color.holo_red_light)));
                data.addDataSet(createDataSet("Y-Axis", getResources().getColor(android.R.color.holo_green_light)));
                data.addDataSet(createDataSet("Z-Axis", getResources().getColor(android.R.color.holo_blue_light)));
            }

            ILineDataSet setX = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(2);

            // Add a new entry to each dataset regardless of visibility.
            // This keeps the data arrays in sync.
            data.addEntry(new Entry(dataPointCount, x), 0);
            data.addEntry(new Entry(dataPointCount, y), 1);
            data.addEntry(new Entry(dataPointCount, z), 2);

            // Now, set the visibility of each dataset based on the toggle button state.
            // This is the correct way to show/hide lines without breaking the chart.
            setX.setVisible(showXAxisButton.isChecked());
            setY.setVisible(showYAxisButton.isChecked());
            setZ.setVisible(showZAxisButton.isChecked());

            data.notifyDataChanged();

            // Let the chart know its data has changed
            accelerometerChart.notifyDataSetChanged();

            // Limit the number of visible entries
            accelerometerChart.setVisibleXRangeMaximum(50); // Show 50 entries at a time

            // Move to the latest entry
            accelerometerChart.moveViewToX(data.getEntryCount());

            // Add to data entry if the user decides to record it
            if (isRecording) {
                writeDataToFile(x, y, z, System.currentTimeMillis() - firstLoggedTime);
            }

            // Increase coordinate on x-axis of chart
            dataPointCount++;
        }
    }

    private LineDataSet createDataSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setMode(LineDataSet.Mode.LINEAR); // Use linear mode for smooth lines
        set.setDrawValues(false); // Do not draw values on the chart
        return set;
    }

    /*████╗░███████╗░█████╗░░█████╗░██████╗░██████╗░
    ██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔══██╗██╔══██╗
    ██████╔╝█████╗░░██║░░╚═╝██║░░██║██████╔╝██║░░██║
    ██╔══██╗██╔══╝░░██║░░██╗██║░░██║██╔══██╗██║░░██║
    ██║░░██║███████╗╚█████╔╝╚█████╔╝██║░░██║██████╔╝
    ╚═╝░░╚═╝╚══════╝░╚════╝░░╚════╝░╚═╝░░╚═╝╚═════*/

    // Getting a FileOutputStream
    public void getFileOutputStream() {
        try {
            fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            // Write the header row for the CSV file
            String header = "Time,X_g,Y_g,Z_g\n";
            fileOutputStream.write(header.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read the accelerometer data from the device's private file
    public void readDataFromFile() {
    }

    // Write a log of the current accelerometer data into the device's private file
    public void writeDataToFile(float x, float y, float z, long time) {
        String data = (int)(time) + "," + x + "," + y + "," + z + "\n";
        try {
            fileOutputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Close file output stream
    public void closeFileOutputStream() {
        try {
            if (fileOutputStream != null) fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Show popup screen when done
    private void showDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.download_popup);
        dialog.show();

        // Plot results on the chart inside of the popup dialog box
        resultChart = dialog.findViewById(R.id.accelerometerResultChart);
        avgForceTxt = dialog.findViewById(R.id.avgForceTxtView);
        maxForceTxt = dialog.findViewById(R.id.maxForceTxtView);
        avgAccelTxt = dialog.findViewById(R.id.avgAccelTxtView);
        maxAccelTxt = dialog.findViewById(R.id.maxAccelTxtView);

        ArrayList<Entry> xEntries = new ArrayList<>();
        ArrayList<Entry> yEntries = new ArrayList<>();
        ArrayList<Entry> zEntries = new ArrayList<>();

        float totalAcceleration = 0, maxAcceleration = 0;

        try {
            FileInputStream fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            // Skip header line
            bufferedReader.readLine();
            int dataPointCtr = 0;

            while ((line = bufferedReader.readLine()) != null) {
                // Row format is : Time, x_g, y_g, z_g
                String[] data = line.split(",");
                if (data.length == 4) {
                    try {
                        float timestamp = Float.parseFloat(data[0]);
                        float x = Float.parseFloat(data[1]);
                        float y = Float.parseFloat(data[2]);
                        float z = Float.parseFloat(data[3]);

                        // Add entries on the chart for each axis
                        xEntries.add(new Entry(dataPointCtr, x));
                        yEntries.add(new Entry(dataPointCtr, y));
                        zEntries.add(new Entry(dataPointCtr, z));

                        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
                        totalAcceleration += acceleration;
                        maxAcceleration = Math.max(maxAcceleration, acceleration);
                        dataPointCtr++;
                    } catch (NumberFormatException e) {
                        Log.e("CSV_PARSE_ERROR", "Invalid number format in CSV file: " + line);
                    }
                }
            }
            bufferedReader.close();

            // Calculate average values for force and acceleration
            avgAccelTxt.setText(String.format("Average Force (N) : %.2f N", (dataPointCtr > 0) ? totalAcceleration / dataPointCtr : 0));
            maxAccelTxt.setText(String.format("Peak Acceleration (g) : %.2f g", maxAcceleration));
            avgForceTxt.setText(String.format("Average Acceleration (g) : %.2f g", (dataPointCtr > 0) ? totalAcceleration * userWeight / dataPointCtr : 0));
            maxForceTxt.setText(String.format("Peak Force (N) : %.2f N", maxAcceleration * userWeight));

            // Create and add datasets to the chart
            LineDataSet xSet = createDataSetForPlot(xEntries, "X-Axis", getResources().getColor(android.R.color.holo_red_light));
            LineDataSet ySet = createDataSetForPlot(yEntries, "Y-Axis", getResources().getColor(android.R.color.holo_green_light));
            LineDataSet zSet = createDataSetForPlot(zEntries, "Z-Axis", getResources().getColor(android.R.color.holo_blue_light));

            LineData lineData = new LineData(xSet, ySet, zSet);
            resultChart.setData(lineData);

            // Customize the chart appearance
            resultChart.getDescription().setEnabled(false);
            resultChart.setTouchEnabled(true);
            resultChart.setDragEnabled(true);
            resultChart.setScaleEnabled(true);
            resultChart.setPinchZoom(true);
            resultChart.setScaleXEnabled(true);
            resultChart.setScaleYEnabled(true);
            resultChart.setVisibleXRangeMaximum(200f);
            resultChart.moveViewToX(lineData.getEntryCount());

            // Refresh the chart
            resultChart.invalidate();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading log file.", Toast.LENGTH_SHORT).show();
        }

        // Button to download the cvs file to device's download folder
        Button downloadButton = dialog.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> {
            exportFileToPublicDirectory();
        });
    }

    private LineDataSet createDataSetForPlot(ArrayList<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setDrawCircles(false);
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return dataSet;
    }

    private void exportFileToPublicDirectory() {
        // Your logic to copy the file from internal storage to a public directory (e.g., Downloads)
        // You'll need to request WRITE_EXTERNAL_STORAGE permission if targeting older Android versions.
        // For modern Android, use MediaStore API.
        try {
            File privateFile = new File(getFilesDir(), filename);
            File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File publicFile = new File(publicDir, filename);

            if (!publicDir.exists()) {
                publicDir.mkdirs();
            }

            InputStream inputStream = new FileInputStream(privateFile);
            OutputStream outputStream = new FileOutputStream(publicFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            Toast.makeText(this, "File saved to Downloads folder!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save file.", Toast.LENGTH_SHORT).show();
        }
    }
}