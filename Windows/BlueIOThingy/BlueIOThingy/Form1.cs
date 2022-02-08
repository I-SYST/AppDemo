using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Windows.Devices.Bluetooth;
using Windows.Devices.Bluetooth.Advertisement;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using Windows.Storage.Streams;
using System.Threading;
using System.Windows.Forms.DataVisualization.Charting;

namespace BlueIOThingy
{
    public partial class Form1 : Form
    {
        GattDeviceService service = null;
        GattCharacteristic charac = null;
        Guid MyService_GUID;
        Guid MYCharacteristic_GUID;
        string bleDevicName = "BlueIOThingy";// !!!your device name!!!
        long deviceFoundMilis = 0, serviceFoundMilis = 0;
        long connectedMilis = 0, characteristicFoundMilis = 0;
        long WriteDescriptorMilis = 0;
        Stopwatch stopwatch;
        long xValue = 0;
        bool scanFlag = true;
        public List<string> MyList
        {
            get; set;
        }
        Series TempSeries = null;
        Series HumiSeries = null;
        Series PressSeries = null;
        public Form1()
        {
            InitializeComponent();
            stopwatch = new Stopwatch();
            // !!!Your service !!!
            MyService_GUID = new Guid("ef680100-9b35-4933-9b10-52ffa9740042");
            //!!!Your characteristic!!!
            MYCharacteristic_GUID = new Guid("{ef680200-9b35-4933-9b10-52ffa9740042}");
            MyList = new List<string>();
            SplineChartExample();
        }

        private void SplineChartExample()
        {
            TempChart.Series.Clear();
            TempSeries = TempChart.Series.Add("Temeprature");
            TempSeries.ChartType = SeriesChartType.Spline;
            TempSeries.Color = Color.Red;
            TempSeries.BorderWidth = 2;
            TempChart.ChartAreas[0].AxisY.IsStartedFromZero = false;
            TempChart.ChartAreas[0].RecalculateAxesScale();


            HumiChart.Series.Clear();
            HumiSeries = HumiChart.Series.Add("Humidity");
            HumiSeries.ChartType = SeriesChartType.Spline;
            HumiSeries.Color = Color.Green;
            HumiSeries.BorderWidth = 2;
            HumiChart.ChartAreas[0].AxisY.IsStartedFromZero = false;
            HumiChart.ChartAreas[0].RecalculateAxesScale();

            PressChart.Series.Clear();
            PressSeries = PressChart.Series.Add("Pressure");
            PressSeries.ChartType = SeriesChartType.Spline;
            PressSeries.Color = Color.Blue;
            PressSeries.BorderWidth = 2;
            PressChart.ChartAreas[0].AxisY.IsStartedFromZero = false;
            PressChart.ChartAreas[0].RecalculateAxesScale();
        }
        private void scanBtn_Click(object sender, EventArgs e)
        {
            scanFlag = true;

            StartUtility();
        }

        private void stopBtn_Click(object sender, EventArgs e)
        {
            scanFlag = false;
        }
        private void StartUtility()
        {
            Thread thread = new Thread(new ThreadStart(test));
            thread.Start();
        }
        public void test()
        {
            while (scanFlag)
            {
                Thread.Sleep(5000);
                StartWatching();
            }
        }

        private void StartWatching()
        {
            // Create Bluetooth Listener
            var watcher = new BluetoothLEAdvertisementWatcher
            {
                //Set scanning mode.
                //Active means get all the possible information in the advertisement data.
                //Use Passive if you already know the Ble-Address and only want to connect.
                //Scanning mode Passive is Action lot faster.
                ScanningMode = BluetoothLEScanningMode.Active
            };
            // Register callback for when we see an advertisements
            watcher.Received += OnAdvertisementReceivedAsync;
            stopwatch.Start();
            watcher.Start();

        }


