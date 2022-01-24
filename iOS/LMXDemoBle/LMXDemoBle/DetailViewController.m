//
//  DetailViewController.m
//  LMXDemoBle
//
//  Created by Hoan on 2014-09-20.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import "DetailViewController.h"

char *g_Attribute[] = {
    "Justify Left",
    "Justify Center",
    "Justify Right",
    "Scroll Left",
    "Scroll Right"
};

char g_Data[512];

@interface DetailViewController ()
@property (strong, nonatomic) UIPopoverController *masterPopoverController;
- (void)configureView;
@end

@implementation DetailViewController

#pragma mark - Managing the detail item

- (void)setDetailItem:(id)newDetailItem
{
    if (_detailItem != newDetailItem) {
        _detailItem = newDetailItem;
        
        // Update the view.
        [self configureView];
        CBPeripheral *peripheral = (CBPeripheral*)newDetailItem;
        CBUUID *servUUID = [CBUUID UUIDWithString:LMX_SERVICE_UUID];
        
        peripheral.delegate = self;
        
        //    BlueIODev *dev = [[BlueIODev alloc] initWithPeripheral:peripheral andCentral:central];
        //    if (peripheral == _CurrPeripheral)
        [peripheral discoverServices:@[servUUID]];
    }

    if (self.masterPopoverController != nil) {
        [self.masterPopoverController dismissPopoverAnimated:YES];
    }        
}

- (void)configureView
{
    // Update the user interface for the detail item.

 //   if (self.detailItem) {
   //     self.detailDescriptionLabel.text = [self.detailItem description];
    //}
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self configureView];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) sendString:(NSString *)string
{
    CBPeripheral *peripheral = (CBPeripheral*)_detailItem;
    NSMutableData *data;
    UIPickerView *picker = (UIPickerView*)[self.view viewWithTag:1];
    uint8_t att = 0;
    
    if (picker)
    {
        att = [picker selectedRowInComponent:0];
    }
    
    g_Data[0] = (char)att;
    
    data = [NSMutableData dataWithBytes:&att length:1];
    [data appendData:[NSMutableData dataWithBytes:string.UTF8String length:string.length]];
    
    [peripheral writeValue:data forCharacteristic:self.characteristic type:CBCharacteristicWriteWithoutResponse];
}

- (IBAction)sendButtonPressed:(id)sender
{
    UITextField* txt  = (UITextField*)[self.view viewWithTag:2];
    [self sendString:txt.text];
}

#pragma mark - Split view

- (void)splitViewController:(UISplitViewController *)splitController willHideViewController:(UIViewController *)viewController withBarButtonItem:(UIBarButtonItem *)barButtonItem forPopoverController:(UIPopoverController *)popoverController
{
    barButtonItem.title = NSLocalizedString(@"Master", @"Master");
    [self.navigationItem setLeftBarButtonItem:barButtonItem animated:YES];
    self.masterPopoverController = popoverController;
}

- (void)splitViewController:(UISplitViewController *)splitController willShowViewController:(UIViewController *)viewController invalidatingBarButtonItem:(UIBarButtonItem *)barButtonItem
{
    // Called when the view is shown again in the split view, invalidating the button and popover controller.
    [self.navigationItem setLeftBarButtonItem:nil animated:YES];
    self.masterPopoverController = nil;
}

#pragma mark - Picker view

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    //[_currencyTableView reloadData];
}

// The number of columns of data
- (int)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

// The number of rows of data
- (int)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    return sizeof(g_Attribute)/sizeof(int);
}

// The data to return for the row and component (column) that's being passed in
- (NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [NSString stringWithCString:g_Attribute[row] encoding:NSASCIIStringEncoding];
}

#pragma mark - UITextField

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    NSLog(@"textFieldShouldReturn entered %@",textField.text);
    [textField resignFirstResponder];
    return YES;
}

#pragma mark - Bluetooth

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
    
    for (CBService *service in peripheral.services) {
        NSLog(@"Discovered service %@", service);
        CBUUID *serviceUUID = [CBUUID UUIDWithString:LMX_SERVICE_UUID];
        
        if ([service.UUID isEqual:(serviceUUID)])
        {
            [peripheral discoverCharacteristics:nil forService:service];
            
        }
    }
    //[self.BleCentralManager cancelPeripheralConnection:_ConnectedPeripheral];
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    CBUUID *charUUID = [CBUUID UUIDWithString:LMX_CHAR_UUID];
    
    for (CBCharacteristic *characteristic in service.characteristics)
    {
        NSLog(@"DetailView Charact %@", characteristic);
        if ([characteristic.UUID isEqual:(charUUID)])
        {
            
            _characteristic = characteristic;
            //           [characteristic.value  getBytes:data];
            //            [self updateCounts:data];
            //[_dataTableView reloadData];
            
            if (characteristic.properties & CBCharacteristicPropertyWriteWithoutResponse )
                NSLog(@"DetailView Charact write ok");
            
        }
    }
    
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if (error)
        return;
    
    CBUUID *charUUID = [CBUUID UUIDWithString:LMX_CHAR_UUID];
    
    if ([characteristic.UUID isEqual:(charUUID)])
    {
        NSLog(@"DetailView didUpdateValueForCharacteristic data %@", characteristic);
        UITextView* tview = (UITextView*)[self.view viewWithTag:1];
        
        NSString *string = [NSString stringWithUTF8String:[[characteristic value] bytes]];
        tview.text = [tview.text stringByAppendingFormat:@"%@", string];
    }
    
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic
             error:(NSError *)error {
    
    if (error) {
        NSLog(@"Error changing notification state: %@",
              [error localizedDescription]);
    }
    [peripheral readValueForCharacteristic:characteristic];
    
}

- (void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic
             error:(NSError *)error
{
    
    if (error) {
        NSLog(@"Error writing characteristic value: %@",
              [error localizedDescription]);
    }
}

- (void)deviceDisconnected
{
    // Device was diconnected
    _detailItem = nil;
    //    memset(newflag, 0, sizeof(newflag));
    //    memset(DiamCounts, 0, sizeof(DiamCounts));
    NSLog(@"Device disconnected");
}

@end
