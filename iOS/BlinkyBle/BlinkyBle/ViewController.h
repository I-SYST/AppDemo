//
//  ViewController.h
//  BlinkyBle
//
//  Created by Hoan on 2014-08-18.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface ViewController : UIViewController <CBCentralManagerDelegate, CBPeripheralDelegate, UITableViewDelegate>
@property (nonatomic, retain) IBOutlet UITableView* TableView;
@property (strong, nonatomic) CBCentralManager *BleCentralManager;
@property (strong, nonatomic) CBPeripheral *ConnPeri;
@property (strong, nonatomic) CBCharacteristic *ConnChar;

@end
