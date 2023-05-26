package com.ekyc;

import androidx.annotation.Nullable;

import com.ekyc.NativeModuleManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.ViewManager;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NativeModulePackage implements ReactPackage {
  public static ReactApplicationContext context;

  @Override
  public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    NativeModulePackage.context = reactContext;

    List<NativeModule> modules = new ArrayList<>();
    modules.add(new ReactNativeEventEmitter(reactContext));
    modules.add(new NativeModuleManager(reactContext));

    return modules;
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    List<ViewManager> nativeViews = new ArrayList<>();
    nativeViews.add(new MRZViewManager());
    nativeViews.add(new LivenessViewManager());

    return nativeViews;
  }

  public static void sendEvent(String eventName,
                         @Nullable WritableMap params) {
    NativeModulePackage.context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }
}
