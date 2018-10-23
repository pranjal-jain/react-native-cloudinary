
package com.pranjal;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cloudinary.android.signed.SignatureProvider;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;

public class RNCloudinaryPackage implements ReactPackage {
    private SignatureProvider mSignatureProvider;

    public RNCloudinaryPackage(SignatureProvider signatureProvider) {
        this.mSignatureProvider = signatureProvider;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new RNCloudinaryModule(reactContext, mSignatureProvider));
    }

    // Deprecated from RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}