//
//  KOViewController.m
//  KoteObormote
//
//  Created by Anastasi Voitova on 07.11.12.
//  Copyright (c) 2012 kpi. All rights reserved.
//

#import "KOViewController.h"
#import "UIView+SFAdditions.h"
#import "KOHTTPClient.h"
#import "AFHTTPRequestOperation.h"
#import "JSONKit.h"
#import "Server.h"
#import "KOSessionManager.h"

#define defaultLabelWidth  300

@interface KOViewController ()

@property (nonatomic, strong) UILabel *textLabel;
@property (nonatomic, strong) UIActivityIndicatorView *activity;

@property (nonatomic, strong) UITextField *textView;

@property (nonatomic, strong) UIButton *registerButton;

@property (nonatomic, strong) NSInputStream * readStream;
@property (nonatomic, strong) NSOutputStream * writeStream;

@property (nonatomic, strong) NSData *readStreamData;
@property (nonatomic, strong) NSMutableData *writeStreamData;

@property (nonatomic, assign) int byteIndex;

@property (nonatomic, strong) Server *server;
@end

@implementation KOViewController

- (NSString *)nibName {
   return nil;
}


- (UIButton *)registerButton {
   if (!_registerButton) {
      _registerButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
      [_registerButton setTitle:@"Register" forState:UIControlStateNormal];
      [_registerButton sizeToFit];
      [_registerButton addTarget:self action:@selector(registerButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
   }
   return _registerButton;
}


- (UILabel *)textLabel {
   if (!_textLabel) {
      _textLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, defaultLabelWidth, 44)];
      [_textLabel setNumberOfLines:0];
   }
   return _textLabel;
}


- (UIActivityIndicatorView *)activity {
   if (!_activity) {
      _activity = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
      [_activity sizeToFit];
      _activity.alpha = 0;
   }
   return _activity;
}


- (UITextField *)textView {
   if (!_textView) {
      _textView = [[UITextField alloc] initWithFrame:CGRectMake(0, 0, 320, 44)];
      _textView.borderStyle = UITextBorderStyleRoundedRect;
      _textView.autocorrectionType = UITextAutocorrectionTypeNo;
      [_textView setReturnKeyType:UIReturnKeySend];
      _textView.delegate = self;
   }
   return _textView;
}


#pragma mark - Life circle -

- (void)viewDidLoad {
   [super viewDidLoad];
   
   self.view.backgroundColor = [UIColor whiteColor];
   
}



- (void)viewWillAppear:(BOOL)animated {
   [super viewWillAppear:animated];
   
   [self.view addSubview:self.activity];
   _activity.left = ceilf((self.view.width - _activity.width) / 2);
   _activity.top = 20;

   [self.view addSubview:self.textLabel];
   _textLabel.left = ceilf((self.view.width - _textLabel.width) / 2);
   _textLabel.top = _activity.bottom + 10;
   
   [self.view addSubview:self.textView];
   _textView.left = ceilf((self.view.width - _textView.width) / 2);
   _textView.top = _textLabel.bottom + 10;
   
//   [self.view addSubview:self.registerButton];
//   _registerButton.left = ceilf((self.view.width - _registerButton.width) / 2);
//   _registerButton.top = _textView.bottom + 10;
   
//   [self registerButtonPressed:nil];
   
   [_textView becomeFirstResponder];
   
   [self showActivity];
   
   [self sendICanHearYouRequest];
   [self sendConnectRequest];
}


#pragma mark - Actions -

- (void)showActivity {
   _activity.alpha = 0;
   [_activity startAnimating];
   [UIView animateWithDuration:0.3
                    animations:^{
                       _activity.alpha = 1;
                    }];
}


- (void)hideActivity {
   if (_activity.alpha > 0) {
      [UIView animateWithDuration:0.3
                       animations:^{
                          _activity.alpha = 0;
                          [_activity stopAnimating];
                       }];
   }
}


- (void)showTextLabel {
   _textLabel.alpha = 0;
   [UIView animateWithDuration:0.3
                    animations:^{
                       _textLabel.alpha = 1;
                    }];
}


- (void)hideTextLabel {
   if (_textLabel.alpha > 0) {
      [UIView animateWithDuration:0.3
                       animations:^{
                          _textLabel.alpha = 0;
                       }];
   }
}


#pragma mark -

- (void)registerButtonPressed:(id)sender {
      
   [[KOHTTPClient shared] requestRegisterSuccess:^(AFHTTPRequestOperation *operation, id object) {
      NSLog(@"%@ success", NSStringFromSelector(_cmd));
      [self parseServerResponse:object];
      
   } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
      NSLog(@"%@ fail %@", NSStringFromSelector(_cmd), error);
      [self hideActivity];
      
   }];
}


- (void)sendICanHearYouRequest {
   [[KOHTTPClient shared] requestICanHearOnSuccess:^(AFHTTPRequestOperation *operation, id object) {
     // NSLog(@"%@ success", NSStringFromSelector(_cmd));
      
   } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
      NSLog(@"%@ fail %@", NSStringFromSelector(_cmd), error);
      [self hideActivity];
      
   }];
}


