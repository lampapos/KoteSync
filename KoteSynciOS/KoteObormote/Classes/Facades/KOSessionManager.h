//
//  KOSessionManager.h
//  KoteObormote
//
//  Created by Anastasi Voitova on 20.12.12.
//  Copyright (c) 2012 kpi. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface KOSessionManager : NSObject

@property (nonatomic, copy) NSString *login;
@property (nonatomic, copy) NSString *password;

@property (nonatomic, copy) NSString *deviceTo;

@property (nonatomic, copy) NSString *serverIP;
@property (nonatomic, copy) NSString *serverPort;


+ (KOSessionManager *)shared;

@end
