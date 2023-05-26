package com.ekyc;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.ekyc.NativeModuleManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import com.ekyc.mlkit.camera.CameraSource;
import com.ekyc.mlkit.camera.CameraSourcePreview;
import com.ekyc.mlkit.camera.GraphicOverlay;
import com.ekyc.mlkit.text.*;
import com.ekyc.mlkit.*;
import com.ekyc.util.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jmrtd.lds.icao.MRZInfo;

import java.io.IOException;

public class MRZViewManager extends SimpleViewManager<View> implements TextRecognitionProcessor.ResultListener {
  public static final String REACT_CLASS = "MRZView";
  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay = null;
  private DocType docType = DocType.ID_CARD;
  private ThemedReactContext reactContext;
  private boolean sentToReact = false;

  @Override
  @NonNull
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  @NonNull
  public View createViewInstance(ThemedReactContext reactContext) {
    this.reactContext = reactContext;
    this.requestPermissionForCamera(reactContext.getCurrentActivity());

    sentToReact = false;
    View scanLayout = new ScanLayout(reactContext);
    preview = scanLayout.findViewById(R.id.camera_source_preview);
    graphicOverlay = scanLayout.findViewById(R.id.graphics_overlay);
    createCameraSource();

    try {
      preview.start(cameraSource);
    } catch (IOException e) {
      cameraSource.release();
    }

    scanLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
      @Override
      public void onViewAttachedToWindow(@NonNull View view) {

      }

      @Override
      public void onViewDetachedFromWindow(@NonNull View view) {
        preview.stop();
        cameraSource.release();
        cameraSource = null;
      }
    });

    return scanLayout;
  }

  private void createCameraSource() {
    if (cameraSource == null) {
      cameraSource = new CameraSource(this.reactContext.getCurrentActivity(), graphicOverlay);
      cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
    }

    cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(docType, this));
  }

  private void requestPermissionForCamera(Activity activity) {
    String[] permissions = {Manifest.permission.CAMERA, MANAGE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};
    boolean isPermissionGranted = PermissionUtil.hasPermissions(activity, permissions);

    if (!isPermissionGranted) {
      ActivityCompat.requestPermissions(activity, permissions, PermissionUtil.REQUEST_CODE_MULTIPLE_PERMISSIONS);
    } else {
    }
  }

  @Override
  public void onSuccess(MRZInfo mrzInfo) {
    if(!sentToReact) {
      sentToReact = true;
      NativeModuleManager.mMRZInfo = mrzInfo;

      WritableMap params = Arguments.createMap();

      params.putString("documentNumber", mrzInfo.getDocumentNumber());
      params.putString("birthdate", mrzInfo.getDateOfBirth());
      params.putString("expiryDate", mrzInfo.getDateOfExpiry());

      NativeModulePackage.sendEvent("onMRZScanned", params);

      cameraSource.stop();
    }
  }

  @Override
  public void onError(Exception exp) {
  }
}


class ScanLayout extends LinearLayout {
    public ScanLayout(Context ctx) {
      super(ctx);

      init();
    }
    public void init() {
      inflate(getContext(), R.layout.activity_capture, this);
    }
}