//
//  MRZViewManager.swift
//  ekyc
//
//  Created by Cuong on 5/20/23.
//

import UIKit
import React
import QKMRZParser

@objc(MRZViewManager)
class MRZViewManager: RCTViewManager {
  override func view() -> UIView! {
    let scannerView = QKMRZScannerView()
    scannerView.delegate = self
    scannerView.startScanning()
    return scannerView
  }
  
  override class func requiresMainQueueSetup() -> Bool {
    false
  }
}

extension MRZViewManager: QKMRZScannerViewDelegate {
    func mrzScannerView(_ mrzScannerView: QKMRZScannerView, didFind scanResult: QKMRZScanResult) {
      let dateFormatter = DateFormatter()
      dateFormatter.dateFormat = "dd/MM/YYYY"
      let humanizeDate = DateFormatter()
      humanizeDate.dateFormat = "YYYY-MM-dd"
      
      NativeModuleManager.sharedValues.cardId = scanResult.documentNumber
      NativeModuleManager.sharedValues.dateOfBirth = dateFormatter.string(from: scanResult.birthdate!)
      NativeModuleManager.sharedValues.dateOfExpiry = dateFormatter.string(from: scanResult.expiryDate!)

      EventEmitter.shared.dispatch(event: .onMRZScanned, body: [
        "documentNumber": scanResult.documentNumber,
        "birthdate": humanizeDate.string(from: scanResult.birthdate!),
        "expiryDate": humanizeDate.string(from: scanResult.expiryDate!),
        "mrz": mrzScannerView.mrz
      ])
      
    }
}
