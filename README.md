
# react-native-cloudinary

## Getting started

`$ npm install react-native-cloudinary --save`

### Mostly automatic installation

`$ react-native link react-native-cloudinary`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.pranjal.RNCloudinaryPackage;` to the imports at the top of the file
  - Add `new RNCloudinaryPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-cloudinary'
  	project(':react-native-cloudinary').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-cloudinary/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-cloudinary')
  	```


## Usage
```javascript
import RNCloudinary from 'react-native-cloudinary';

try {
 const uploadRequest = await RNCloudinary
  .init(filePath)
  .setOptions(options) // Set cloudinary options (Optional)
  .setPolicy(policy) // Set upload policy options (Optional)
  .setListeners({
    onStart: () => { // Do something on upload start },
    onProgress: (bytes, totalBytes) => { // Show upload progress },
    onSuccess: () => { // Do something on success },
    onError: (e) => { // Handle upload time error }
  })
  .dispatch()
} catch (e) {
  // Handle init error
}




uploadRequest.clearListeners() // Clears listeners
```
  