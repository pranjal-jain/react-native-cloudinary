import {DeviceEventEmitter} from "react-native"
import {NativeModules} from 'react-native';
import UploadRequest from "./UploadRequest"

const {RNCloudinary} = NativeModules;

export default class Uploader {
  static uploadRequests = [];
  static isListening = false;

  constructor() {throw new Error("Uploader cannot be initialized");}

  static init(filePath, authToken, listeners, options, policy) {
    const uploadRequest = new UploadRequest(filePath, authToken, listeners, options, policy);
    Uploader.uploadRequests.push(uploadRequest);
    Uploader.startListening()
    return uploadRequest
  }

  static getUploadRequest(requestId) {
    return Uploader.uploadRequests.find((uploadReq) => uploadReq.getRequestId() === requestId)
  }

  static onStart(requestId) {
    const uploadRequest = Uploader.getUploadRequest(requestId)
    if (!uploadRequest) return
    uploadRequest.onStart()
  }

  static onProgress({requestId, bytes, totalBytes}) {
    const uploadRequest = Uploader.getUploadRequest(requestId)
    if (!uploadRequest) return
    uploadRequest.onProgress(bytes, totalBytes)
  }

  static onSuccess({requestId, ...response}) {
    const uploadRequest = Uploader.getUploadRequest(requestId)
    if (!uploadRequest) return
    Uploader.removeRequest(uploadRequest)
    uploadRequest.onSuccess(response)
  }

  static onError({requestId, code, description}) {
    const uploadRequest = Uploader.getUploadRequest(requestId)
    if (!uploadRequest) return
    uploadRequest.onError(new UploadError(code, description))
  }

  static startListening() {
    if (Uploader.isListening) return
    DeviceEventEmitter.addListener(
      RNCloudinary.UPLOAD_EVENT.START,
      Uploader.onStart
    );
    DeviceEventEmitter.addListener(
      RNCloudinary.UPLOAD_EVENT.PROGRESS,
      Uploader.onProgress
    );
    DeviceEventEmitter.addListener(
      RNCloudinary.UPLOAD_EVENT.SUCCESS,
      Uploader.onSuccess
    );
    DeviceEventEmitter.addListener(
      RNCloudinary.UPLOAD_EVENT.ERROR,
      Uploader.onError
    );
  }

  static removeRequest(uploadRequest) {
    Uploader.uploadRequests.filter((req) => req !== uploadRequest)
    if (!Uploader.uploadRequests.length) Uploader.stopListening()
  }

  static stopListening() {
    DeviceEventEmitter.removeListener(
      RNCloudinary.UPLOAD_EVENT.START,
      Uploader.onStart
    );
    DeviceEventEmitter.removeListener(
      RNCloudinary.UPLOAD_EVENT.PROGRESS,
      Uploader.onProgress
    );
    DeviceEventEmitter.removeListener(
      RNCloudinary.UPLOAD_EVENT.SUCCESS,
      Uploader.onSuccess
    );
    DeviceEventEmitter.removeListener(
      RNCloudinary.UPLOAD_EVENT.ERROR,
      Uploader.onError
    );
  }
}

class UploadError extends Error {
  constructor(code, message) {
    super(message)
    this.code = code;
    this.name = "RNCloudinaryUploadError"
  }

  getCode() {
    return this.code
  }
}