        private async void OnAdvertisementReceivedAsync(BluetoothLEAdvertisementWatcher watcher,
                                                        BluetoothLEAdvertisementReceivedEventArgs eventArgs)
        {

            // Filter for specific Device by name
            if (eventArgs.Advertisement.LocalName == bleDevicName)
            {
                watcher.Stop();
                var device = await BluetoothLEDevice.FromBluetoothAddressAsync(eventArgs.BluetoothAddress);
                //always check for null!!
                if (device != null)
                {
                    deviceFoundMilis = stopwatch.ElapsedMilliseconds;
                    //Debug.WriteLine("Device found in " + deviceFoundMilis + " ms");
                    //MyList.Add("Device found in " + deviceFoundMilis + " ms");
                    listBox1.Invoke(new Action(() => listBox1.Items.Add("Device found in " + deviceFoundMilis + " ms")));
                    var rssi = eventArgs.RawSignalStrengthInDBm;
                    //Debug.WriteLine("Signalstrengt = " + rssi + " DBm");
                    //MyList.Add("Signalstrengt = " + rssi + " DBm");
                    listBox1.Invoke(new Action(() => listBox1.Items.Add("Signalstrengt = " + rssi + " DBm")));
                    rssiLabel.Invoke(new Action(() => rssiLabel.Text = rssi + " DBm"));
                    var bleAddress = eventArgs.BluetoothAddress;
                    byte[] address_bytes = BitConverter.GetBytes(bleAddress);
                    string addressStr = BitConverter.ToString(address_bytes);
                    //Debug.WriteLine("Ble address = " + bleAddress);
                    //MyList.Add("Ble address = " + bleAddress);
                    listBox1.Invoke(new Action(() => listBox1.Items.Add("Ble address = " + addressStr)));
                    var advertisementType = eventArgs.AdvertisementType;
                    //Debug.WriteLine("Advertisement type = " + advertisementType);
                    //MyList.Add("Advertisement type = " + advertisementType);
                    //listBox1.Invoke(new Action(() => listBox1.Items.Add("Advertisement type = " + advertisementType)));

                    var advertiserment = eventArgs.Advertisement;
                    var manuData = advertiserment.ManufacturerData;
                    //var manu = advertiserment.GetManufacturerDataByCompanyId(0x177);
                    //var advData = advertiserment.DataSections;
                    if (manuData.Count > 0)
                    {
                        var manufacturerData = manuData[0];
                        var data = new byte[manufacturerData.Data.Length];
                        using (var reader = DataReader.FromBuffer(manufacturerData.Data))
                        {
                            reader.ReadBytes(data);
                        }
                        // Print the company ID + the raw data in hex format
                        string manufacturerDataString = string.Format("0x{0}: {1}",
                            manufacturerData.CompanyId.ToString("X"),
                            BitConverter.ToString(data));
                        if (data[0] == 1)
                        {
                            listBox1.Invoke(new Action(() => listBox1.Items.Add("Advertiserment Data = " + manufacturerDataString)));
                            byte[] pressBytes = new byte[4];
                            System.Buffer.BlockCopy(data, 1, pressBytes, 0, 4);
                            int press = BitConverter.ToInt32(pressBytes, 0);
                            string pressStr = String.Format("{0:0.00} KPa", (float)press / 1000.0);

                            byte[] tempBytes = new byte[2];
                            System.Buffer.BlockCopy(data, 5, tempBytes, 0, 2);
                            int temp = BitConverter.ToInt16(tempBytes, 0);
                            string tempStr = String.Format("{0:0.00} C", (float)temp / 100.0);

                            byte[] humiBytes = new byte[2];
                            System.Buffer.BlockCopy(data, 7, humiBytes, 0, 2);
                            int humi = BitConverter.ToInt16(humiBytes, 0);
                            string humiStr = String.Format("{0:0.00} C", (float)humi / 100.0);
                            xValue += 1;
                            listBox2.Invoke(new Action(() => listBox2.Items.Add("Pressure: " + pressStr + "; Temperature: " + tempStr + "; Humidity: " + humiStr)));
                            TempChart.Invoke(new Action(() => TempSeries.Points.AddXY(xValue, (float)temp / 100.0)));
                            tempLabel.Invoke(new Action(() => tempLabel.Text = tempStr));
                            TempChart.Invoke(new Action(() => TempChart.ChartAreas[0].RecalculateAxesScale()));
                            HumiChart.Invoke(new Action(() => HumiSeries.Points.AddXY(xValue, (float)humi / 100.0)));
                            humiLabel.Invoke(new Action(() => humiLabel.Text = humiStr));
                            HumiChart.Invoke(new Action(() => HumiChart.ChartAreas[0].RecalculateAxesScale()));
                            PressChart.Invoke(new Action(() => PressSeries.Points.AddXY(xValue, (float)press / 1000.0)));
                            pressLabel.Invoke(new Action(() => pressLabel.Text = pressStr));
                            PressChart.Invoke(new Action(() => PressChart.ChartAreas[0].RecalculateAxesScale()));
                        }
                    }
                }
                else
                {
                    //Debug.WriteLine("No device found");
                    //MyList.Add("No device found");
                    listBox1.Invoke(new Action(() => listBox1.Items.Add("No device found")));
                }
            }
        }
    }
}
