//
//  ReactNativeEventEmitter.swift
//  ekyc
//
//  Created by Cuong on 5/20/23.
//

import React

@objc(ReactNativeEventEmitter)
open class ReactNativeEventEmitter: RCTEventEmitter {
  open override class func requiresMainQueueSetup() -> Bool {
    true
  }
  
  public override init() {
    super.init()
    EventEmitter.shared.registerEventEmitter(self)
  }
  
  open override func supportedEvents() -> [String]! {
    EventEmitter.shared.allEvents
  }
}
