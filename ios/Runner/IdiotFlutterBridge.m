//
//  IdiotFlutterBridge.m
//  Runner
//
//  Created by vicky Leu on 2023/12/17.
//

#import "IdiotFlutterBridge.h"

@implementation IdiotFlutterBridge
+ (void)registerWith:(NSObject<FlutterPluginRegistry>*)registry {
    [GeneratedPluginRegistrant registerWithRegistry:registry];
    return;
}
@end
