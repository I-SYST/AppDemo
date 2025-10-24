# Accelerometer-Demo

This project is meant to display motion data provided by the ICM-20948 sensor on the BLUEIO-TAG-EVIM sensor board through BLE.

## App Features
- **Bluetooth Low Energy (BLE) Connectivity:** Connects to the sensor board using the GATT communication protocol after requesting Bluetooth permissions.

- **Real-Time Data Visualization:** Displays real-time motion data from the 9-axis ICM-20948 sensor's 3-axis accelerometer, gyroscope, and magnetometer on a dynamic motion/time chart.

- **Data Recording**: Records the displayed data to a local CSV file on the device, which can be downloaded after the session ends.

## Prerequisites

**To run and use this application, you will need the following:**

- **An Android device:** The app is designed for Android, so you will need a smartphone or tablet running Android OS. It's recommended to use a device with Android 6.0 (Marshmallow) or higher for full BLE compatibility.

- **[I-Syst's BLUEIO-TAG-EVIM sensor board:](https://micropython.org/download/BLUEIO_TAG_EVIM/)** This android application is configured for this specific sensor.

## Usage
**Follow these steps to use the Accelerometer-Demo application:**

1. **Enable Bluetooth:** Make sure Bluetooth is turned on on your Android device.

2. **Install the App:** As of now, fork this repository and launch it on Android Studio

3. **Connect to the Sensor:** Tap the "Connect" button in the app's interface to scan for and select your BLUEIO-TAG-EVIM sensor board.

4. **View Data:** Once connected, the motion data will begin streaming and be displayed in real-time on the chart.

5. **Record Data:** To start recording, tap the "Record" button. The data will be saved within the app's local storage.

6. **Download CSV:** After you have finished your session, use the "Download" option within the app to save the recorded CSV file to your device.

## License

This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/DINDIN2007/Accelerometer-Demo?tab=MIT-1-ov-file#readme) file for details.
