//
//  KOHTTPClient.m
//  KoteObormote
//
//  Created by Anastasi Voitova on 07.11.12.
//  Copyright (c) 2012 kpi. All rights reserved.
//

#import "KOHTTPClient.h"
#import "KOHTTPRequestOperation.h"
#import "AFJSONRequestOperation.h"
#import "NSObject+ClassCast.h"
#import "NSMutableDictionary+NotNilValue.h"
#import "KOSessionManager.h"

#import <ifaddrs.h>
#import <arpa/inet.h>

#define BaseURL         @"http://kote-obormote.appspot.com/"


@implementation KOHTTPClient


#pragma mark - Singletone

+ (KOHTTPClient *)shared {
   static KOHTTPClient * instance;
   static dispatch_once_t onceToken;
   dispatch_once(&onceToken, ^{
      instance = [[self alloc] initWithBaseURL:[NSURL URLWithString:BaseURL]];
   });
   return instance;
}


- (id)initWithBaseURL:(NSURL *)url {
   self = [super initWithBaseURL:url];
   if (self) {
      
      [self registerHTTPOperationClass:[AFJSONRequestOperation class]];
   }
   return self;
}


#pragma mark - Extra -

- (NSString *)fullPathWithToken:(NSString *)token {
   return [NSString stringWithFormat:@"%@%@", BaseURL, token];
}

- (NSString *)getIPAddress {
   struct ifaddrs *interfaces = NULL;
   struct ifaddrs *temp_addr = NULL;
   NSString *wifiAddress = nil;
   NSString *cellAddress = nil;
   
   // retrieve the current interfaces - returns 0 on success
   if(!getifaddrs(&interfaces)) {
      // Loop through linked list of interfaces
      temp_addr = interfaces;
      while(temp_addr != NULL) {
         sa_family_t sa_type = temp_addr->ifa_addr->sa_family;
         if(sa_type == AF_INET || sa_type == AF_INET6) {
            NSString *name = [NSString stringWithUTF8String:temp_addr->ifa_name];
            NSString *addr = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)]; // pdp_ip0
            NSLog(@"NAME: \"%@\" addr: %@", name, addr); // see for yourself
            
            if([name isEqualToString:@"en0"]) {
               // Interface is the wifi connection on the iPhone
               wifiAddress = addr;
            } else
               if([name isEqualToString:@"pdp_ip0"]) {
                  // Interface is the cell connection on the iPhone
                  cellAddress = addr;
               }
         }
         temp_addr = temp_addr->ifa_next;
      }
      // Free memory
      freeifaddrs(interfaces);
   }
   NSString *addr = wifiAddress ? wifiAddress : cellAddress;
   return addr ? addr : @"0.0.0.0";
}


- (NSString *)getPort {
   return @"22816";
}

#pragma mark - Requests -

- (void)getAppsInfoWithSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                       failure:(void (^)(AFHTTPRequestOperation * operation, NSError * error))failure {
   [self getPath:BaseURL parameters:nil success:success failure:failure];
}


- (void)requestInfoSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                   failure:(void (^)(AFHTTPRequestOperation * operation, NSError * error))failure {
   
   [self getPath:[self fullPathWithToken:@"info"]
       parameters:nil
          success:success
          failure:failure];
}


- (void)requestRegisterSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                       failure:(void (^)(AFHTTPRequestOperation * operation, NSError * error))failure {
   
   NSMutableDictionary *params = [NSMutableDictionary dictionary];
   
   [params setObjectIfNotNil:[[KOSessionManager shared] login] forKey:@"login"];
   [params setObjectIfNotNil:[[KOSessionManager shared] password] forKey:@"password"];
   
   NSLog(@"%@ - %@", NSStringFromSelector(_cmd), params);
   
   [self postPath:[self fullPathWithToken:@"register"]
       parameters:params
          success:success
          failure:failure];
}


- (void)requestICanHearOnSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                  failure:(void (^)(AFHTTPRequestOperation *, NSError * error))failure {
   
   NSMutableDictionary *params = [NSMutableDictionary dictionary];
   
   [params setObjectIfNotNil:[[KOSessionManager shared] login] forKey:@"login"];
   [params setObjectIfNotNil:[[KOSessionManager shared] password] forKey:@"password"];
   
   
   [params setObjectIfNotNil:[self getIPAddress] forKey:@"address"];
   [params setObjectIfNotNil:[self getPort] forKey:@"port"];
   
   NSLog(@"%@ - %@", NSStringFromSelector(_cmd), params);
   
   [self postPath:[self fullPathWithToken:@"icanhearon"]
       parameters:params
          success:success
          failure:failure];
}


- (void)requestConnectSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                      failure:(void (^)(AFHTTPRequestOperation *, NSError * error))failure {
   
   NSMutableDictionary *params = [NSMutableDictionary dictionary];
   
   [params setObjectIfNotNil:[[KOSessionManager shared] login] forKey:@"login"];
   [params setObjectIfNotNil:[[KOSessionManager shared] password] forKey:@"password"];

   [params setObjectIfNotNil:[[KOSessionManager shared] deviceTo] forKey:@"deviceTo"];
   
   NSLog(@"%@ - %@", NSStringFromSelector(_cmd), params);
   
   [self postPath:[self fullPathWithToken:@"connect"]
       parameters:params
          success:success
          failure:failure];
}
@end
