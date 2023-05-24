//
//  NativeModuleManager.swift
//  ekyc
//
//  Created by Cuong on 5/20/23.
//

import React
import UIKit
import IDCardReader

@objc(NativeModuleManager)
final class NativeModuleManager: NSObject {
  public static var sharedValues = SharedValues(cardId: "", dateOfBirth: "", dateOfExpiry: "")
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  override init() {
    guard let url = Bundle.main.url(forResource: "license", withExtension: "txt") else {
        return
    }
    do {
        let privateKey = try String(contentsOf: url, encoding: .utf8)
        IDCardReaderManager.shared.setup(appId: "com.pvcb",
                                         license: privateKey)
    } catch {
        print(error)
    }
  }

  @objc
  func startScanNFC() {
    Task {
      let parsedInformation = try await IDCardReaderManager.shared.readIDCard(cardId: NativeModuleManager.sharedValues.cardId, dateOfBirth: NativeModuleManager.sharedValues.dateOfBirth, dateOfExpiry: NativeModuleManager.sharedValues.dateOfExpiry)

        DispatchQueue.main.async {
          EventEmitter.shared.dispatch(event: .onNFCScanned, body: parsedInformation.toJSON())
        }
    }
  }
}

struct SharedValues {
  var cardId: String
  var dateOfBirth: String
  var dateOfExpiry: String
}
