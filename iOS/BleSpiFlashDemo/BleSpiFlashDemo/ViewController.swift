//
//  ViewController.swift
//  BleSpiFlashDemo
//
//  Created by Ho Manh Tai on 2023-02-11.
//

import UIKit

class ViewController: UIViewController {

    @Published
    var device: BluetoothDevice? = nil
    @Published
    var bluetoothManager: BluetoothManager = BluetoothManager()
    @Published var devices: [BluetoothDevice] = []
    
    var pickerData: [String] = [String]()
    var device_name:String = ""
    var isConnect = false
    let semaphore = DispatchSemaphore(value: 1)
    
    @IBOutlet weak var DevicePicker: UIPickerView!
    @IBOutlet weak var ScanButton: UIButton!
    @IBOutlet weak var TestButton: UIButton!
    @IBOutlet weak var ConnectButton: UIButton!
    
    @IBOutlet weak var Erase: UIButton!
    @IBOutlet weak var ReadButton: UIButton!
    @IBOutlet weak var SPIDataTextView: UITextView!
    
    public static var isZoomed: Bool {
    return UIScreen.main.scale < UIScreen.main.nativeScale
    }
    var ratio = UIScreen.main.scale/UIScreen.main.nativeScale
    var screenSize: CGRect = UIScreen.main.bounds
    let modelName = UIDevice.modelName
    
    func pickerView(_ pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusing view: UIView?) -> UIView {
        var label = UILabel()
        if let v = view as? UILabel { label = v }
        label.font = UIFont (name: "Helvetica Neue", size: 17)
        label.textColor = UIColor.systemTeal//.tintColor
        label.text =  pickerData[row]
        label.textAlignment = .center
        return label
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        DevicePicker.dataSource = self
        DevicePicker.delegate = self
        self.SPIDataTextView.frame = CGRect(x: 5, y: 250, width: screenSize.width-10, height:  300)
        self.SPIDataTextView.layer.borderWidth = 1;
        self.SPIDataTextView.font = UIFont(name: "Courier", size: 20)
        self.SPIDataTextView.isEditable = false
        self.SPIDataTextView.backgroundColor = UIColor.black
        TestButton.isEnabled = false
        ReadButton.isEnabled = false
        Erase.isEnabled = false
        scanBlueIODevice()
    }


