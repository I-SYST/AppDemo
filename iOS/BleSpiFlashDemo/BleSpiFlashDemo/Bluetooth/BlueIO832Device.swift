//
//  BlueIO832Device.swift
//  BlueIOSigCap
//
//  Created by Ho Manh Tai on 2022-09-06.
//

import Foundation

import UIKit
import CoreBluetooth

class BlueIOPeripheral: NSObject {

        public static let BLUEIO_UUID_UART_SERVICE   = CBUUID.init(string: "00000101-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_UART_RX_CHAR   = CBUUID.init(string: "00000102-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_UART_TX_CHAR   = CBUUID.init(string: "00000103-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_UART_CONFIG_CHAR   = CBUUID.init(string: "00000104-287c-11e4-ab74-0002a5d5c51b")
    
        public static let BLUEIO_UUID_I2C_SERVICE   = CBUUID.init(string: "00000201-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_I2C_RX_CHAR   = CBUUID.init(string: "00000202-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_I2C_TX_CHAR   = CBUUID.init(string: "00000203-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_I2C_CONFIG_CHAR   = CBUUID.init(string: "00000204-287c-11e4-ab74-0002a5d5c51b")
    
        public static let BLUEIO_UUID_SPI_SERVICE   = CBUUID.init(string: "00000301-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_SPI_RX_CHAR   = CBUUID.init(string: "00000302-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_SPI_TX_CHAR   = CBUUID.init(string: "00000303-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_SPI_CONFIG_CHAR   = CBUUID.init(string: "00000304-287c-11e4-ab74-0002a5d5c51b")
    
        public static let BLUEIO_UUID_OPMODE_CTRL_SERVICE   = CBUUID.init(string: "00000A01-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_OPMODE_CTRL_READ_CHAR   = CBUUID.init(string: "00000A02-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_OPMODE_CTRL_WRITE_CHAR   = CBUUID.init(string: "00000A03-287c-11e4-ab74-0002a5d5c51b")
        
        public static let BLUEIO_UUID_SIGCAP_SERVICE   = CBUUID.init(string: "00000501-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_SIGCAP_DATA_CHAR   = CBUUID.init(string: "00000502-287c-11e4-ab74-0002a5d5c51b")
        public static let BLUEIO_UUID_SIGCAP_CONFIG_CHAR   = CBUUID.init(string: "00000503-287c-11e4-ab74-0002a5d5c51b")
        
    
        public static let BLUEIO_UUID_DEVICE_INFORMATION_SERVICE   = CBUUID.init(string: "180A")
        public static let BLUEIO_UUID_DEVICE_INFORMATION_FIRMWARE_CHAR   = CBUUID.init(string: "2A26")
    
    }
