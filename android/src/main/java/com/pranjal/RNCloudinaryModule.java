
package com.pranjal;

import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RNCloudinaryModule extends ReactContextBaseJavaModule {
    private UploadListener mUploadListener;
    private String authToken;

    RNCloudinaryModule(ReactApplicationContext reactContext, URL signatureUrl) {
        super(reactContext);
        init(signatureUrl);
    }

    public void init(URL signatureUrl) {
        RNCloudinarySignatureProvider signatureProvider = new RNCloudinarySignatureProvider(signatureUrl);
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
    public void upload(String path, String authToken, ReadableMap options, ReadableMap policyMap, Promise promise) {
        try {
            this.authToken = authToken;
            Uri uri = Uri.parse(path);
            UploadRequest uploadRequest = MediaManager
                    .get()
                    .upload(uri);
            if (policyMap != null) {
                uploadRequest = uploadRequest.policy(getUploadPolicy(policyMap));
            }
            if (options != null) {
                uploadRequest = uploadRequest.options(Utils.recursivelyDeconstructReadableMap(options));
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

    private class RNCloudinarySignatureProvider implements SignatureProvider {
        private URL mSignatureUrl;
        RNCloudinarySignatureProvider(URL signatureUrl) {
            this.mSignatureUrl = signatureUrl;
        }
        @Override
        public Signature provideSignature(Map options) {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            builder.url(mSignatureUrl);
            builder.addHeader("auth_token", authToken);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject responseJSON = new JSONObject(response.body().string()).getJSONObject("response");
                String signatureString = responseJSON.getString("signature");
                String apiKey = responseJSON.getString("api_key");
                long timestamp = responseJSON.getLong("timestamp");
                return new Signature(signatureString, apiKey, timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String getName() {
            return "RNCloudinarySignatureProvider";
        }
    }
}