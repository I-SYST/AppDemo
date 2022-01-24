//
//  DetailViewController.h
//  LMXDemoBle
//
//  Created by Hoan on 2014-09-20.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>

// Base UUID : 9df5bd40-4070-11e4-84d0-0002a5d5c51b
#define LMX_SERVICE_UUID        @"9df5bd41-4070-11e4-84d0-0002a5d5c51b"
#define LMX_CHAR_UUID           @"9df5bd42-4070-11e4-84d0-0002a5d5c51b"
#define DEVINFO_SERIVE_UUID     @"180A"

@interface DetailViewController : UIViewController <UISplitViewControllerDelegate, CBPeripheralDelegate, UITextFieldDelegate>

@property (strong, nonatomic) id detailItem;

@property (nonatomic, strong) CBCharacteristic *characteristic;
@property (nonatomic, strong) IBOutlet UIPickerView* atributePicker;
- (void) sendString:(NSString *) string;
@end
