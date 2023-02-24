/*
* Copyright (c) 2022, Nordic Semiconductor
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice, this
*    list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright notice, this
*    list of conditions and the following disclaimer in the documentation and/or
*    other materials provided with the distribution.
*
* 3. Neither the name of the copyright holder nor the names of its contributors may
*    be used to endorse or promote products derived from this software without
*    specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
* IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
* INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
* NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/

import SwiftUI
import Combine
import os
import os.log
import CoreBluetooth



class BluetoothManager : NSObject, ObservableObject {
    
    private let MIN_RSSI = NSNumber(-65)
    
    @Published var devices: [BluetoothDevice] = []
    
    @Published var nearbyOnlyFilter = false
    
    @Published var withNameOnlyFilter = false
    
    @Published var device: BluetoothDevice!
    
    @Published var centralManager: CBCentralManager!
    
    @Published var SpiData: Data!
    @Published var SpiWriteData: Data!
    @Published var isSpiWrite = false
    @Published var isSpiRead = false
    
    private var isOnScreen = false
    private var isBluetoothReady = false
    private var isModeRead = false
    private var isModeUpdate = false
   
    @Published var PckCounter: Int32 = 0
    @Published var LogString:String = ""
    private var formatter1 = DateFormatter()
    
    override init() {
        super.init()
        os_log("init CBCentralManager")
        LogString = LogString + "\n[" + formatter1.string(from: Date.now) + "]: " + "init CBCentralManager"
        centralManager = CBCentralManager(delegate: self, queue: nil)
        formatter1.dateFormat = "y-MM-dd H:mm:ss.SSSS"
    }
    
    func filteredDevices() -> [BluetoothDevice] {
        return devices.filter { device in
            if !nearbyOnlyFilter {
                return true
            }
            let result: ComparisonResult = device.rssi.compare(MIN_RSSI)
            return result == ComparisonResult.orderedDescending
        }.filter { device in
            if !withNameOnlyFilter {
                return true
            }
            return device.name != nil
        }
    }
    
    func startConnect(){
        if self.device != nil{
            self.device.peripheral.delegate = self
            print(self.device.peripheral.name as Any)
            print(self.device.peripheral.identifier.uuidString as Any)
            centralManager.connect(self.device.peripheral, options: nil)
        }
    }
    func disconnect(){
        if self.device != nil{
            self.device.peripheral.delegate = self
            print(self.device.peripheral.name as Any)
            centralManager.cancelPeripheralConnection(self.device.peripheral)
        }
    }
   
    
    func startScan() {
        isOnScreen = true
        runScanningWhenNeeded()
    }
    
    func stopScan() {
        isOnScreen = false
        centralManager.stopScan()
    }
    
    private func runScanningWhenNeeded() {
        if (isOnScreen && isBluetoothReady) {
            centralManager.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey: true])
        }
    }
}

// MARK: - CB Central Manager impl

extension BluetoothManager: CBCentralManagerDelegate, CBPeripheralDelegate {
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == CBManagerState.poweredOn {
            os_log("BLE powered on")
            LogString = LogString + "\n" + "BLE powered on"
            // Turned on
            isBluetoothReady = true
            runScanningWhenNeeded()
        }
        else {
            isBluetoothReady = false
            os_log("Something wrong with BLE")
            LogString = LogString + "\n" + "Something wrong with BLE"
            // Not on, but can have different issues
        }
        
        var consoleLog = ""

        switch central.state {
            case .poweredOff:
                consoleLog = "BLE is powered off"
                
            case .poweredOn:
                consoleLog = "BLE is poweredOn"
                
            case .resetting:
                consoleLog = "BLE is resetting"
                
            case .unauthorized:
                consoleLog = "BLE is unauthorized"
                
            case .unknown:
                consoleLog = "BLE is unknown"
                
            case .unsupported:
                consoleLog = "BLE is unsupported"
                
            default:
                consoleLog = "default"
        }
        
        os_log("BluetoothManager status: %@", consoleLog)
        LogString = LogString + "\n" + "BluetoothManager status: " + consoleLog
    }
    
    func centralManager(
        _ central: CBCentralManager,
        didDiscover peripheral: CBPeripheral,
        advertisementData: [String : Any],
        rssi RSSI: NSNumber){
            
        //os_log("Device: \(peripheral.name ?? "NO_NAME"), Rssi: \(RSSI)")
        //let pname = advertisementData[CBAdvertisementDataLocalNameKey] as? String
            if advertisementData[CBAdvertisementDataLocalNameKey] == nil {
                //print("No Local name advertised")
                return
            }
            else{
                let localName = advertisementData[CBAdvertisementDataLocalNameKey] as! NSString
                //print("Local name is advertised:" + (localName as String))
                let pname = String(localName)
                if pname.contains("BleSpiBridge"){
                    let device = BluetoothDevice(peripheral: peripheral, rssi: RSSI, name: pname )
                    let index = devices.map { $0.peripheral }.firstIndex(of: peripheral)
                    if let index = index {
                        devices[index] = device
                    } else {
                        devices.append(device)
                    }
                }
            }
            
    }
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
      
        if self.isModeRead||self.isModeUpdate{
            peripheral.discoverServices([BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_SERVICE])
            print("Connected to OPMODE_CTRL mode")
        }else{
            peripheral.discoverServices([BlueIOPeripheral.BLUEIO_UUID_SPI_SERVICE])
            print("Connected to SPI mode")
        }
        
    }
    
    func centralManager(_ central: CBCentralManager,
                        didDisconnectPeripheral peripheral: CBPeripheral,
                        error: Error?) {
        os_log("Disconnected from  peripheral")
        LogString = LogString + "\n" + "Disconnected from  peripheral"
        
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        os_log("Device connect fail")
        LogString = LogString + "\n" + "Device connect fail"
    }
    
    func scanPeripheral(_ sender: CBCentralManager)
    {
        os_log("Scan for peripherals")
        LogString = LogString + "\n" + "Scan for peripherals"
       
    }
    
    // Handles discovery event
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let services = peripheral.services {
            for service in services {
                
                if service.uuid == BlueIOPeripheral.BLUEIO_UUID_SPI_SERVICE {
                    os_log("SPI service found")
                    //Now kick off discovery of characteristics
                    peripheral.discoverCharacteristics([BlueIOPeripheral.BLUEIO_UUID_SPI_RX_CHAR, BlueIOPeripheral.BLUEIO_UUID_SPI_TX_CHAR], for: service)
                    return
                }
                if service.uuid == BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_SERVICE {
                    print("OPMODE_CTRL service found")
                    //Now kick off discovery of characteristics
                    peripheral.discoverCharacteristics([BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_WRITE_CHAR, BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_READ_CHAR], for: service)
                    return
                }
            }
        }
    }
    // Handling discovery of characteristics
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let characteristics = service.characteristics {
            for characteristic in characteristics {
                // OPMODE_CTRL Characteristics --------------------------------------------
                if characteristic.uuid == BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_WRITE_CHAR{
                    if self.isModeUpdate{
                        print("OPMODE_CTRL_WRITE characteristic found")
                        let bytes: [UInt8] = [05]
                        let appData = Data(bytes)
                        peripheral.writeValue(appData, for: characteristic, type: .withoutResponse)
                    }
                }
                if characteristic.uuid == BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_READ_CHAR{
                    if self.isModeRead{
                        print("OPMODE_CTRL_READ characteristic found")
                        peripheral.readValue(for: characteristic)
                    }
                }
                //
                if characteristic.uuid == BlueIOPeripheral.BLUEIO_UUID_SPI_TX_CHAR{
                    if self.isSpiWrite{
                        print("SPI_WRITE characteristic found")
                        
                        let appData = self.SpiWriteData
                        peripheral.writeValue(appData!, for: characteristic, type: .withoutResponse)
                    }
                }
                if characteristic.uuid == BlueIOPeripheral.BLUEIO_UUID_SPI_RX_CHAR{
                    //if self.isSpiRead{
                        print("SPI_READ characteristic found")
                        //peripheral.readValue(for: characteristic)
                        peripheral.setNotifyValue(true, for: characteristic)
                    //}
                }
                
            }
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 1, execute: {
                
                /*if self.isModeUpdate {
                    self.isModeUpdate = false
                    self.disconnect()                    
                    self.startConnect()
                    //NotificationCenter.default.post(name: Notification.Name("SigCapConnectNotification"), object: nil)
                }*/
                if self.isSpiWrite {
                    self.isSpiWrite = false
                    //self.isSpiRead = true
                    //self.disconnect()
                    //self.startConnect()
                }
                
            })
            
                       
        }
        
    }
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let data = characteristic.value else { return }
        os_log("\nValue: \(data) \nwas written to Characteristic:\n\(characteristic)")
        if(error != nil){
            os_log("\nError while writing on Characteristic:\n\(characteristic). Error Message:")
            print(error as Any)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?){
        os_log("didUpdateNotificationStateFor")

        os_log("characteristic description:", characteristic.description)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?){
        //print("didUpdateValueFor")
        if let error = error {
            print("error:", error)
        }

        guard characteristic.value != nil else {
            print("Value null!")
            return
        }
        let data = characteristic.value
        print("Data length: ")
        print(data as Any)
        var arr2 = Array<UInt8>(repeating: 0, count: (data?.count ?? 0)/MemoryLayout<UInt8>.stride)
        _ = arr2.withUnsafeMutableBytes { data?.copyBytes(to: $0) }
        print(arr2)
        print(data!.hexEncodedString(options: .upperCase))
        if characteristic.uuid == BlueIOPeripheral.BLUEIO_UUID_OPMODE_CTRL_READ_CHAR{
            //print(data)
            /*
            self.isModeRead = false
            if data?.count == 0 {
                
                print("Cannot read operation Mode")
                //NotificationCenter.default.post(name: Notification.Name("SigCapOpModeFailNotification"), object: nil)
            }else{
                if data![0] != 5 {
                    print("Not In SIGCAP Mode")
                    //NotificationCenter.default.post(name: Notification.Name("SigCapOpModeNotification"), object: nil)
                    
                }else{
                    print("In SIGCAP Mode")
                    self.disconnect()
                    
                    self.startConnect()
                    //NotificationCenter.default.post(name: Notification.Name("SigCapConnectNotification"), object: nil)
                }
            }*/
        }
        
        if characteristic.uuid == BlueIOPeripheral.BLUEIO_UUID_SPI_RX_CHAR{
        
            print("SPI Read data")
            print(arr2)
            print(data!.hexEncodedString(options: .upperCase))
                
                  
        }
    }
    
}
extension Data {
    struct HexEncodingOptions: OptionSet {
        let rawValue: Int
        static let upperCase = HexEncodingOptions(rawValue: 1 << 0)
    }

    func hexEncodedString(options: HexEncodingOptions = []) -> String {
        let format = options.contains(.upperCase) ? "%02hhX" : "%02hhx"
        return self.map { String(format: format, $0) + " " }.joined()
    }
}
extension Date {
    var millisecondsSince1970: Int64 {
        Int64((self.timeIntervalSince1970 * 1000.0).rounded())
    }
    
    init(milliseconds: Int64) {
        self = Date(timeIntervalSince1970: TimeInterval(milliseconds) / 1000)
    }
}
