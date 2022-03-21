//
//  ViewController.swift
//  IOThingy
//
//  Created by Tai on 2022-03-21.
//

import UIKit
import CoreBluetooth

class BlueIOPeripheral: NSObject {

        public static let BLUEIO_UUID_SERVICE   = CBUUID.init(string: "ef680400-9b35-4933-9b10-52ffa9740042")
        public static let BLE_UUID_QUATERNION_CHAR   = CBUUID.init(string: "ef680404-9b35-4933-9b10-52ffa9740042")
            
        
    }

class ViewController: UIViewController, CBCentralManagerDelegate, CBPeripheralDelegate  {
    
    var bleCentral : CBCentralManager!
    var mBlueIODevice: CBPeripheral!
    var cube:CATransformLayer!
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        bleCentral = CBCentralManager(delegate: self, queue: DispatchQueue.main)
    }

    func face(with transform: CATransform3D, color: UIColor) -> CALayer {
        let face = CALayer()
        face.frame = CGRect(x: -50, y: -50, width: 100, height: 100)
        face.backgroundColor = color.cgColor
        face.transform = transform
        return face
    }
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(true)

        view.backgroundColor = .black

        cube = CATransformLayer()

        // create the front face
        let transform1 = CATransform3DMakeTranslation(0, 0, 50)
        cube.addSublayer(face(with: transform1, color: .red))

        // create the right-hand face
        var transform2 = CATransform3DMakeTranslation(50, 0, 0)
        transform2 = CATransform3DRotate(transform2, CGFloat.pi / 2, 0, 1, 0)
        cube.addSublayer(face(with: transform2, color: .yellow))

        // create the top face
        var transform3 = CATransform3DMakeTranslation(0, -50, 0)
        transform3 = CATransform3DRotate(transform3, CGFloat.pi / 2, 1, 0, 0)
        cube.addSublayer(face(with: transform3, color: .green))

        // create the bottom face
        var transform4 = CATransform3DMakeTranslation(0, 50, 0)
        transform4 = CATransform3DRotate(transform4, -(CGFloat.pi / 2), 1, 0, 0)
        cube.addSublayer(face(with: transform4, color: .white))

        // create the left-hand face
        var transform5 = CATransform3DMakeTranslation(-50, 0, 0)
        transform5 = CATransform3DRotate(transform5, -(CGFloat.pi / 2), 0, 1, 0)
        cube.addSublayer(face(with: transform5, color: .cyan))

        // create the back face
        var transform6 = CATransform3DMakeTranslation(0, 0, -50)
        transform6 = CATransform3DRotate(transform6, CGFloat.pi, 0, 1, 0)
        cube.addSublayer(face(with: transform6, color: .magenta))

        // now position the transform layer in the center
        cube.position = CGPoint(x: view.bounds.midX, y: view.bounds.midY)

        // and add the cube to our main view's layer
        view.layer.addSublayer(cube)
        
        /*let anim = CABasicAnimation(keyPath: "transform")
        anim.fromValue = cube.transform
        anim.toValue = CATransform3DMakeRotation(3*CGFloat.pi, 1, 1, 1)
        anim.duration = 2
        anim.isCumulative = true
        anim.repeatCount = .greatestFiniteMagnitude
        cube.add(anim, forKey: "transform")*/
        
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
            if pname == "BlueIOThingy" {
                //self.bleCentral.stopScan()
                //mDeviceNameLabel.text = peripheral.name
                self.mBlueIODevice = peripheral
                self.mBlueIODevice.delegate = self
                //print(peripheral.identifier)
                self.bleCentral.connect(self.mBlueIODevice, options: nil)
                    
            }
        }
    }
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
      
        peripheral.discoverServices([BlueIOPeripheral.BLUEIO_UUID_SERVICE])
        print("Connected to BlueIO peripheral")
        
        
    }
    func centralManager(_ central: CBCentralManager,
                        didDisconnectPeripheral peripheral: CBPeripheral,
                        error: Error?) {
        print("Disconnected from BlueIO peripheral")
        
        
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
                if service.uuid == BlueIOPeripheral.BLUEIO_UUID_SERVICE {
                    print("BlueIO service found")
                    //Now kick off discovery of characteristics
                    peripheral.discoverCharacteristics([BlueIOPeripheral.BLE_UUID_QUATERNION_CHAR], for: service)
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
                if characteristic.uuid == BlueIOPeripheral.BLE_UUID_QUATERNION_CHAR{
                    print("Quaternion service found")
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
        
        let Wdata = data![0...3]
        let Xdata = data![4...7]
        let Ydata = data![8...11]
        let Zdata = data![12...15]
        
        
        let X_array = Int32(bigEndian: Xdata.withUnsafeBytes { $0.pointee })
        let X = Float(Int32(bigEndian: X_array))/Float(1<<30)
        print(X)
        let Y_array = Int32(bigEndian: Ydata.withUnsafeBytes { $0.pointee })
        let Y = Float(Int32(bigEndian: Y_array))/Float(1<<30)
        print(Y)
        let Z_array = Int32(bigEndian: Zdata.withUnsafeBytes { $0.pointee })
        let Z = Float(Int32(bigEndian: Z_array))/Float(1<<30)
        print(Z)
        let W_array = Int32(bigEndian: Wdata.withUnsafeBytes { $0.pointee })
        let W = Float(Int32(bigEndian: W_array))/Float(1<<30)
        print(W)
        
        let angle = 2*acos(W)
        let rotateX = X/sin(angle/2)
        let rotateY = Y/sin(angle/2)
        let rotateZ = Z/sin(angle/2)
        
        let anim = CABasicAnimation(keyPath: "transform")
        anim.fromValue = cube.transform
        anim.toValue = CATransform3DMakeRotation(CGFloat(angle), CGFloat(rotateX), CGFloat(rotateY), CGFloat(rotateZ))
        
        //anim.duration = 2
        //anim.isCumulative = true
        //anim.repeatCount = .greatestFiniteMagnitude
        cube.add(anim, forKey: "transform")
    
    }
    func floatValue(data: Data) -> Float {
        return Float(bitPattern: UInt32(bigEndian: data.withUnsafeBytes { $0.load(as: UInt32.self) }))
        
    }
}

