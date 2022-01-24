//
//  ViewController.m
//  BlinkyBle
//
//  Created by Hoan on 2014-08-18.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import "ViewController.h"

#define BLINKY_SERVICE_UUID             @"00000001-59a4-4d2b-8479-8bbdbcf77fcd"
#define BLINKY_CHAR_UUID                @"00000002-59a4-4d2b-8479-8bbdbcf77fcd"
#define DEVINFO_SERIVE_UUID             @"180A"

typedef struct __attribute__((packed)) {
    uint8_t pin;
    uint8_t cmd;
} IOCTRL;

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    self.BleCentralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self scanDevice];
    });
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table View

- (IBAction)segmentAction:(id)sender
{
    UITableViewCell* cell = [[sender superview] superview];
    NSIndexPath *indexPath =
    [self.TableView indexPathForCell:cell];
    NSUInteger row = indexPath.row;
    IOCTRL data = {row, 2};
    if (self.ConnPeri && self.ConnChar)
        [self.ConnPeri writeValue:[NSData dataWithBytes:&data length:2] forCharacteristic:self.ConnChar
                                   type:CBCharacteristicWriteWithoutResponse];
}

- (IBAction)switchAction:(id)sender
{
    UITableViewCell* cell = [[sender superview] superview] ;
    NSIndexPath *indexPath =
    [self.TableView indexPathForCell:cell];
    NSUInteger row = indexPath.row;
    NSInteger  onoff = 0;
    if (((UISwitch*)sender).on)
        onoff = 1;
    IOCTRL data = { row, onoff};
    if (self.ConnPeri && self.ConnChar)
        [self.ConnPeri writeValue:[NSData dataWithBytes:&data length:2] forCharacteristic:self.ConnChar
                         type:CBCharacteristicWriteWithoutResponse];
    
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;//[[self.fetchedResultsController sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    //    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    return 32;//[sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];
    UILabel *label = (UILabel *)[cell viewWithTag:1];
    label.text = [NSString stringWithFormat:@"%2ld : ", (long)indexPath.row];
    UISegmentedControl *segCtrl = (UISegmentedControl*)[cell viewWithTag:2];
    [segCtrl addTarget:self action:@selector(segmentAction:) forControlEvents:UIControlEventValueChanged];
    UISwitch *switchCtrl = (UISwitch*)[cell viewWithTag:3];
    [switchCtrl addTarget:self action:@selector(switchAction:) forControlEvents:UIControlEventValueChanged];
    //    [self configureCell:cell atIndexPath:indexPath];
    /*EkoCCBlue *object = _objects[indexPath.row];
    if (object)
    {
        NSLog(@"Device name %@", object.Name);
        cell.textLabel.text = object.Name;//[NSString stringWithFormat:@"%@", [object Name]];
    }*/
    
    return cell;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return NO;
}


//
//  Bluetooth stuffs start here
//

- (void) scanDevice
{
//    [_objects removeAllObjects];
//    [self.tableView reloadData];
    
    if (_BleCentralManager.state == CBCentralManagerStatePoweredOn)
    {
        
        // Scans for any peripheral
        CBUUID *serviceUUID = [CBUUID UUIDWithString:BLINKY_SERVICE_UUID];
        
        NSArray *peripherals = [_BleCentralManager retrieveConnectedPeripheralsWithServices:@[serviceUUID]];
        if (peripherals.count <= 0)
            [self.BleCentralManager scanForPeripheralsWithServices:@[serviceUUID] options:nil];
        else
        {
            NSLog(@"Found Connected peripheral %@", peripherals);
            //            [_DeviceList addObject:peripherals[0]];//[NSString stringWithFormat:@"%@", peripheral.name]];
            //            [_DevicePickerView reloadAllComponents];
            for (CBPeripheral *peripheral in peripherals)
            {
//                _CurrPeripheral = peripheral;
                //peripheral.delegate = self;
                // [self.BleCentralManager connectPeripheral:peripheral options:nil];
            }
        }
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {

    // Stops scanning for peripheral
    [self.BleCentralManager stopScan];
    
    peripheral.delegate = self;
    self.ConnPeri = peripheral;
    
    // Connects to the discovered peripheral
    [self.BleCentralManager connectPeripheral:peripheral options:nil];
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral
{
    CBUUID *servUUID = [CBUUID UUIDWithString:BLINKY_SERVICE_UUID];
    
    [peripheral discoverServices:@[servUUID]];
    NSLog(@"Peripheral connected");
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
    CBUUID *serviceUUID = [CBUUID UUIDWithString:BLINKY_SERVICE_UUID];
    CBUUID *charUUID = [CBUUID UUIDWithString:BLINKY_CHAR_UUID];
    
    for (CBService *service in peripheral.services) {
        NSLog(@"Discovered service %@", service);
        
        if ([service.UUID isEqual:(serviceUUID)])
            [peripheral discoverCharacteristics:@[charUUID] forService:service];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    if (error) {
        NSLog(@"Error discovering characteristics: %@", [error localizedDescription]);
        //        [self cleanup];
        return;
    }
    
    CBUUID *charUUID = [CBUUID UUIDWithString:BLINKY_CHAR_UUID];
    for (CBCharacteristic *characteristic in service.characteristics)
    {
        NSLog(@"Discovered characteristic %@", characteristic);
        if ([characteristic.UUID isEqual:(charUUID)])
        {
            self.ConnChar = characteristic;
            if (characteristic.properties & CBCharacteristicPropertyRead)
                [peripheral readValueForCharacteristic:characteristic];
        }
    }
    
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if (!error)
    {
        
        CBUUID *charUUID = [CBUUID UUIDWithString:BLINKY_CHAR_UUID];//DEVINFO_SYSID_UUID];
        
        if ([characteristic.UUID isEqual:(charUUID)])
        {
            int64_t d = *(int64_t*)([characteristic.value bytes]);
            //NSString* name = [NSString stringWithFormat:@"%@-%llX", peripheral.name, d];
            //EkoCCBlue *dev = [[EkoCCBlue alloc] init:name peripheral:peripheral];
            //if (!_objects) {
            //    _objects = [[NSMutableArray alloc] init];
            //}
            //[_objects insertObject:dev atIndex:0];
            
            //_CurrPeripheral = nil;
            
            //[self.tableView reloadData];
            
            //NSIndexPath *indexPath = [NSIndexPath indexPathForRow:0 inSection:0];
            //[self.tableView insertRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
            //[self.BleCentralManager cancelPeripheralConnection:peripheral];
        }
    }
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    
    //[self.detailViewController deviceDisconnected];
    [self scanDevice];
}

- (void)centralManager:(CBCentralManager *)central didRetrieveConnectedPeripherals:(NSArray *)peripherals
{
    NSLog(@"Connected peripheral %@", peripherals);
    
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    
}



@end
