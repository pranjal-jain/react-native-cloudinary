package com.pranjal;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.pranjal.utils.MapUtils;

import java.util.Map;

public class UploadListener implements UploadCallback {

    public static final String START_EVENT = "cloudinary:upload:start";
    public static final String PROGRESS_EVENT = "cloudinary:upload:progress";
    public static final String SUCCESS_EVENT = "cloudinary:upload:success";
    public static final String ERROR_EVENT = "cloudinary:upload:error";
    public static final String RESCHEDULE_EVENT = "cloudinary:upload:reschedule";

    private ReactApplicationContext mReactContext;

    public UploadListener(ReactApplicationContext reactContext) {
        this.mReactContext = reactContext;
    }

    private void emit(String eventKey, Object data) {
        DeviceEventManagerModule.RCTDeviceEventEmitter emitter = mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        emitter.emit(eventKey, data);
    }

    private void sendError(String eventKey, String requestId, ErrorInfo error) {
        WritableMap errorData = Arguments.createMap();
        errorData.putString("requestId", requestId);
        errorData.putInt("code", error.getCode());
        errorData.putString("description", error.getDescription());
        emit(eventKey, errorData);
    }

    @Override
    public void onStart(String requestId) {
        emit(START_EVENT, requestId);
    }
    @Override
    public void onProgress(String requestId, long bytes, long totalBytes) {
        Double progress = (double) bytes/totalBytes;
        WritableMap progressData = Arguments.createMap();
        progressData.putString("requestId", requestId);
        progressData.putDouble("bytes", bytes);
        progressData.putDouble("totalBytes", totalBytes);
        emit(PROGRESS_EVENT, progressData);
    }
    @Override
    public void onSuccess(String requestId, Map resultData) {
        WritableMap successData = MapUtils.toWritableMap(resultData);
        successData.putString("requestId", requestId);
        emit(SUCCESS_EVENT, successData);
    }
    @Override
    public void onError(String requestId, ErrorInfo error) {
        sendError(ERROR_EVENT, requestId, error);
    }
    @Override
    public void onReschedule(String requestId, ErrorInfo error) {
        sendError(RESCHEDULE_EVENT, requestId, error);
    }
}
