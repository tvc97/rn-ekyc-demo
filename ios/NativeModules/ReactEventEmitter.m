//
//  ReactEventEmitter.m
//  ekyc
//
//  Created by Cuong on 5/20/23.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(ReactNativeEventEmitter, RCTEventEmitter)

RCT_EXTERN_METHOD(supportedEvents)

@end
