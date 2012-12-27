//
//  KOSessionManager.m
//  KoteObormote
//
//  Created by Anastasi Voitova on 20.12.12.
//  Copyright (c) 2012 kpi. All rights reserved.
//

#import "KOSessionManager.h"

@implementation KOSessionManager

#pragma mark - Singletone -

+ (KOSessionManager *)shared {
   static KOSessionManager * instance;
   static dispatch_once_t onceToken;
   dispatch_once(&onceToken, ^{
      instance = [[self alloc] init];
   });
   return instance;
}

#pragma mark - 

- (NSString *)login {
   return @"device2";
}


- (NSString *)password {
   return @"dev3pass";
}


- (NSString *)deviceTo {
   return @"device1";
}

@end
