//
//  LivenessViewManager.swift
//  ekyc
//
//  Created by Cuong on 5/20/23.
//


import UIKit
import React
import IDCardReader
import AVFoundation

@objc(LivenessViewManager)
class LivenessViewManager: RCTViewManager {
  var player: AVAudioPlayer?
  var action_array :[LivenessAction] = []
  var last_action:LivenessAction?
  var livenessDetector: ARLivenessDetector?
  var baseImage = ""
  
  override func view() -> UIView! {
    let uiView = UIView()
    livenessDetector = ARLivenessDetector(previewView: uiView)
    livenessDetector?.delegate = self
    
    DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
      do {
        try self.livenessDetector?.getVerificationRequiresAndStartSession()
      } catch {}
    }
    
    return uiView
  }
  
  override class func requiresMainQueueSetup() -> Bool {
    false
  }

  func playSound(soundName:String) {
      guard let url = Bundle.main.url(forResource: soundName, withExtension: "mp3") else { return }

      do {
          try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
          try AVAudioSession.sharedInstance().setActive(true)

          /* The following line is required for the player to work on iOS 11. Change the file type accordingly*/
          player = try AVAudioPlayer(contentsOf: url, fileTypeHint: AVFileType.mp3.rawValue)

          /* iOS 10 and earlier require the following line:
          player = try AVAudioPlayer(contentsOf: url, fileTypeHint: AVFileTypeMPEGLayer3) */

          guard let player = player else { return }

          player.play()

      } catch let error {
          print(error.localizedDescription)
      }
  }
}


extension LivenessViewManager: ARLivenessDetectorDelegate {
    func arliveness(liveness: ARLivenessDetector, didFinish verification: FaceValidationResponse, base64Images: [ImageAction], videoPath: String?) {
      var result = verification.toJSON()
      result["images"] = base64Images.map({ $0.image })

        DispatchQueue.main.async {
          EventEmitter.shared.dispatch(event: .onLivenessChecked, body: result)
        }
    }
    
    func arLiveness(liveness: ARLivenessDetector, startLivenessAction action: LivenessAction) {
        var soundName = ""
        if last_action != action{
            last_action = action
            self.action_array.append(action)
            switch action {
            case .eyesLookIn:
                soundName = "center-face"
                break
            case .smile:
                soundName = "smile-face"
                break
            case .wink:
                soundName = "wink-eye"
                break
            case .headPoseUp:
                soundName = "up-face"
                break
            case .headPoseDown:
                soundName = "down-face"
                break
            case .headPoseLeft:
                soundName = "right-face"
                break
            case .headPoseRight:
                soundName = "left-face"
                break
            case .startVerification:
                soundName = "thank-face"
                break
            case .fetchConfig:
                soundName = "center-face"
                break
            }
            self.playSound(soundName: soundName)
        }
        
    }
    
    func arLiveness(liveness: ARLivenessDetector, didFail withError: LivenessError) {
    }
    
}
