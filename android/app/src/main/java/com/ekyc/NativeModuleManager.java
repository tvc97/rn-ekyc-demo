package com.ekyc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.lds.icao.MRZInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.kyc.IOKyc;
import io.kyc.idcard.IOKycErrorCode;
import io.kyc.idcard.IdCardInfo;
import io.kyc.idcard.ProgressCallback;

@ReactModule(name = NativeModuleManager.NAME)
public class NativeModuleManager extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {
    public static final String NAME = "NativeModuleManager";
    private static final String APP_ID = "com.pvcb";
    public static MRZInfo mMRZInfo;
    private IOKyc ioKyc;
    private IdCardInfo mCardInfo;
    private String requestId;
    private ReactApplicationContext ctx;
    private Boolean mScanNFC = false;

    public NativeModuleManager(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);
        reactContext.addLifecycleEventListener(this);

        this.ctx = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void startScanNFC() {
        mScanNFC = true;
        registerNFCTech();
    }

    private String readFile(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("license.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String contents = stringBuilder.toString();
            return contents;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void onActivityResult(Activity activity, int i, int i1, @Nullable Intent intent) {
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mScanNFC && NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag mTag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
            if (mMRZInfo != null) {
                BACKeySpec bacKey = new BACKey(mMRZInfo.getDocumentNumber(), mMRZInfo.getDateOfBirth(), mMRZInfo.getDateOfExpiry());
                new ReadTask(IsoDep.get(mTag), bacKey).execute();
            }
        }
    }

    public static void registerNFCTech() {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.applicationContext);
        if (mNfcAdapter != null) {
            Intent mIntent = new Intent(MainActivity.applicationContext, MainActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.applicationContext, 0, mIntent, PendingIntent.FLAG_MUTABLE);
            String[][] mFilter = new String[][]{new String[]{"android.nfc.tech.IsoDep"}};
            mNfcAdapter.enableForegroundDispatch(MainActivity.mainActivity, mPendingIntent, null, mFilter);
        }
    }

    @Override
    public void onHostResume() {
        registerNFCTech();
    }

    @Override
    public void onHostPause() {
        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(ctx);
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(ctx.getCurrentActivity());
        }
    }

    @Override
    public void onHostDestroy() {

    }

    @SuppressLint("StaticFieldLeak")
    private class ReadTask extends AsyncTask<Void, Void, Exception> {
        private IsoDep mIsoDep;
        private BACKeySpec mBACKey;

        public ReadTask(IsoDep isoDep, BACKeySpec bacKey) {
            this.mIsoDep = isoDep;
            this.mBACKey = bacKey;
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            ioKyc = new IOKyc(APP_ID, readFile(ctx));
            ioKyc.readCardInfo(this.mIsoDep, this.mBACKey, true, true, false, new ProgressCallback() {
                @Override
                public void onProgress(Integer integer) {

                }
            });
            mCardInfo = ioKyc.getCardInfo();
            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e == null && mCardInfo.getErrorCode() == IOKycErrorCode.READER_CARD_SUCCESS) {
                requestId = mCardInfo.getRequestId();

                WritableMap map = Arguments.createMap();

                map.putString("citizenIdentify", mCardInfo.getCitizenIdentifyCard());
                map.putString("oldCitizenIdentify", mCardInfo.getOldIdentifyCard());
                map.putString("fullname",  mCardInfo.getFullName());
                map.putString("dateOfBirth", mCardInfo.getBirthday());
                map.putString("dateOfExpiry", mCardInfo.getDateOfExpiry());
                map.putString("ethnic", mCardInfo.getEthnic());
                map.putString("religion", mCardInfo.getReligion());
                map.putString("gender", mCardInfo.getGender());
                map.putString("nationality",  mCardInfo.getNationality());
                map.putString("placeOfOrigin", mCardInfo.getPlaceOfOrigin());
                map.putString("placeOfResidence", mCardInfo.getPlaceOfResidence());
                map.putString("personalIdentification", mCardInfo.getPersonalIdentification());
                map.putString("dateProvide", mCardInfo.getDateProvide());
                map.putString("fatherName", mCardInfo.getFatherName());
                map.putString("motherName", mCardInfo.getMotherName());
                map.putString("partner_name", mCardInfo.getPartnerName());

                NativeModulePackage.sendEvent("onNFCScanned", map);
            }
        }
    }
}
