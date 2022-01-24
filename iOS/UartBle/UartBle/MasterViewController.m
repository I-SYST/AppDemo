/*--------------------------------------------------------------------------
 File   : MasterViewController.m
 
 Author : Hoang Nguyen Hoan          Sep. 17, 2014
 
 Desc   : UART over custom BLE exemple
 
 Copyright (c) 2014, I-SYST inc., all rights reserved
 
 Permission to use, copy, modify, and distribute this software for any purpose
 with or without fee is hereby granted, provided that the above copyright
 notice and this permission notice appear in all copies, and none of the
 names : I-SYST or its contributors may be used to endorse or
 promote products derived from this software without specific prior written
 permission.
 
 For info or contributing contact : hnhoan at i-syst dot com
 
 THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 ----------------------------------------------------------------------------
 Modified by          Date              Description
 
 ----------------------------------------------------------------------------*/
//
//  MasterViewController.m
//  UartBle
//
//  Created by Hoan on 2014-09-17.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import "MasterViewController.h"

#import "DetailViewController.h"

@interface MasterViewController () {
    NSMutableArray *_objects;
}
@end

@implementation MasterViewController

- (void)awakeFromNib
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        self.clearsSelectionOnViewWillAppear = NO;
        self.preferredContentSize = CGSizeMake(320.0, 600.0);
    }
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    //self.navigationItem.leftBarButtonItem = self.editButtonItem;

    self.BleCentralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        [self scanBle];
    });

    UIBarButtonItem *addButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemRefresh target:self action:@selector(refreshBleScan:)];
    self.navigationItem.leftBarButtonItem = addButton;
    self.detailViewController = (DetailViewController *)[[self.splitViewController.viewControllers lastObject] topViewController];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)refreshBleScan:(id)sender
{
    if (_BleCentralManager.state == CBCentralManagerStatePoweredOn)
    {
        [_BleCentralManager stopScan];
        [self scanBle];
        
    }
}

#pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return _objects.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];

    CBPeripheral *object = _objects[indexPath.row];
    cell.textLabel.text = [object name];
    return cell;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [_objects removeObjectAtIndex:indexPath.row];
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view.
    }
}

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    //if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        CBPeripheral *object = _objects[indexPath.row];
        
        [self.BleCentralManager stopScan];
        if (object.state == CBPeripheralStateDisconnected)
            [self.BleCentralManager connectPeripheral:object options:nil];
        
//        self.detailViewController.detailItem = object;
   // }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"showDetail"]) {
        NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
        CBPeripheral *object = _objects[indexPath.row];
        [[segue destinationViewController] setDetailItem:object];
    }
}

#pragma mark BLE

- (void)centralManager:(CBCentralManager *)central didRetrieveConnectedPeripherals:(NSArray *)peripherals
{
    NSLog(@"Connected peripheral %@", peripherals);
    
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    //    NSLog(@"Central %@", central.state);
    
}

- (void) scanBle
{
    [_objects removeAllObjects];
    [self.tableView reloadData];
    
    if (_BleCentralManager.state == CBCentralManagerStatePoweredOn)
    {
        
        // Scans for any peripheral
        CBUUID *serviceUUID = [CBUUID UUIDWithString:UART_SERVICE_UUID];
        
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

- (void) deviceFound: (CBPeripheral*)peripheral
{
    NSLog(@"deviceFound peripheral %@", peripheral);
    
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    
    if (!_objects)
    {
        _objects = [[NSMutableArray alloc] init];
    }
    [_objects insertObject:peripheral atIndex:0];
    [self.tableView reloadData];
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral
{
    CBUUID *servUUID = [CBUUID UUIDWithString:UART_SERVICE_UUID];
    
    //    BlueIODev *dev = [[BlueIODev alloc] initWithPeripheral:peripheral andCentral:central];
    //    if (peripheral == _CurrPeripheral)
    self.detailViewController.detailItem = peripheral;
    NSLog(@"Peripheral connected");
    //[peripheral discoverServices:nil];//@[servUUID]];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    
    //    [self.detailViewController deviceDisconnected];
    [self scanBle];
}


@end
