//
//  ViewController.swift
//  UARTDemo
//
//  Created by Tai on 2022-03-22.
//

import UIKit
import CoreBluetooth

class UARTPeripheral: NSObject {

        public static let UART_UUID_SERVICE   = CBUUID.init(string: "00000101-287c-11e4-ab74-0002a5d5c51b")
        public static let UART_UUID_NOTIFICATION_CHAR   = CBUUID.init(string: "00000102-287c-11e4-ab74-0002a5d5c51b")
            
        
    }
class ViewController: UIViewController, CBCentralManagerDelegate, CBPeripheralDelegate {

    var bleCentral : CBCentralManager!
    var mUARTDevice: CBPeripheral!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        bleCentral = CBCentralManager(delegate: self, queue: DispatchQueue.main)
    }
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
            
        case CBManagerState.poweredOff:
            print("CoreBluetooth BLE hardware is powered off")
            break
        case CBManagerState.poweredOn:
            print("CoreBluetooth BLE hardware is powered on and ready")
            
           
            bleCentral.scanForPeripherals(withServices: nil, options: nil)
            //bleCentral.scanForPeripherals(withServices: [BluePyroPeripheral.BLUEPYRO_SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey : true])
            break
        case CBManagerState.resetting:
            print("CoreBluetooth BLE hardware is resetting")
            break
        case CBManagerState.unauthorized:
            print("CoreBluetooth BLE state is unauthorized")
            
            break
        case CBManagerState.unknown:
            print("CoreBluetooth BLE state is unknown")
            break
        case CBManagerState.unsupported:
            print("CoreBluetooth BLE hardware is unsupported on this platform")
            break
            
        @unknown default:
            print("CoreBluetooth BLE state is unknown")
            break
        }
    }
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if let pname = peripheral.name {
            //print(peripheral.name)
            if pname == "UARTDemo" {
                //self.bleCentral.stopScan()
                //mDeviceNameLabel.text = peripheral.name
                self.mUARTDevice = peripheral
                self.mUARTDevice.delegate = self
                //print(peripheral.identifier)
                //self.bleCentral.connect(self.mUARTDevice, options: nil)
                
                if advertisementData[CBAdvertisementDataManufacturerDataKey] == nil {
                    return
                }
                
                var manId = UInt16(0)
                let manData = advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData
                print(manData.length)
                if manData.length < 3 {
                    return
                }
              
                manData.getBytes(&manId, range: NSMakeRange(0, 2))
                if manId != 0x177 {
                    return
                }
            }
        }
    }
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
      
        peripheral.discoverServices([UARTPeripheral.UART_UUID_SERVICE])
        print("Connected to UART peripheral")
        
        
    }
    func centralManager(_ central: CBCentralManager,
                        didDisconnectPeripheral peripheral: CBPeripheral,
                        error: Error?) {
        print("Disconnected from UART peripheral")
        
        
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        print("Device connect fail")
    }
    
    func scanPeripheral(_ sender: CBCentralManager)
    {
        print("Scan for peripherals")
        bleCentral.scanForPeripherals(withServices: nil, options: nil)
        //bleCentral.scanForPeripherals(withServices: [BlueIOPeripheral.BLUEPYRO_SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey : true])
    }
    // Handles discovery event
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let services = peripheral.services {
            for service in services {
                if service.uuid == UARTPeripheral.UART_UUID_SERVICE {
                    print("UART service found")
                    //Now kick off discovery of characteristics
                    peripheral.discoverCharacteristics([UARTPeripheral.UART_UUID_NOTIFICATION_CHAR], for: service)
                    return
                }
            }
        }
    }
    // Handling discovery of characteristics
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        var mRawChar: CBCharacteristic
       
        if let characteristics = service.characteristics {
            for characteristic in characteristics {
                if characteristic.uuid == UARTPeripheral.UART_UUID_NOTIFICATION_CHAR{
                    print("UART service found")
                    mRawChar = characteristic
                    
                    peripheral.setNotifyValue(true, for: mRawChar)
                    
                }
              
                              
            }
                       
        }
        
    }
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let data = characteristic.value else { return }
        print("\nValue: \(data) \nwas written to Characteristic:\n\(characteristic)")
        if(error != nil){
            print("\nError while writing on Characteristic:\n\(characteristic). Error Message:")
            print(error as Any)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?){
        print("didUpdateNotificationStateFor")

        print("characteristic description:", characteristic.description)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?){
        //print("didUpdateValueFor")
        if let error = error {
            print("error:", error)
        }

        guard characteristic.value != nil else {
            return
        }
        
        
        //print("characteristic description:", characteristic.description)
        let data = characteristic.value
        print(data)
        
        
        
        
        
             
    
    }
   

}

