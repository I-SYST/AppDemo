/*--------------------------------------------------------------------------
 File   : DetailViewController.h
 
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
//  DetailViewController.h
//  UartBle
//
//  Created by Hoan on 2014-09-17.
//  Copyright (c) 2014 I-SYST inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreBluetooth/CoreBluetooth.h>



#define UART_SERVICE_UUID       @"6e400001-b5a3-f393-e0a9-e50e24dcca9e"
#define UART_DATA_CHAR_UUID     @"6e400002-b5a3-f393-e0a9-e50e24dcca9e"
#define UART_CTRL_CHAR_UUID     @"6e400003-b5a3-f393-e0a9-e50e24dcca9e"
#define DEVINFO_SERIVE_UUID     @"180A"

@interface DetailViewController : UIViewController <UISplitViewControllerDelegate, CBPeripheralDelegate, UITextFieldDelegate>

@property (strong, nonatomic) id detailItem;

//@property (weak, nonatomic) IBOutlet UILabel *detailDescriptionLabel;
@property (weak, nonatomic) IBOutlet UILabel *consoleTextView;
@property (nonatomic, strong) CBCharacteristic *dataCharacteristic;
@property (nonatomic, strong) CBCharacteristic *ctrlCharacteristic;
@property (nonatomic, strong) IBOutlet UIPickerView* BaudratePicker;
- (void) writeString:(NSString *) string;
@end
