//
//  EventEmitter.swift
//  ekyc
//
//  Created by Cuong on 5/20/23.
//

import React

public class EventEmitter {
  public static var shared = EventEmitter()
  
  private static var eventEmitter: ReactNativeEventEmitter!
  
  private init() {}
  
  func registerEventEmitter(_ eventEmitter: ReactNativeEventEmitter) {
    EventEmitter.eventEmitter = eventEmitter
  }
  
  func dispatch(event: EventEmitterName, body: Any?) {
    EventEmitter.eventEmitter.sendEvent(withName: event.rawValue, body: body)
  }
  
  var allEvents: [String] {
    EventEmitterName.allCases.map { $0.rawValue }
  }
}

public enum EventEmitterName: String, CaseIterable {
  case onMRZScanned
  case onNFCScanned
  case onLivenessChecked
}
