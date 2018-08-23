package com.pranjal;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

public class UploadListener implements UploadCallback {

    public static final String START_EVENT_PREFIX = "cloudinary:upload:start:";
    public static final String PROGRESS_EVENT_PREFIX = "cloudinary:upload:progress:";
    public static final String SUCCESS_EVENT_PREFIX = "cloudinary:upload:success:";
    public static final String ERROR_EVENT_PREFIX = "cloudinary:upload:error:";
    public static final String RESCHEDULE_EVENT_PREFIX = "cloudinary:upload:reschedule:";

    private ReactApplicationContext mReactContext;

    public UploadListener(ReactApplicationContext reactContext) {
        this.mReactContext = reactContext;
    }

    private void emit(String message, Object data) {
        DeviceEventManagerModule.RCTDeviceEventEmitter emitter = mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        emitter.emit(message, data);
    }

    private void sendError(String eventPrefix, String requestId, ErrorInfo error) {
        WritableMap errorData = Arguments.createMap();
        errorData.putInt("code", error.getCode());
        errorData.putString("description", error.getDescription());
        emit(eventPrefix.concat(requestId), errorData);
    }

    @Override
    public void onStart(String requestId) {
        emit(START_EVENT_PREFIX.concat(requestId), null);
    }
    @Override
    public void onProgress(String requestId, long bytes, long totalBytes) {
        Double progress = (double) bytes/totalBytes;
        WritableMap progressData = Arguments.createMap();
        progressData.putDouble("bytes", bytes);
        progressData.putDouble("totalBytes", totalBytes);
        emit(PROGRESS_EVENT_PREFIX.concat(requestId), progressData);
    }
    @Override
    public void onSuccess(String requestId, Map resultData) {
        WritableMap successData = Utils.recursivelyConstructWritableMap(resultData);
        emit(SUCCESS_EVENT_PREFIX + requestId, successData);
    }
    @Override
    public void onError(String requestId, ErrorInfo error) {
        sendError(ERROR_EVENT_PREFIX, requestId, error);
    }
    @Override
    public void onReschedule(String requestId, ErrorInfo error) {
        sendError(RESCHEDULE_EVENT_PREFIX, requestId, error);
    }
}
