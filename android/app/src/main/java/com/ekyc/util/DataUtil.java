package com.ekyc.util;

import android.graphics.Bitmap;
import android.util.Base64;
import io.kyc.IOKyc;
import io.kyc.face.FaceStatus;
import io.kyc.face.camera.ImageAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataUtil {
    private static boolean livelinessCheckSuccessfully = false;
    private static JSONObject livenessResult = null;
    private static List<ImageAction> lstPhotoInIdCard = null;
    private static List<ImageAction> lstPhotoInLivenessCheck = null;
    public static final String LIVENESS_CHECK_RESULT_SCORE = "matching_score";
    public static final String LIVENESS_CHECK_RESULT_THRESHOLD = "matching_threshold";
    public static final String LIVENESS_CHECK_RESULT_STATUS = "liveness_check_status";
    public static boolean IsLivelinessCheckSuccessfully() {
        return livelinessCheckSuccessfully;
    }
    public static void ConvertAndSaveLivenessResult(JSONObject livenessResultOrigin) {
        livelinessCheckSuccessfully = false;
        try {
            livenessResult = new JSONObject();
            if (lstPhotoInLivenessCheck == null) {
                lstPhotoInLivenessCheck = new ArrayList<>();
            }
            if (lstPhotoInLivenessCheck.size() > 0) {
                lstPhotoInLivenessCheck.clear();
            }
            if (livenessResultOrigin.has("success")) {
                livenessResult.put(LIVENESS_CHECK_RESULT_STATUS, livenessResultOrigin.getBoolean("success"));
            } else {
                livenessResult.put(LIVENESS_CHECK_RESULT_STATUS, false);
            }
            Double faceMatchingScore = 0.0;
            Double threshHold = 0.0;
            if (livenessResultOrigin.has("data")) {
                JSONObject data = livenessResultOrigin.getJSONObject("data");
                if (data.has("faceMatchingScore")) {
                    faceMatchingScore = data.getDouble("faceMatchingScore");
                }
                if (data.has("sim")) {
                    threshHold = data.getDouble("sim");
                }
            }
            livenessResult.put(LIVENESS_CHECK_RESULT_SCORE, faceMatchingScore);
            livenessResult.put(LIVENESS_CHECK_RESULT_THRESHOLD, threshHold);
            if (livenessResultOrigin.has("image_action")) {
                JSONArray jArray = livenessResultOrigin.getJSONArray("image_action");
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jo = jArray.getJSONObject(i);
                    if (jo.has("action") && jo.has("image")) {
                        lstPhotoInLivenessCheck.add(new ImageAction(
                                FaceStatus.valueOf(jo.getString("action")),
                                jo.getString("image"))
                        );

                    }
                }
            }
        } catch (Exception e) {
            if (IOKyc.DEBUG) {
                e.printStackTrace();
            }
            return;
        }
        livelinessCheckSuccessfully = true;
    }

    public static void SaveCardIdPhoto(Bitmap bm) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Data = Base64.encodeToString(byteArray, Base64.DEFAULT);
        if (lstPhotoInIdCard == null) {
            lstPhotoInIdCard = new ArrayList<ImageAction>();
        }
        if (lstPhotoInIdCard.size() > 0) {
            lstPhotoInIdCard.clear();
        }
        lstPhotoInIdCard.add(new ImageAction(FaceStatus.NONE, base64Data));
    }

    public static JSONObject GetLivenessResult() {
        return livenessResult;
    }

    public static List<ImageAction> GetListPhotoInLivenessCheck() {
        return lstPhotoInLivenessCheck;
    }

    public static List<ImageAction> GetListPhotoInIdCard() {
        return lstPhotoInIdCard;
    }

    public static void ClearData() {
        livelinessCheckSuccessfully = false;
        livenessResult = null;
        lstPhotoInIdCard = null;
        lstPhotoInLivenessCheck = null;
    }

}
