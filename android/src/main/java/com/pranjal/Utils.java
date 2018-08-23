package com.pranjal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {
    public static String getMetaFromContext(Context context, String key) {
        String url = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (info != null && info.metaData != null) {
                url = (String) info.metaData.get(key);
            }
        } catch (NameNotFoundException e) {
            // No metadata found
        }
        return url;
    }

    public static Map<String, Object> recursivelyDeconstructReadableMap(ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        Map<String, Object> deconstructedMap = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    deconstructedMap.put(key, null);
                    break;
                case Boolean:
                    deconstructedMap.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    deconstructedMap.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    deconstructedMap.put(key, readableMap.getString(key));
                    break;
                case Map:
                    deconstructedMap.put(key, recursivelyDeconstructReadableMap(readableMap.getMap(key)));
                    break;
                case Array:
                    deconstructedMap.put(key, recursivelyDeconstructReadableArray(readableMap.getArray(key)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
            }

        }
        return deconstructedMap;
    }

    public static List<Object> recursivelyDeconstructReadableArray(ReadableArray readableArray) {
        List<Object> deconstructedList = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch(indexType) {
                case Null:
                    deconstructedList.add(i, null);
                    break;
                case Boolean:
                    deconstructedList.add(i, readableArray.getBoolean(i));
                    break;
                case Number:
                    deconstructedList.add(i, readableArray.getDouble(i));
                    break;
                case String:
                    deconstructedList.add(i, readableArray.getString(i));
                    break;
                case Map:
                    deconstructedList.add(i, recursivelyDeconstructReadableMap(readableArray.getMap(i)));
                    break;
                case Array:
                    deconstructedList.add(i, recursivelyDeconstructReadableArray(readableArray.getArray(i)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object at index " + i + ".");
            }
        }
        return deconstructedList;
    }

    static WritableMap recursivelyConstructWritableMap(Map<String, Object> map) {
        WritableMap data = Arguments.createMap();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = map.get(key);
            switch (value.getClass().getName()) {
                case "java.lang.Boolean":
                    data.putBoolean(key, (Boolean) value);
                    break;
                case "java.lang.Integer":
                    data.putInt(key, (Integer) value);
                    break;
                case "java.lang.Double":
                    data.putDouble(key, (Double) value);
                    break;
                case "java.lang.String":
                    data.putString(key, (String) value);
                    break;
                case "java.util.Map":
                    data.putMap(key, recursivelyConstructWritableMap((Map<String, Object>) value));
                    break;
                case "java.util.ArrayList":
                    data.putArray(key, recursivelyConstructWritableArray((ArrayList<Object>) value));
                    break;
                default:
                    throw new IllegalArgumentException("Failed to convert HashMap with unrecognized value of class name: " + value.getClass().getName());
            }
        }
        return data;
    }

    public static WritableArray recursivelyConstructWritableArray(ArrayList<Object> list) {
        WritableArray writableList = Arguments.createArray();
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            String type = value.getClass().getName();
            switch(type) {
                case "java.lang.Boolean":
                    writableList.pushBoolean((Boolean) value);
                    break;
                case "java.lang.Integer":
                    writableList.pushInt((Integer) value);
                    break;
                case "java.lang.Double":
                    writableList.pushDouble((Double) value);
                    break;
                case "java.lang.String":
                    writableList.pushString((String) value);
                    break;
                case "java.util.Map":
                    writableList.pushMap(recursivelyConstructWritableMap((Map<String, Object>) value));
                    break;
                case "java.util.ArrayList":
                    writableList.pushArray(recursivelyConstructWritableArray((ArrayList<Object>) value));
                    break;
                default:
                    throw new IllegalArgumentException("Failed to convert ArrayList with unrecognized value of class name: " + value.getClass().getName());
            }
        }
        return writableList;
    }
}
