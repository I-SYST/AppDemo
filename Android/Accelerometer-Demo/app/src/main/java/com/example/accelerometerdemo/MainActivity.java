package com.example.accelerometerdemo;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "BlueIO_Accelerometer";

    // BlueIO device information
    public static String targetDeviceName = "BlueIOThingy";
    private static final String BLUEIO_UUID_SERVICE = "ef680400-9b35-4933-9b10-52ffa9740042";

    // Raw Data (Accelerometer)
    private static final String ACCELEROMETER_CHAR_UUID_STRING = "ef680406-9b35-4933-9b10-52ffa9740042";
    private static final UUID ACCELEROMETER_CHAR_UUID = UUID.fromString(ACCELEROMETER_CHAR_UUID_STRING);

    // Quaternion Characteristic (Used to "Prime" the ICM-20948 sensor)
    private static final String QUATERNION_CHAR_UUID_STRING = "ef680404-9b35-4933-9b10-52ffa9740042";
    private static final UUID QUATERNION_CHAR_UUID = UUID.fromString(QUATERNION_CHAR_UUID_STRING);

    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // Standard CCCD

    // Bluetooth Variables
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;


    // Bluetooth Permissions
    private static final int REQUEST_CODE_COARSE_PERMISSION = 1;
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSION = 2;
    private ActivityResultContracts.RequestMultiplePermissions requestMultiplePermissionsContract;
    private final String[] ANDROID_12_BLE_PERMISSIONS = new String[] {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private final String[] BLE_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // Bluetooth Scan Variables
    private BluetoothLeScanner mLEScanner;
    private static long SCAN_PERIOD = 5000;
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
    private long firstLoggedTime = 0;
    private float userWeight = 70.0f; // Default weight

    // Runnable to handle connection watchdog
    private Runnable connectionWatchdog;


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

        statusTextView = findViewById(R.id.statusTextView);
        chartOptionsDropDown = findViewById(R.id.chartOptionsDropDown);
        final List<String> states = Arrays.asList("Accelerometer (g)", "Force Meter (N)");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_spinner_item, states);
        adapter.setDropDownViewResource(R.layout.custom_spinner_item);
        chartOptionsDropDown.setAdapter(adapter);

        accelerometerChart = findViewById(R.id.accelerometerChart);
        setupChart(accelerometerChart, "Accelerometer Data (X, Y, Z)");

        showXAxisButton = findViewById(R.id.showXAxisButton);
        showYAxisButton = findViewById(R.id.showYAxisButton);
        showZAxisButton = findViewById(R.id.showZAxisButton);

        connectButton = findViewById(R.id.btnStartDiscovery);
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
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_spinner_item, units);
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

        // Permission handling setup
        requestMultiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        registerForActivityResult(requestMultiplePermissionsContract, isGranted -> {
            Log.d(TAG, "Permissions Launcher result: " + isGranted.toString());

            // Check if all necessary permissions are granted
            boolean allGranted = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (Boolean.FALSE.equals(isGranted.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false)) || Boolean.FALSE.equals(isGranted.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false))) {
                    allGranted = false;
                }
            }
            else {
                if (Boolean.FALSE.equals(isGranted.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false))) {
                    allGranted = false;
                }
            }

            // If the device hasn't granted permission, ask user to do so
            if (!allGranted) {
                Toast.makeText(this, "Required Bluetooth/Location permissions not granted. App may not function correctly.", Toast.LENGTH_LONG).show();
            }
            else {
                Log.d(TAG, "All necessary BLE permissions granted.");
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
        if (SDK_INT >= Build.VERSION_CODES.Q) {
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
            if (SDK_INT >= Build.VERSION_CODES.S && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Toast.makeText(this, "BLUETOOTH_CONNECT permission needed to enable Bluetooth.", Toast.LENGTH_SHORT).show();
                requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION);
                return;
            }
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
        }
    }

    // ActivityResultLauncher for enabling Bluetooth
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, "Bluetooth has been enabled.", Toast.LENGTH_SHORT).show();
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

    // Start scanning the devices nearby
    @SuppressLint("MissingPermission")
    private void startScan() {
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not enabled. Asking for permission...", Toast.LENGTH_SHORT).show();
            askBluetoothPermission();
            return;
        }

        if (mLEScanner == null) {
            Toast.makeText(this, "BLE Scanner not initialized. Bluetooth may be off or unsupported.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (SDK_INT >= Build.VERSION_CODES.S && !hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            Toast.makeText(this, "BLUETOOTH_SCAN permission required to start scan.", Toast.LENGTH_SHORT).show();
            requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION);
            return;
        }
        else if (SDK_INT < Build.VERSION_CODES.S && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this,"Location permission required to start scan.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Starting BLE scan for device: " + targetDeviceName);
        runOnUiThread(() -> statusTextView.setText("Status: Scanning for " + targetDeviceName + "..."));
        Toast.makeText(this, "Scanning for " + targetDeviceName + "...", Toast.LENGTH_SHORT).show();

        discoveredDeviceName = null;
        discoveredDeviceAddress = null;

        mHandler.postDelayed(() -> {
            if (mBluetoothAdapter.isEnabled() && mLEScanner != null) {
                if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return;
                mLEScanner.stopScan(mScanCallback);

                Log.d(TAG, "Scan stopped by timeout.");
                if (discoveredDeviceAddress == null) {
                    runOnUiThread(() -> {
                        statusTextView.setText("Status: Scan finished, device not found.");
                        Toast.makeText(MainActivity.this, "Device " + targetDeviceName + " not found.", Toast.LENGTH_SHORT).show();
                        connectButton.setText("Search for Device"); // Reset button text
                    });
                }
            }
        }, SCAN_PERIOD);

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return;
        mLEScanner.startScan(mScanCallback);
    }

    // --- BLE Scan Callback ---
    private ScanCallback mScanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();

            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                Log.d(TAG, "Check Bluetooth permissions again");
                return;
            }

            String name = device.getName();
            String address = device.getAddress();
            List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();

            if (name != null && name.equals(targetDeviceName)) {
                if (SDK_INT >= Build.VERSION_CODES.O)
                    Log.d(TAG, "Found target device: " + name + ", address: " + address + ", UUIDs " + (uuids != null ? uuids.toString() : "null"));

                discoveredDeviceName = name;
                discoveredDeviceAddress = address;

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
        if (isConnecting) {
            Log.d(TAG, "Already attempting to connect. Ignoring new request.");
            return;
        }

        if (mBluetoothAdapter == null || device == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified device.");
            return;
        }

        // Ensure previous connection is fully closed before starting new one
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Closing previous GATT connection before reconnecting.");
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        // Clear any old handlers (like timeouts from previous session)
        mHandler.removeCallbacksAndMessages(null);

        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Toast.makeText(this, "BLUETOOTH_CONNECT permission required to connect.", Toast.LENGTH_SHORT).show();
            requestBlePermissions(this, REQUEST_CODE_BLUETOOTH_PERMISSION);
            return;
        }

        isConnecting = true;
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);

        Log.d(TAG, "Attempting to create a new GATT connection.");
        runOnUiThread(() -> statusTextView.setText("Status: Connecting to " + (device.getName() != null ? device.getName() : device.getAddress()) + "..."));
    }

    // --- GATT Callbacks ---
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.d(TAG, "Connected to GATT server.");
                        isConnecting = false;
                        firstLoggedTime = System.currentTimeMillis();

                        runOnUiThread(() -> {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            statusTextView.setText("Status: Connected to " + (gatt.getDevice().getName() != null ? gatt.getDevice().getName() : gatt.getDevice().getAddress()));
                            Toast.makeText(MainActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                            connectButton.setText("Disconnect");
                            chartLayout.setVisibility(View.VISIBLE);
                        });

                        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                            Log.w(TAG, "BLUETOOTH_CONNECT permission missing for discoverServices.");
                            return;
                        }
                        // DELAY FIX: Add small delay before discovering services
                        mHandler.postDelayed(gatt::discoverServices, 500);
                        break;

                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.d(TAG, "Disconnected from GATT server.");
                        isConnecting = false;
                        notificationEnabled = false;

                        // Nullify service references to ensure fresh lookup next time
                        mBlueIOService = null;
                        mAccelerometerCharacteristic = null;

                        runOnUiThread(() -> {
                            statusTextView.setText("Status: Disconnected");
                            Toast.makeText(MainActivity.this, "Disconnected.", Toast.LENGTH_SHORT).show();

                            // Reset chart data so re-connection starts fresh
                            accelerometerChart.clear();
                            accelerometerChart.setData(new LineData()); // Empty data
                            dataPointCount = 0;

                            connectButton.setText("Search for Device");
                            chartLayout.setVisibility(View.GONE);
                        });

                        // Ensure explicit close
                        gatt.close();
                        mBluetoothGatt = null;
                        break;
                }
            }
            else {
                Log.w(TAG, "GATT connection failed with status: " + status);
                isConnecting = false;
                runOnUiThread(() -> {
                    statusTextView.setText("Status: Connection Failed (" + status + ")");
                    Toast.makeText(MainActivity.this, "Connection failed (Status: " + status + ")", Toast.LENGTH_LONG).show();
                    connectButton.setText("Search for Device");
                });
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

                mBlueIOService = gatt.getService(UUID.fromString(BLUEIO_UUID_SERVICE));
                if (mBlueIOService == null) {
                    Log.w(TAG, "BlueIO Service (UUID: " + BLUEIO_UUID_SERVICE + ") not found!");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "BlueIO Service not found!", Toast.LENGTH_LONG).show());
                    return;
                }

                // CRITICAL FIX: Set a "Watchdog" timer.
                // If the "Priming" write callback never fires (common on reconnects),
                // we force-enable the raw data stream after 1.5 seconds anyway.
                connectionWatchdog = () -> {
                    Log.w(TAG, "Watchdog triggered: Priming callback missing. Forcing Raw Data Enable.");
                    enableRawDataNotification(gatt);
                };
                mHandler.postDelayed(connectionWatchdog, 1500);

                // Start Sensor Priming Sequence
                Log.d(TAG, "Starting Sensor Priming Sequence...");
                mHandler.postDelayed(() -> enableQuaternionNotification(gatt), 600);
            }
            else Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID charUuid = descriptor.getCharacteristic().getUuid();
                Log.d(TAG, "Descriptor written successfully for Char: " + charUuid);

                // Check if we just enabled Quaternion (The "Primer")
                if (charUuid.equals(QUATERNION_CHAR_UUID)) {
                    Log.d(TAG, "Quaternion enabled (Sensor Primed). Now enabling Raw Accelerometer Data...");

                    // Cancel the watchdog, since the callback worked!
                    if (connectionWatchdog != null) mHandler.removeCallbacks(connectionWatchdog);

                    // Wait 100ms before enabling raw data
                    mHandler.postDelayed(() -> enableRawDataNotification(gatt), 100);
                }
                // Check if we just enabled Raw Data (The Goal)
                else if (charUuid.equals(ACCELEROMETER_CHAR_UUID)) {
                    notificationEnabled = true;
                    Log.d(TAG, "Notifications enabled for Accelerometer Characteristic. Data flow should start.");
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Sensor Ready & Streaming", Toast.LENGTH_SHORT).show());
                }
            }
            else {
                Log.e(TAG, "Descriptor write failed: " + status);
                // If priming failed, try skipping to raw data immediately
                if (descriptor.getCharacteristic().getUuid().equals(QUATERNION_CHAR_UUID)) {
                    Log.w(TAG, "Priming failed. Attempting to force Raw Data...");
                    mHandler.postDelayed(() -> enableRawDataNotification(gatt), 100);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            mOnCharacteristicChanged(gatt, characteristic);
        }
    };

    // --- Helper to Enable Quaternion (Priming) ---
    @SuppressLint("MissingPermission")
    private void enableQuaternionNotification(BluetoothGatt gatt) {
        if (mBlueIOService == null) {
            // Safety check: refresh service if null
            mBlueIOService = gatt.getService(UUID.fromString(BLUEIO_UUID_SERVICE));
        }
        if (mBlueIOService == null) return;

        BluetoothGattCharacteristic quatCharacteristic = mBlueIOService.getCharacteristic(QUATERNION_CHAR_UUID);
        if (quatCharacteristic != null) {
            setCharacteristicNotification(gatt, quatCharacteristic, true);
        } else {
            Log.w(TAG, "Quaternion characteristic not found! Cannot prime sensor. Trying Raw Data directly.");
            enableRawDataNotification(gatt);
        }
    }

    // --- Helper to Enable Raw Data ---
    @SuppressLint("MissingPermission")
    private void enableRawDataNotification(BluetoothGatt gatt) {
        if (mBlueIOService == null) {
            mBlueIOService = gatt.getService(UUID.fromString(BLUEIO_UUID_SERVICE));
        }
        if (mBlueIOService == null) return;

        mAccelerometerCharacteristic = mBlueIOService.getCharacteristic(ACCELEROMETER_CHAR_UUID);
        if (mAccelerometerCharacteristic != null) {
            setCharacteristicNotification(gatt, mAccelerometerCharacteristic, true);
        } else {
            Log.w(TAG, "Raw Accelerometer characteristic not found.");
        }
    }

    /*█╗░░██╗░█████╗░████████╗██╗███████╗██╗░░░██╗
    ████╗░██║██╔══██╗╚══██╔══╝██║██╔════╝╚██╗░██╔╝
    ██╔██╗██║██║░░██║░░░██║░░░██║█████╗░░░╚████╔╝░
    ██║╚████║██║░░██║░░░██║░░░██║██╔══╝░░░░╚██╔╝░░
    ██║░╚███║╚█████╔╝░░░██║░░░██║██║░░░░░░░░██║░░░
    ╚═╝░░╚══╝░╚════╝░░░░╚═╝░░░╚═╝╚═╝░░░░░░░░╚═╝░*/

    // Enables or disables notification/indication for a given characteristic.
    @SuppressLint("MissingPermission")
    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enable) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission missing for setCharacteristicNotification.");
            return;
        }

        // 1. Set Local Notification
        boolean set = gatt.setCharacteristicNotification(characteristic, enable);
        Log.d(TAG, "Local setCharacteristicNotification for " + characteristic.getUuid() + " returned: " + set);

        // 2. Write to CCCD Descriptor
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCCD_UUID);
        if (descriptor == null) {
            Log.e(TAG, "CCCD descriptor not found for characteristic: " + characteristic.getUuid().toString());
            return;
        }

        byte[] value;
        if (enable) {
            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            }
            else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
            }
            else {
                return;
            }
        }
        else {
            value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }

        descriptor.setValue(value);
        gatt.writeDescriptor(descriptor);
    }

    public void mOnCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // Handle Raw Data (Accelerometer)
        if (ACCELEROMETER_CHAR_UUID.equals(characteristic.getUuid())) {
            byte[] data = characteristic.getValue();

            if (data != null && data.length >= 6) { // X, Y, Z (2 bytes each)
                int xRaw = BytesToInt(data[0],data[1]);
                int yRaw = BytesToInt(data[2],data[3]);
                int zRaw = BytesToInt(data[4],data[5]);

                float xAccel = xRaw / 256.0f;
                float yAccel = yRaw / 256.0f;
                float zAccel = zRaw / 256.0f;

                runOnUiThread(() -> addAccelerometerEntry(xAccel, yAccel, zAccel));
            }
        }
        // Note: We ignore incoming Quaternion data here, it's just used to keep the sensor alive.
    }

    public static short BytesToInt(byte byte1, byte byte2) {
        byte[] bytes = {byte2,byte1};
        return new BigInteger(bytes).shortValue();
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

        // VERY IMPORTANT: Stop any pending tasks (like the watchdog or priming sequence)
        mHandler.removeCallbacksAndMessages(null);

        mBluetoothGatt.disconnect();
    }

    /*████╗░██╗░░██╗░█████╗░██████╗░████████╗
    ██╔══██╗██║░░██║██╔══██╗██╔══██╗╚══██╔══╝
    ██║░░╚═╝███████║███████║██████╔╝░░░██║░░░
    ██║░░██╗██╔══██║██╔══██║██╔══██╗░░░██║░░░
    ╚█████╔╝██║░░██║██║░░██║██║░░██║░░░██║░░░
    ░╚════╝░╚═╝░░╚═╝╚═╝░░╚═╝╚═╝░░╚═╝░░░╚═╝░*/

    private void setupChart(LineChart chart, String description) {
        chart.getDescription().setText(description);
        chart.setNoDataText("No data yet.");
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        chart.getDescription().setText("X-axis: Time (ms) | Y-axis: Acceleration (g)");
        chart.getLegend().setEnabled(false);

        chart.setData(new LineData());
        chart.invalidate();
    }

    private void addAccelerometerEntry(float x, float y, float z) {
        LineData data = accelerometerChart.getData();

        if (data != null) {
            if (data.getDataSetCount() == 0) {
                data.addDataSet(createDataSet("X-Axis", getResources().getColor(android.R.color.holo_red_light)));
                data.addDataSet(createDataSet("Y-Axis", getResources().getColor(android.R.color.holo_green_light)));
                data.addDataSet(createDataSet("Z-Axis", getResources().getColor(android.R.color.holo_blue_light)));
            }

            ILineDataSet setX = data.getDataSetByIndex(0);
            ILineDataSet setY = data.getDataSetByIndex(1);
            ILineDataSet setZ = data.getDataSetByIndex(2);

            data.addEntry(new Entry(dataPointCount, x), 0);
            data.addEntry(new Entry(dataPointCount, y), 1);
            data.addEntry(new Entry(dataPointCount, z), 2);

            setX.setVisible(showXAxisButton.isChecked());
            setY.setVisible(showYAxisButton.isChecked());
            setZ.setVisible(showZAxisButton.isChecked());

            data.notifyDataChanged();
            accelerometerChart.notifyDataSetChanged();
            accelerometerChart.setVisibleXRangeMaximum(50);
            accelerometerChart.moveViewToX(data.getEntryCount());

            if (isRecording) {
                writeDataToFile(x, y, z, System.currentTimeMillis() - firstLoggedTime);
            }

            dataPointCount++;
        }
    }

    private LineDataSet createDataSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false);
        return set;
    }

    /*████╗░███████╗░█████╗░░█████╗░██████╗░██████╗░
    ██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔══██╗██╔══██╗
    ██████╔╝█████╗░░██║░░╚═╝██║░░██║██████╔╝██║░░██║
    ██╔══██╗██╔══╝░░██║░░██╗██║░░██║██╔══██╗██║░░██║
    ██║░░██║███████╗╚█████╔╝╚█████╔╝██║░░██║██████╔╝
    ╚═╝░░╚═╝╚══════╝░╚════╝░░╚════╝░╚═╝░░╚═╝╚═════*/

    public void getFileOutputStream() {
        try {
            fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            String header = "Time,X_g,Y_g,Z_g\n";
            fileOutputStream.write(header.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDataToFile(float x, float y, float z, long time) {
        String data = (int)(time) + "," + x + "," + y + "," + z + "\n";
        try {
            fileOutputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFileOutputStream() {
        try {
            if (fileOutputStream != null) fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.download_popup);
        dialog.show();

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

            bufferedReader.readLine(); // Skip header
            int dataPointCtr = 0;

            while ((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    try {
                        float x = Float.parseFloat(data[1]);
                        float y = Float.parseFloat(data[2]);
                        float z = Float.parseFloat(data[3]);

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

            avgAccelTxt.setText(String.format("Average Accel (g) : %.2f g", (dataPointCtr > 0) ? totalAcceleration / dataPointCtr : 0));
            maxAccelTxt.setText(String.format("Peak Accel (g) : %.2f g", maxAcceleration));
            avgForceTxt.setText(String.format("Average Force (N) : %.2f N", (dataPointCtr > 0) ? (totalAcceleration * 9.8f * userWeight / dataPointCtr) : 0));
            maxForceTxt.setText(String.format("Peak Force (N) : %.2f N", maxAcceleration * 9.8f * userWeight));

            LineDataSet xSet = createDataSetForPlot(xEntries, "X-Axis", getResources().getColor(android.R.color.holo_red_light));
            LineDataSet ySet = createDataSetForPlot(yEntries, "Y-Axis", getResources().getColor(android.R.color.holo_green_light));
            LineDataSet zSet = createDataSetForPlot(zEntries, "Z-Axis", getResources().getColor(android.R.color.holo_blue_light));

            LineData lineData = new LineData(xSet, ySet, zSet);
            resultChart.setData(lineData);

            resultChart.getDescription().setEnabled(false);
            resultChart.setTouchEnabled(true);
            resultChart.setDragEnabled(true);
            resultChart.setScaleEnabled(true);
            resultChart.setPinchZoom(true);
            resultChart.setVisibleXRangeMaximum(200f);
            resultChart.moveViewToX(lineData.getEntryCount());
            resultChart.invalidate();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error reading log file.", Toast.LENGTH_SHORT).show();
        }

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

    // There is this small issue to fix: Failed to delete file: accelerometer_data.csv
    private void exportFileToPublicDirectory() {
        try {
            File privateFile = new File(getFilesDir(), filename);
            File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File publicFile = new File(publicDir, filename);

            if (!publicDir.exists()) {
                publicDir.mkdirs();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(privateFile));
            FileOutputStream outputStream = new FileOutputStream(publicFile);
            FileInputStream inputStream = new FileInputStream(privateFile);

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