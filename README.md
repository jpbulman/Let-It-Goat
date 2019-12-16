# Let It Goat
This is an Android app that targets WPI students to resell their used possessions (such as textbooks, furniture, etc.).

## Building and running

The app uses the standard Android project format and requires Android Studio and Gradle to build. Additionally, the app supports a minimum 
SDK version supported is 24 (Android Nougat), and required Google Play services be installed. To ensure that the required dependencies are 
installed, open Android Studio, click the Tools tab, and select SDK manager. From the list of SDK platforms, select and install an APK version of
24 or higher (version 29 was primarily used for testing). Then, in the same window, open the SDK tools tab and install Google Play services.
Finally, you will have to run gradle to install all library dependencies for the app. Close out of the window and locate the Grade tab in the IDE 
(it is usually on the bottom or right edge of the Android Studio window). Click the "let it goat" task and hit the "Execute Gradle build" button.
Now, to build the app, simply hit the run button on upper right corner of Android Studio.


## User authentication
![node logo](https://d2eip9sf3oo6c2.cloudfront.net/tags/images/000/000/256/square_256/nodejslogo.png)

Every person who uses this app must have a valid WPI login. To validate identity, we are hosting an express server using [glitch](https://glitch.com/).

## Database
![firebase logo](https://huang-an-sheng.gallerycdn.vsassets.io/extensions/huang-an-sheng/firebase-web-app-snippets/1.1.6/1563878238121/Microsoft.VisualStudio.Services.Icons.Default)

For our database, we decided to use a Firestore instance.

