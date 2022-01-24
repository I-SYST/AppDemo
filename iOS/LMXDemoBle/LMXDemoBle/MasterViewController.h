//
//  MasterViewController.h
//  LMXDemoBle
//
//  Created by Hoan on 2014-09-20.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>

@class DetailViewController;

@interface MasterViewController : UITableViewController <CBCentralManagerDelegate>

@property (strong, nonatomic) DetailViewController *detailViewController;
@property (strong, nonatomic) CBCentralManager *BleCentralManager;

@end