    @IBAction func ScanButtonClick(_ sender: Any) {
        scanBlueIODevice()
    }
    func scanBlueIODevice()
    {
        self.bluetoothManager.devices.removeAll()
        self.pickerData.removeAll()
        self.bluetoothManager.startScan()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5, execute: {
            self.bluetoothManager.stopScan()
            if self.bluetoothManager.devices.count > 0{
                for device in self.bluetoothManager.devices {
                    self.pickerData.append(device.name!)
                }
                self.DevicePicker.reloadAllComponents()
                self.bluetoothManager.device = self.bluetoothManager.devices[self.DevicePicker.selectedRow(inComponent: 0)]
                //self.bluetoothManager.startConnect()
            }
        })
    }
    
    @IBAction func ConnectButtonClick(_ sender: Any) {
        if !isConnect{
            if self.bluetoothManager.devices.count > 0{
                isConnect = true
                //SPIDataTextView.insertText("Connecting...\n")
                ConnectButton.setTitle("Disonnect", for: .normal)
                self.bluetoothManager.device = self.bluetoothManager.devices[self.DevicePicker.selectedRow(inComponent: 0)]
                self.bluetoothManager.startConnect()
                TestButton.isEnabled = true
                ReadButton.isEnabled = true
                Erase.isEnabled = true
                SPIDataTextView.insertText("Connected\n")
            }
        }else{
            isConnect = false
            //SPIDataTextView.insertText("Disconnecting...\n")
            ConnectButton.setTitle("Connect", for: .normal)
            self.bluetoothManager.disconnect()
            TestButton.isEnabled = false
            ReadButton.isEnabled = false
            Erase.isEnabled = false
            SPIDataTextView.insertText("Disconnected\n")
        }
        
    }
    
    @IBAction func EraseClick(_ sender: Any) {
        SPIDataTextView.insertText("Erase whole flash memory...\n")
        self.bluetoothManager.device = self.bluetoothManager.devices[DevicePicker.selectedRow(inComponent: 0)]
              
        self.bluetoothManager.PckCounter = 0

        let command: [UInt8] = [0x35]
        //let memAddress: [UInt8] = [0xFF,0xFF,0xFF,0xFF]
        let memAddress: UInt32 = 0xFFFFFFFF
        let memAddress_bytes = withUnsafeBytes(of: memAddress.littleEndian, Array.init)
        let dataLen: [UInt8] = [0x00,0x00]
        let data: [UInt8] = [UInt8](repeating: UInt8(0xFF), count: 233)
        
        let messageData = NSMutableData()
        messageData.append(Data(command))
        messageData.append(Data(memAddress_bytes))
        messageData.append(Data(dataLen))
        messageData.append(Data(data))
        self.bluetoothManager.SpiWriteData = messageData as Data
        
        self.bluetoothManager.device.peripheral.writeValue(self.bluetoothManager.SpiWriteData, for: self.bluetoothManager.writeChar, type: .withoutResponse)
        
    }
    
    
    @IBAction func TestButtonClick(_ sender: Any) {
        
        
        //let originalValues = Array(messageData)
        //print(originalValues.count)
        
        if pickerData.count > 0 {
            //let pickerChoice = pickerData[DevicePicker.selectedRow(inComponent: 0)]
            //print(self.bluetoothManager.devices.count)
            //print(DevicePicker.selectedRow(inComponent: 0))
            if self.bluetoothManager.devices.count < DevicePicker.selectedRow(inComponent: 0){
                scanBlueIODevice()
            } else{
                SPIDataTextView.insertText("Start writing flash memory...\n")
                self.bluetoothManager.device = self.bluetoothManager.devices[DevicePicker.selectedRow(inComponent: 0)]
                
                var memAddress: UInt32 = 0x00000000
                
                self.bluetoothManager.PckCounter = 0
                var len = Int(50)
                var timeStamp = Int(1000 * Date().timeIntervalSince1970)
                print(timeStamp)
                DispatchQueue.global().async {
                    while len > 0{
                        //DispatchQueue.main.asyncAfter(deadline: .now() + 0.1, execute: {
                            //print(len)
                            let command: [UInt8] = [0x10]
                            //let memAddress: [UInt8] = [0xFF,0xFF,0xFF,0xFF]
                            
                            let memAddress_bytes = withUnsafeBytes(of: memAddress.littleEndian, Array.init)
                            let dataLen: [UInt8] = [0xE9,0x00]
                            //let data: [UInt8] = [UInt8](repeating: UInt8(Int.random(in: 0..<255)), count: 233)
                            let data: [UInt8] = memAddress_bytes + Array(0...228)
                            
                            let messageData = NSMutableData()
                            messageData.append(Data(command))
                            messageData.append(Data(memAddress_bytes))
                            messageData.append(Data(dataLen))
                            messageData.append(Data(data))
                            
                            self.bluetoothManager.SpiWriteData = messageData as Data
                            var arr2 = Array<UInt8>(repeating: 0, count: (self.bluetoothManager.SpiWriteData?.count ?? 0)/MemoryLayout<UInt8>.stride)
                            _ = arr2.withUnsafeMutableBytes { self.bluetoothManager.SpiWriteData?.copyBytes(to: $0) }
                            print(arr2)
                            self.bluetoothManager.device.peripheral.writeValue(self.bluetoothManager.SpiWriteData, for: self.bluetoothManager.writeChar, type: .withoutResponse)
                            memAddress += 0xE9
                            //print(memAddress)
                            len -= 1
                        //})
                    }
                    var timeStamp2 = Int(1000 * Date().timeIntervalSince1970)
                    print(timeStamp2 - timeStamp)
                    
                }
            }
            self.SPIDataTextView.insertText("Finish writing flash memory...\n")
        }
        
    }
    
    @IBAction func ReadButtonClick(_ sender: Any) {
        if pickerData.count > 0 {
            if self.bluetoothManager.devices.count < DevicePicker.selectedRow(inComponent: 0){
                scanBlueIODevice()
            } else{
                SPIDataTextView.insertText("Start reading flash memory...\n")
                self.bluetoothManager.device = self.bluetoothManager.devices[DevicePicker.selectedRow(inComponent: 0)]
                
                var memAddress: UInt32 = 0x00000000
                
                self.bluetoothManager.PckCounter = 0
                var len = Int(60)
                DispatchQueue.global().async {
                    print("Read Flash data")
                    while len > 0{
                        //DispatchQueue.main.asyncAfter(deadline: .now() + 0.1, execute: {
                            
                            let command: [UInt8] = [0x1F]
                            
                            
                            let memAddress_bytes = withUnsafeBytes(of: memAddress.littleEndian, Array.init)
                            let dataLen: [UInt8] = [0xE9,0x00]
                            let data: [UInt8] = [UInt8](repeating: 0xFF, count: 233)
                            
                            
                            let messageData = NSMutableData()
                            messageData.append(Data(command))
                            messageData.append(Data(memAddress_bytes))
                            messageData.append(Data(dataLen))
                            messageData.append(Data(data))
                            self.bluetoothManager.SpiWriteData = messageData as Data
                            
                            self.bluetoothManager.device.peripheral.writeValue(self.bluetoothManager.SpiWriteData, for: self.bluetoothManager.writeChar, type: .withoutResponse)
                            memAddress += 0xE9
                            //print(memAddress)
                            len -= 1
                        //})
                    }
                    
                }
                self.SPIDataTextView.insertText("Finish reading flash memory...\n")
            }
        }
    }
    
    
}

extension ViewController: UIPickerViewDataSource{
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return pickerData.count
    }
}

extension ViewController: UIPickerViewDelegate{
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return pickerData[row]
    }
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        self.device_name = pickerData[row]
        
    }
}
