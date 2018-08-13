package com.pranjal;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;

public class UploadListener implements UploadCallback {

    public final String START_EVENT_PREFIX = "cloudinary:upload:start:";
    public final String PROGRESS_EVENT_PREFIX = "cloudinary:upload:progress:";
    public final String SUCCESS_EVENT_PREFIX = "cloudinary:upload:success:";
    public final String ERROR_EVENT_PREFIX = "cloudinary:upload:error:";
    public final String RESCHEDULE_EVENT_PREFIX = "cloudinary:upload:reschedule:";

    private DeviceEventManagerModule.RCTDeviceEventEmitter emitter;

    public UploadListener(ReactApplicationContext reactContext) {
        this.emitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }
    @Override
    public void onStart(String requestId) {
        this.emitter.emit(START_EVENT_PREFIX.concat(requestId), null);
    }
    @Override
    public void onProgress(String requestId, long bytes, long totalBytes) {
        Double progress = (double) bytes/totalBytes;
        WritableMap progressData = Arguments.createMap();
        progressData.putDouble("bytes", bytes);
        progressData.putDouble("totalBytes", totalBytes);
        this.emitter.emit(PROGRESS_EVENT_PREFIX.concat(requestId), progressData);
    }
    @Override
    public void onSuccess(String requestId, Map resultData) {
        this.emitter.emit(SUCCESS_EVENT_PREFIX + requestId, resultData);
    }
    @Override
    public void onError(String requestId, ErrorInfo error) {
        this.sendError(ERROR_EVENT_PREFIX, requestId, error);
    }
    @Override
    public void onReschedule(String requestId, ErrorInfo error) {
        this.sendError(RESCHEDULE_EVENT_PREFIX, requestId, error);
    }

    private void sendError(String eventPrefix, String requestId, ErrorInfo error) {
        WritableMap errorData = Arguments.createMap();
        errorData.putInt("code", error.getCode());
        errorData.putString("description", error.getDescription());
        this.emitter.emit(eventPrefix.concat(requestId), errorData);
    }
}
