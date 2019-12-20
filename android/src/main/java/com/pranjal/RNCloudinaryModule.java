
package com.pranjal;

import android.net.Uri;

import com.cloudinary.Configuration;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.SignatureProvider;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.pranjal.utils.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class RNCloudinaryModule extends ReactContextBaseJavaModule {
    private UploadListener mUploadListener;

    RNCloudinaryModule(ReactApplicationContext reactContext, SignatureProvider signatureProvider) {
        super(reactContext);
        init(signatureProvider);
    }

    public void init(SignatureProvider signatureProvider) {
        mUploadListener = new UploadListener(getReactApplicationContext());
        try {
            MediaManager.init(getReactApplicationContext(), signatureProvider);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "RNCloudinary";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<String, Object>() {{
            put("UPLOAD_EVENT", new HashMap<String, String>() {{
                put("START", UploadListener.START_EVENT);
                put("PROGRESS", UploadListener.PROGRESS_EVENT);
                put("SUCCESS", UploadListener.SUCCESS_EVENT);
                put("ERROR", UploadListener.ERROR_EVENT);
                put("RESCHEDULE", UploadListener.RESCHEDULE_EVENT);
            }});
        }};
        return constants;
    }

    @ReactMethod
    public void upload(String path, ReadableMap options, ReadableMap uploadPolicy, Promise promise) {
        try {
            Uri uri = Uri.parse(path);
            String cloudinaryUrl = options.getString("cloudinaryUrl");
            if (null != cloudinaryUrl) {
                Map<String, Object> cloudinaryConfig = Configuration.from(options.getString("cloudinaryUrl")).asMap();
                MediaManager.get().getCloudinary().config.update(cloudinaryConfig);
            }
            UploadRequest uploadRequest = MediaManager.get().upload(uri);
            if (uploadPolicy != null) {
                uploadRequest = uploadRequest.policy(getUploadPolicy(uploadPolicy));
            }
            if (options != null) {
                uploadRequest = uploadRequest.options(MapUtils.toMap(options));
            }
            String requestId = uploadRequest.callback(mUploadListener)
                    .dispatch();
            promise.resolve(requestId);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private UploadPolicy getUploadPolicy(ReadableMap policyMap) {
        return new UploadPolicy.Builder()
                .networkPolicy(UploadPolicy.NetworkType.valueOf(policyMap.getString("networkType")))
                .requiresCharging(policyMap.getBoolean("requiresCharging"))
                .maxRetries(policyMap.getInt("maxRetries"))
                .backoffCriteria((long) policyMap.getDouble("backoffMillis"), UploadPolicy.BackoffPolicy.valueOf(policyMap.getString("backoffPolicy")))
                .build();
    }
}