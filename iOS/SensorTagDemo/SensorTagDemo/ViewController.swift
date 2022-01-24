//
//  ViewController.swift
//  SensorTagDemo
//
//  Created by Nguyen Hoan Hoang on 2017-10-08.
//  Copyright © 2017 I-SYST inc. All rights reserved.
//

import UIKit
import CoreBluetooth
import simd

class ViewController: UIViewController, CBCentralManagerDelegate {

    var bleCentral : CBCentralManager!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        bleCentral = CBCentralManager(delegate: self, queue: DispatchQueue.main)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
        
    }

    @IBOutlet weak var graphView : GraphView!
    @IBOutlet weak var tempLabel: UILabel!
    @IBOutlet weak var humiLabel : UILabel!
    @IBOutlet weak var pressLabel : UILabel!
    @IBOutlet weak var rssiLabel : UILabel!
    @IBOutlet weak var aqiLabel : UILabel!
    @IBOutlet weak var batLevelLabel : UILabel!
    @IBOutlet weak var batVoltLabel : UILabel!
    
    // MARK: BLE Central
    
    func centralManager(_ central: CBCentralManager,
                        didDiscover peripheral: CBPeripheral,
                        advertisementData : [String : Any],
                        rssi RSSI: NSNumber) {
        print("PERIPHERAL NAME: \(String(describing: peripheral.name))\n AdvertisementData: \(advertisementData)\n RSSI: \(RSSI)\n")
        
        print("UUID DESCRIPTION: \(peripheral.identifier.uuidString)\n")
        
        print("IDENTIFIER: \(peripheral.identifier)\n")
        
        if advertisementData[CBAdvertisementDataManufacturerDataKey] == nil {
            return
        }
        
        //sensorData.text = sensorData.text + "FOUND PERIPHERALS: \(peripheral) AdvertisementData: \(advertisementData) RSSI: \(RSSI)\n"
        var manId = UInt16(0)
        let manData = advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData
        
        if manData.length < 3 {
            return
        }
        //(advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&manId, range: NSMakeRange(0, 2))
        manData.getBytes(&manId, range: NSMakeRange(0, 2))
        if manId != 0x177 {
            return
        }
        
        var type = UInt8(0)
        //(advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&type, range: NSMakeRange(2, 1))
        manData.getBytes(&type, range: NSMakeRange(2, 1))
        
        switch (type) {
        	case 1:
                var press = Int32(0)
                (advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&press, range: NSMakeRange(3, 4))
                pressLabel.text = String(format:"%.2f KPa", Float(press) / 1000.0)
                
                var temp = Int16(0)
                (advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&temp, range: NSMakeRange(7, 2))
                tempLabel.text = String(format:"%.2f C", Float(temp) / 100.0)
                
                var humi = UInt16(0)
                (advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&humi, range: NSMakeRange(9, 2))
                humiLabel.text = String(format:"%d%%", humi / 100)
                graphView.add(double3(Double(temp) / 100.0, Double(press) / 1000.0, Double(humi) / 100.0))
            	break
        	case 2:
                var aqi = UInt16(0)
                (advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&aqi, range: NSMakeRange(3, 2))
            	break
        	case 16:
                var batLevel = UInt8(0)
                (advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&batLevel, range: NSMakeRange(3, 1))
                batLevelLabel.text = String(format:"%d%%", batLevel)

                var batVolt = Int32(0)
                (advertisementData[CBAdvertisementDataManufacturerDataKey] as! NSData).getBytes(&batVolt, range: NSMakeRange(4, 4))
                batVoltLabel.text = String(format:"%.2fV", Float(batVolt) / 1000.0)
                
            	break
        	default:
            	break
        }

        rssiLabel.text = String( describing: RSSI)
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        
        peripheral.discoverServices(nil)
        print("Connected to peripheral")
        
        
    }
    
    func centralManager(_ central: CBCentralManager,
                        didDisconnectPeripheral peripheral: CBPeripheral,
                        error: Error?) {
        print("disconnected from peripheral")
        
        
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
    }
    
    func scanPeripheral(_ sender: CBCentralManager)
    {
        print("Scan for peripherals")
        bleCentral.scanForPeripherals(withServices: nil, options: nil)
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
            
        case .poweredOff:
            print("CoreBluetooth BLE hardware is powered off")
            break
        case .poweredOn:
            print("CoreBluetooth BLE hardware is powered on and ready")
            
            bleCentral.scanForPeripherals(withServices: nil, options: nil)
            break
        case .resetting:
            print("CoreBluetooth BLE hardware is resetting")
            break
        case .unauthorized:
            print("CoreBluetooth BLE state is unauthorized")
            
            break
        case .unknown:
            print("CoreBluetooth BLE state is unknown")
            break
        case .unsupported:
            print("CoreBluetooth BLE hardware is unsupported on this platform")
            break
            
        }
    }
    
}

