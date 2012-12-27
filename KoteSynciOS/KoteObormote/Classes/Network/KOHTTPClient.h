//
//  KOHTTPClient.h
//  KoteObormote
//
//  Created by Anastasi Voitova on 07.11.12.
//  Copyright (c) 2012 kpi. All rights reserved.
//

#import "AFHTTPClient.h"

@interface KOHTTPClient : AFHTTPClient

+ (KOHTTPClient *)shared;


- (void)getAppsInfoWithSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                       failure:(void (^)(AFHTTPRequestOperation * operation, NSError * error))failure;


- (void)requestInfoSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                   failure:(void (^)(AFHTTPRequestOperation * operation, NSError * error))failure;


- (void)requestRegisterSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                       failure:(void (^)(AFHTTPRequestOperation * operation, NSError * error))failure;

- (void)requestICanHearOnSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                         failure:(void (^)(AFHTTPRequestOperation *, NSError * error))failure;


- (void)requestConnectSuccess:(void (^)(AFHTTPRequestOperation * operation, id object))success
                      failure:(void (^)(AFHTTPRequestOperation *, NSError * error))failure;
@end
