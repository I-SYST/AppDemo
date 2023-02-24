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
    
    @IBOutlet weak var DevicePicker: UIPickerView!
    @IBOutlet weak var ScanButton: UIButton!
    @IBOutlet weak var TestButton: UIButton!
    
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
            for device in self.bluetoothManager.devices {
                self.pickerData.append(device.name!)
            }
            self.DevicePicker.reloadAllComponents()
        })
    }
    
    @IBAction func TestButtonClick(_ sender: Any) {
        let command: [UInt8] = [0x35]
        //let memAddress: [UInt8] = [0xFF,0xFF,0xFF,0xFF]
        let memAddress: UInt32 = 0xFFFFFFFF
        let memAddress_bytes = withUnsafeBytes(of: memAddress.littleEndian, Array.init)
        let dataLen: [UInt8] = [0x00,0x00]
        let data: [UInt8] = [UInt8](repeating: 0xFF, count: 233)
    
        
        let messageData = NSMutableData()
        messageData.append(Data(command))
        messageData.append(Data(memAddress_bytes))
        messageData.append(Data(dataLen))
        messageData.append(Data(data))
        
        let originalValues = Array(messageData)
        print(originalValues.count)
        
        if pickerData.count > 0 {
            //let pickerChoice = pickerData[DevicePicker.selectedRow(inComponent: 0)]
            print(self.bluetoothManager.devices.count)
            print(DevicePicker.selectedRow(inComponent: 0))
            if self.bluetoothManager.devices.count < DevicePicker.selectedRow(inComponent: 0){
                scanBlueIODevice()
            } else{
                self.bluetoothManager.device = self.bluetoothManager.devices[DevicePicker.selectedRow(inComponent: 0)]
                self.bluetoothManager.SpiWriteData = messageData as Data
                print(self.bluetoothManager.SpiWriteData!.hexEncodedString(options: .upperCase))
                self.bluetoothManager.isSpiWrite = true
                self.bluetoothManager.startConnect()
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
