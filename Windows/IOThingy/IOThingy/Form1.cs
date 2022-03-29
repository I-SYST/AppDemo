using System;
using System.Collections.Generic;
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
using Windows.Devices.Enumeration;
using System.Diagnostics;
using Windows.Security.Cryptography;
using System.Drawing.Drawing2D;
using System.Numerics;

namespace IOThingy
{
    public partial class Form1 : Form
    {
        //GattDeviceService gattService = null;
        //GattCharacteristic characteristic = null;
        //Guid MyService_GUID;
        //Guid MYCharacteristic_GUID;
        string bleDevicName = "BlueIOThingy";// !!!your device name!!!
        Stopwatch stopwatch;
        bool scanFlag = true;
        ulong bleDeviceAddress = 0;

        [System.Runtime.InteropServices.DllImportAttribute("gdi32.dll")]
        private static extern bool BitBlt(IntPtr hdcDest, int nXDest, int nYDest, int nWidth, int nHeight, IntPtr hdcSrc, int nXSrc, int nYSrc, System.Int32 dwRop);

        [System.Runtime.InteropServices.DllImportAttribute("user32.dll")]
        public static extern IntPtr GetDC(IntPtr hwnd);

        [System.Runtime.InteropServices.DllImportAttribute("user32.dll")]
        public static extern IntPtr ReleaseDC(IntPtr hwnd, IntPtr hdc);

        Math3D.Cube mainCube;
        Point drawOrigin;
        public float RotateX = (float)0;
        public float RotateY = (float)0;
        public float RotateZ = (float)0;
        public Form1()
        {
            InitializeComponent();
            stopwatch = new Stopwatch();
      
  
        }
        private void Form1_Load(object sender, EventArgs e)
        {
            mainCube = new Math3D.Cube(200, 200, 200);
            mainCube.DrawWires = true; 
            mainCube.FillFront = true;
            mainCube.FillBack = true;
            mainCube.FillLeft = true;
            mainCube.FillRight = true;
            mainCube.FillTop = true;
            mainCube.FillBottom = true;
            drawOrigin = new Point(pictureBox1.Width / 2, pictureBox1.Height / 2);
        }

        private void Form1_Paint(object sender, PaintEventArgs e)
        {
            
            Render();
        }

