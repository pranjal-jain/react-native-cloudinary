import { NativeModules } from "react-native";

const { RNCloudinary } = NativeModules;

const noop = () => {};

export default class UploadRequest {
  filePath = null;
  authToken = null;
  options = null;
  policy = null;
  requestId;

  onStart;
  onProgress;
  onSuccess;
  onError;

  constructor(filePath, authToken, listeners, options, policy) {
    this.setFilePath(filePath)
      .setAuthToken(authToken)
      .setListeners(listeners)
      .setOptions(options)
      .setPolicy(policy);
  }

  setAuthToken(authToken) {
    this.authToken = authToken;
    return this;
  }

  setFilePath(filePath) {
    this.filePath = filePath;
    return this;
  }

  setOptions(options) {
    this.options = options;
    return this;
  }

  setPolicy(policy) {
    this.policy = policy;
    return this;
  }

  setListeners(listeners = {}) {
    const { onStart, onProgress, onSuccess, onError } = listeners;
    this.onStart = onStart || noop;
    this.onProgress = onProgress || noop;
    this.onSuccess = onSuccess || noop;
    this.onError = onError || noop;
    return this;
  }

  clearListeners() {
    this.onStart = noop
    this.onProgress = noop
    this.onSuccess = noop
    this.onError = noop
  }

  getRequestId() {
    return this.requestId;
  }

  dispatch() {
    this.requestId = RNCloudinary.upload(
      this.filePath,
      this.authToken,
      this.options,
      this.policy
    );
    return this;
  }
}