- (void)sendConnectRequest {   
   [[KOHTTPClient shared] requestConnectSuccess:^(AFHTTPRequestOperation *operation, id object) {
      NSLog(@"%@ success", NSStringFromSelector(_cmd));
      NSDictionary *response = [self parseServerResponse:object];
      if (response) {
         NSString *address = [response objectForKey:@"address"];
         NSString *port = [response objectForKey:@"port"];
         
         if ([address length] > 0 && [port length] > 0) {
            [[KOSessionManager shared] setServerIP:address];
            [[KOSessionManager shared] setServerPort:port];
            
            
            [self openStreamForReading];
            
            
            [self hideActivity];
         }
         
      }
   } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
      NSLog(@"%@ fail %@", NSStringFromSelector(_cmd), error);
      [self hideActivity];

   }];
}


- (NSDictionary *)parseServerResponse:(id)response {
   NSData *resp = [NSData dataWithData:response];
   NSDictionary *data = (NSDictionary *)[resp objectFromJSONData];
   if (data) {
      NSLog(@"%@", data);
   }
   return data;
}


#pragma mark - 

- (void)openStreamForReading {
   NSString *type = @"TestingProtocol";
   _server = [[Server alloc] initWithProtocol:type];
   _server.delegate = self;
   NSError *error = nil;
   if(![_server start:&error]) {
      NSLog(@"error = %@", error);
   }
}


- (void)saySmthToServer:(NSString *)smth {
   if ([smth length] > 0) {
      self.writeStreamData = [NSMutableData dataWithData:[smth dataUsingEncoding:NSUTF8StringEncoding]];
      
      
      CFWriteStreamRef cfStream;
      CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)[[KOSessionManager shared] serverIP], [[[KOSessionManager shared] serverPort] integerValue], nil, &cfStream);
      _writeStream = (__bridge_transfer NSOutputStream *)cfStream;
      [_writeStream setDelegate:self];
      [_writeStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
      [_writeStream open];
   }
}

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode {
   NSLog(@"stream %@ event %i", aStream, eventCode);
   NSMutableData *data = [NSMutableData data];
   
   
   if ([aStream isKindOfClass:[NSOutputStream class]]) {
      switch(eventCode) {
         case NSStreamEventHasSpaceAvailable: {
            uint8_t *readBytes = (uint8_t *)[self.writeStreamData mutableBytes];
            readBytes += _byteIndex;
            int data_len = [self.writeStreamData length];
            unsigned int len = ((data_len - _byteIndex >= 1024) ?
                                1024 : (data_len - _byteIndex));
            uint8_t buf[len];
            (void)memcpy(buf, readBytes, len);
            len = [(NSOutputStream *)aStream write:(const uint8_t *)buf maxLength:len];
            _byteIndex += len;
            break;
         }
      }
   } else {
      switch(eventCode) {
         case NSStreamEventHasBytesAvailable: {
            uint8_t buf[1024];
            unsigned int len = 0;
            len = [(NSInputStream *)aStream read:buf maxLength:1024];
            if (len) {
               [data appendBytes:(const void *)buf length:len];
            } else {
               NSLog(@"no buffer!");
            }
            break;
         }
      }
   }
   
   switch(eventCode) {
      case NSStreamEventEndEncountered: {
         [aStream close];
         [aStream removeFromRunLoop:[NSRunLoop currentRunLoop]
                            forMode:NSDefaultRunLoopMode];
         aStream = nil;
         _byteIndex = 0;
         break;
      }
      case NSStreamEventErrorOccurred: {
         NSError *theError = [aStream streamError];
         NSLog(@"stream error");
         NSLog(@"%@", [NSString stringWithFormat:@"Error %i: %@",
                       [theError code], [theError localizedDescription]]);
         [aStream close];
         aStream = nil;
         _byteIndex = 0;
         break;
      }
   }
   
   NSLog(@"data %@", data);
}


#pragma mark Server Delegate Methods

- (void)serverRemoteConnectionComplete:(Server *)server {
   NSLog(@"Server Started");
}

- (void)serverStopped:(Server *)server {
   NSLog(@"Server stopped");
}

- (void)server:(Server *)server didNotStart:(NSDictionary *)errorDict {
   NSLog(@"Server did not start %@", errorDict);
}

- (void)server:(Server *)server didAcceptData:(NSData *)data {
   NSLog(@"Server did accept data %@", data);
   NSString *message = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
   if (nil != message && [message length] > 0) {
      NSLog(@"message %@", message);
      [_textLabel setText:message];
      
      CGFloat width = _textLabel.width;
      [_textLabel sizeToFit];
      _textLabel.width = width;
      
   } else {
      NSLog(@"no data received");
   }
}


- (void)server:(Server *)server lostConnection:(NSDictionary *)errorDict {
   NSLog(@"Server lost connection %@", errorDict);
}

- (void)serviceRemoved:(NSNetService *)service moreComing:(BOOL)more {
   
}

- (void)serviceAdded:(NSNetService *)service moreComing:(BOOL)more {
   
}

#pragma mark -

- (void)textViewDidEndEditing:(UITextView *)textView {

}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
   [self saySmthToServer:textField.text];
   //[textField resignFirstResponder];
   return YES;
}

@end
