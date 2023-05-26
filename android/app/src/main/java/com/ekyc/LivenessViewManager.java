package com.ekyc;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static io.kyc.face.FaceStatus.NONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.ekyc.mlkit.DocType;
import com.ekyc.util.BundleJSONConverter;
import com.ekyc.util.mlkit.CameraSource;
import com.ekyc.util.mlkit.CameraSourcePreview;
import io.kyc.face.camera.GraphicOverlay;
import com.ekyc.mlkit.text.TextRecognitionProcessor;
import com.ekyc.util.PermissionUtil;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import org.jmrtd.lds.icao.MRZInfo;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.kyc.IOKyc;
import io.kyc.face.FaceStatus;
import io.kyc.face.camera.FaceCallback;
import io.kyc.face.camera.FaceDetectorProcessor;
import io.kyc.idcard.IdCardReader;

public class LivenessViewManager extends SimpleViewManager<View> implements TextRecognitionProcessor.ResultListener {
  public static final String REACT_CLASS = "LivenessView";
  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay = null;
  private ThemedReactContext reactContext;
  private boolean sentToReact = false;

  private static final String TAG = "FaceDetectionActivity";
  private TextView mTv;
  private ImageView imgVolume;
  private Boolean soundOn = true;

  private List<FaceStatus> mFaceStateList = new ArrayList<>();
  private int indexFace = -1;
  private int indexState = 0;
  private boolean processingResult = false;
  private JSONObject resultFace;
  private MediaPlayer player;

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
    View livenessLayout = new LivenessCheckLayout(reactContext);
    preview = livenessLayout.findViewById(R.id.preview_view);
    graphicOverlay = livenessLayout.findViewById(R.id.graphic_overlay);
    mTv = livenessLayout.findViewById(R.id.output_face);
    mTv.setText(R.string.straight_face);
    imgVolume = livenessLayout.findViewById(R.id.img_volume);

    createCameraSource();

    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), Environment.DIRECTORY_DOWNLOADS + File.separator + "CardId");
    if (!dir.exists()) {
      dir.mkdir();
    }

    indexFace = -1;
    processingResult = false;


    try {
      preview.start(cameraSource, graphicOverlay);
    } catch (IOException e) {
      cameraSource.release();
    }

    livenessLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
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

    return livenessLayout;
  }

  private void createCameraSource() {
    if (cameraSource == null) {
      cameraSource = new CameraSource(reactContext.getCurrentActivity(), graphicOverlay);
    }
    cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

    cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(reactContext, new FaceCallback() {
      @Override
      public void resultFace(String recordVideo) {
        processingResult = true;
        playSound(R.raw.thank_you);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            new ApiUploadTask().execute();
          }
        }, 3000); // Delay 3s for play thank you sound

      }

      @Override
      public void faceProcess(FaceStatus state) {
        if (state != NONE && indexFace != -1  && !processingResult) {
          setLabelAndSound(state);
        }
      }

      @Override
      public void faceState(List<FaceStatus> status) {
        if (mFaceStateList == null || mFaceStateList.isEmpty()) {
          // copy for not changing state in SDK
          mFaceStateList = new ArrayList<>(status);
          Collections.shuffle(mFaceStateList);
          indexFace = 0;
          indexState = 0;
        }
        mTv.setText(R.string.straight_face);
        playSound(R.raw.center_face);
      }
    }));
  }


  private void setLabelAndSound(FaceStatus faceStatus) {
    switch (faceStatus) {
      case TURN_RIGHT:
        mTv.setText(R.string.right_face);
        playSound(R.raw.right_face);
        break;
      case TURN_LEFT:
        mTv.setText(R.string.left_face);
        playSound(R.raw.left_face);
        break;
      case UP:
        mTv.setText(R.string.up_face);
        playSound(R.raw.up_face);
        break;
      case DOWN:
        mTv.setText(R.string.down_face);
        playSound(R.raw.down_face);
        break;
      case SMILE:
        mTv.setText(R.string.smile_face);
        playSound(R.raw.smile_face);
        break;
      case WINK:
        mTv.setText(R.string.wink_face);
        playSound(R.raw.wink_eye);
        break;
      default:
        break;
    }
  }

  private void requestPermissionForCamera(Activity activity) {
    String[] permissions = {Manifest.permission.CAMERA, MANAGE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE};
    boolean isPermissionGranted = PermissionUtil.hasPermissions(activity, permissions);

    if (!isPermissionGranted) {
      ActivityCompat.requestPermissions(activity, permissions, PermissionUtil.REQUEST_CODE_MULTIPLE_PERMISSIONS);
    } else {
    }
  }


  private void turnSoundOnOrOff() {
    soundOn = !soundOn;
    if (soundOn) {
      imgVolume.setImageResource(R.drawable.volume);
    } else {
      imgVolume.setImageResource(R.drawable.no_volume);
    }
  }

  private void playSound(int soundResId) {
    if (!soundOn) return;
    if (player == null) {
      player = MediaPlayer.create(reactContext, soundResId);
      player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          player.start();
        }
      });
    } else {
      if (player.isPlaying()) {
        player.pause();
      }
      player.reset();
      try {
        player.setDataSource(reactContext, Uri.parse("android.resource://" + reactContext.getPackageName() + "/" + soundResId));
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
      }
      player.prepareAsync();
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


  @SuppressLint("StaticFieldLeak")
  private class ApiUploadTask extends AsyncTask<Void, Void, Exception> {

    @Override
    protected Exception doInBackground(Void... voids) {
      try {
        resultFace = IdCardReader.verifyFaceByCardID();

        if (resultFace != null) {
          if(!sentToReact) {
            sentToReact = true;
            WritableMap map = Arguments.createMap();

            map.putString("test", resultFace.getString("request_id"));
            map.putString("result", resultFace.toString());

            NativeModulePackage.sendEvent("onLivenessChecked", map);
          }
          Log.d(TAG, "Result verify face by card id: " + resultFace.toString());
        }
      } catch (Exception e) {
        if (IOKyc.DEBUG) {
          e.printStackTrace();
        }
      }
      return null;
    }
  }
}


class LivenessCheckLayout extends LinearLayout {
    public LivenessCheckLayout(Context ctx) {
      super(ctx);

      init();
    }
    public void init() {
      inflate(getContext(), R.layout.activity_face, this);
    }
}