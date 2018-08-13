
package com.pranjal;

import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RNCloudinaryModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private final static String CLOUDINARY_SIGNATURE_ENDPOINT_KEY = "CLOUDINARY_SIGNATURE_ENDPOINT";
    private static String CLOUDINARY_SIGNATURE_ENDPOINT;

    public RNCloudinaryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        CLOUDINARY_SIGNATURE_ENDPOINT = Utils.getMetaFromContext(reactContext, CLOUDINARY_SIGNATURE_ENDPOINT_KEY);
        MediaManager.init(reactContext, new SignatureProvider() {
            @Override
            public Signature provideSignature(Map options) {
                OkHttpClient client = new OkHttpClient();
                Request.Builder builder = new Request.Builder();
                builder.url(CLOUDINARY_SIGNATURE_ENDPOINT);
                Request request = builder.build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject responseJSON = new JSONObject(response.body().string());
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
                return null;
            }
        });
    }

    @Override
    public String getName() {
        return "RNCloudinary";
    }

    @ReactMethod
    public void upload(String path, ReadableMap options, ReadableMap policyMap, Promise promise) {
        try {
            Uri uri = Uri.parse(path);
            String requestId = MediaManager
                    .get()
                    .upload(uri)
                    .policy(getUploadPolicy(policyMap))
                    .options(Utils.recursivelyDeconstructReadableMap(options))
                    .callback(new UploadListener(this.reactContext))
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