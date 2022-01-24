/*--------------------------------------------------------------------------
 File   : DetailViewController.m
 
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
 
 ----------------------------------------------------------------------------*///
//  DetailViewController.m
//  UartBle
//
//  Created by Hoan on 2014-09-17.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import "DetailViewController.h"

int g_BaudrateTable[] = {
    1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 76800, 115200, 230400,
    250000, 460800, 921600
};

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
        CBUUID *servUUID = [CBUUID UUIDWithString:UART_SERVICE_UUID];
        
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

    //if (self.detailItem) {
    //    self.detailDescriptionLabel.text = [self.detailItem description];
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

- (void) writeString:(NSString *)string
{
    CBPeripheral *peripheral = (CBPeripheral*)_detailItem;
    NSData *data = [NSData dataWithBytes:string.UTF8String length:string.length];
    
    [peripheral writeValue:data forCharacteristic:self.dataCharacteristic type:CBCharacteristicWriteWithoutResponse];
}

- (IBAction)sendButtonPressed:(id)sender
{
    UITextField* txt  = (UITextField*)[self.view viewWithTag:2];
    [self writeString:txt.text];
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
    return sizeof(g_BaudrateTable)/sizeof(int);
}

// The data to return for the row and component (column) that's being passed in
- (NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return [NSString stringWithFormat:@"%d", g_BaudrateTable[row]];
}

#pragma mark - UITextField

- (void)textFieldDidEndEditing:(UITextField *)textField {
    NSLog(@"textFieldDidEndEditing entered %@",textField.text);
    
}

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
        CBUUID *serviceUUID = [CBUUID UUIDWithString:UART_SERVICE_UUID];
        CBUUID *datacharUUID = [CBUUID UUIDWithString:UART_DATA_CHAR_UUID];
        
        if ([service.UUID isEqual:(serviceUUID)])
        {
            [peripheral discoverCharacteristics:nil forService:service];
            
        }
    }
    //[self.BleCentralManager cancelPeripheralConnection:_ConnectedPeripheral];
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    CBUUID *datacharUUID = [CBUUID UUIDWithString:UART_DATA_CHAR_UUID];
    CBUUID *ctrlcharUUID = [CBUUID UUIDWithString:UART_CTRL_CHAR_UUID];
    
    for (CBCharacteristic *characteristic in service.characteristics)
    {
        NSLog(@"DetailView Charact %@", characteristic);
        if ([characteristic.UUID isEqual:(datacharUUID)])
        {
            
            _dataCharacteristic = characteristic;
//           [characteristic.value  getBytes:data];
//            [self updateCounts:data];
            //[_dataTableView reloadData];
            if (characteristic.properties & CBCharacteristicPropertyNotify)
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
        
            if (characteristic.properties & CBCharacteristicPropertyWriteWithoutResponse )
                NSLog(@"DetailView Charact write ok");
            
        }
        if ([characteristic.UUID isEqual:(ctrlcharUUID)])
        {
            _ctrlCharacteristic = characteristic;
        }
    }
    
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if (error)
        return;
    
    CBUUID *datacharUUID = [CBUUID UUIDWithString:UART_DATA_CHAR_UUID];
    CBUUID *ctrlcharUUID = [CBUUID UUIDWithString:UART_CTRL_CHAR_UUID];

    if ([characteristic.UUID isEqual:(datacharUUID)])
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