        private void Render()
        {
            mainCube.RotateX = RotateX;
            mainCube.RotateY = RotateY;
            mainCube.RotateZ = RotateZ;

            pictureBox1.Image = mainCube.DrawCube(drawOrigin);
        }
        private void DisconnectBtn_Click(object sender, EventArgs e)
        {
            scanFlag = false;
            stopwatch.Stop();
            
        }
        private void ConnectBtn_Click(object sender, EventArgs e)
        {
            scanFlag = true;
            StartUtility();
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
                Thread.Sleep(500);
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
                    var gattService = (await device.GetGattServicesForUuidAsync(Guid.Parse("ef680400-9b35-4933-9b10-52ffa9740042"))).Services.FirstOrDefault();
                    if (gattService != null)
                    {
                        Console.WriteLine($"[{device.Name}] Get GATT characteristics");
                        var gattCharacteristicsResult = await gattService.GetCharacteristicsForUuidAsync(new Guid("ef680404-9b35-4933-9b10-52ffa9740042"));
                        Console.WriteLine($"[{device.Name}] GATT Characteristics result: status={gattCharacteristicsResult?.Status}, count={gattCharacteristicsResult?.Characteristics?.Count}, cx={device.ConnectionStatus}");

                        if (gattCharacteristicsResult == null
                            || gattCharacteristicsResult.Status != GattCommunicationStatus.Success
                            || gattCharacteristicsResult.Characteristics == null
                            || gattCharacteristicsResult.Characteristics?.Count < 1)
                        {
                            Console.WriteLine($"[{device.Name}] Failed to find GATT characteristic.");
                            return;
                        }

                        var characteristic = gattCharacteristicsResult.Characteristics[0];

                        // register for notifications
                        Thread.Sleep(150);

                        characteristic.ValueChanged += (sender, args) =>
                        {
                            Console.WriteLine($"[{device.Name}] Received notification containing {args.CharacteristicValue.Length} bytes");                           
                            byte[] data;
                            CryptographicBuffer.CopyToByteArray(args.CharacteristicValue, out data);

                            byte[] WData = new byte[4];
                            System.Buffer.BlockCopy(data, 0, WData, 0, 4);
                            float W = (float)BitConverter.ToInt32(WData, 0)/ (float)(1 << 30);
                            string Wvalue = String.Format("{0:0.00}", W);

                            byte[] XData = new byte[4];
                            System.Buffer.BlockCopy(data, 4, XData, 0, 4);
                            float X = (float)BitConverter.ToInt32(XData, 0) / (float)(1 << 30);
                            string Xvalue = String.Format("{0:0.00}", X);

                            byte[] YData = new byte[4];
                            System.Buffer.BlockCopy(data, 8, YData, 0, 4);
                            float Y = (float)BitConverter.ToInt32(YData, 0) / (float)(1 << 30);
                            string Yvalue = String.Format("{0:0.00}", Y);

                            byte[] ZData = new byte[4];
                            System.Buffer.BlockCopy(data, 12, ZData, 0, 4);
                            float Z = (float)BitConverter.ToInt32(ZData, 0) / (float)(1 << 30);
                            string Zvalue = String.Format("{0:0.00}", Z);

                            double sqw = W * W;
                            double sqx = X * X;
                            double sqy = Y * Y;
                            double sqz = Z * Z;

                            RotateY = (float)Math.Atan2(2f * X * W + 2f * Y * Z, 1 - 2f * (sqz + sqw));     // Yaw 
                            RotateY = RotateY * 180 / (float)Math.PI;
                            RotateX = (float)Math.Asin(2f * (X * Z - W * Y));                             // Pitch 
                            RotateX = RotateX * 180 / (float)Math.PI;
                            RotateZ = (float)Math.Atan2(2f * X * Y + 2f * Z * W, 1 - 2f * (sqy + sqz));      // Roll 
                            RotateZ = RotateZ * 180 / (float)Math.PI;



                            this.Invoke(new Action(() => this.Refresh()));
                            valueLb.Invoke(new Action(() => valueLb.Text = "Value: W=" + Wvalue + "; X = " + Xvalue + "; Y = " + Yvalue + "; Z = " + Zvalue));
                            if (scanFlag == false)
                            {
                                
                                gattService.Dispose();
                                //GC.Collect();
                                Console.WriteLine($"[{device.Name}] Status: " + gattService?.Session.SessionStatus);
                                //device?.Dispose();
                                //device = null;
                                return;


                            }
                        };
                        Console.WriteLine($"[{device.Name}] Writing CCCD...");
                        GattWriteResult result =
                            await characteristic.WriteClientCharacteristicConfigurationDescriptorWithResultAsync(GattClientCharacteristicConfigurationDescriptorValue.Notify);
                        Console.WriteLine($"[{device?.Name}] Characteristics write result: status={result.Status}, protocolError={result.ProtocolError}");
                        
                    }
                    else
                    {
                        Debug.WriteLine("GattService null");

                    }
                }
                else
                {
                    Debug.WriteLine("No device found");
                    valueLb.Invoke(new Action(() => valueLb.Text = "No device found"));
                }
            }
        }
        public string RetrieveStringFromUtf8IBuffer(Windows.Storage.Streams.IBuffer theBuffer)
        {
            using (var dataReader = Windows.Storage.Streams.DataReader.FromBuffer(theBuffer))
            {
                dataReader.UnicodeEncoding = Windows.Storage.Streams.UnicodeEncoding.Utf8;
                return dataReader.ReadString(theBuffer.Length);
            }
        }

        




    }

    
}
