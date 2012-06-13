The MiniBus Project
===================

MiniBus is an Android application which displays bus arrival information for Cambridge and the local region. It was originally developed at the University of Cambridge by David Tattersall as part of a ten-week [summer project](http://www.cl.cam.ac.uk/research/dtg/summer/). This project is licenced under [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0). Further details are available on the [project website](http://www.cl.cam.ac.uk/research/dtg/android/minibus/).

MiniBus uses Git for source code management and Maven can be used for build management. The instructions below ignore the Maven build configuration and set up dependencies manually.

The source code for MiniBus is available from our public repository. To build minibus itself, you will need to install two repositories, "TIMEBase" and "minibus". TIMEBase provides a common framework which is used in various applications; minibus provides the end-user Android application.

The respositories are available here:

 * https://github.com/ucam-cl-dtg/minibus.git
 * https://github.com/ucam-cl-dtg/timebase.git 

Instructions on installing and building MiniBus using Eclipse.
--------------------------------------------------------------

1. Install the Java JDK 1.6 or later on your workstation.

2. Download and unzip Eclipse. At the time of writing Indigo SR2, EE Edition, was used.

3. Install the Android SDK into Eclipse along with the associated plugin by following the [developer installation instructions](http://developer.android.com/sdk/installing.html).

4. In Eclipse go to Window -> Android SDK Manager. Install Android 1.6 (API 4) and possibly other API levels too if you want to test functionality on more recent devices. Note: Make sure you select "Google APIs" as well as the SDK platform itself since MiniBus uses the Google Maps API.

5. In Eclipse, go to Help -> Install New Software. Select "Add" and add the [EGit repository](http://download.eclipse.org/egit/updates).

6. Select "Eclipse Git Team Provider" and install.
   
7. Restart Eclipse

8. In Eclipse, go to File -> Import... -> Git -> Projects from Git. Select URI as the method of accessing the repository and enter: https://github.com/ucam-cl-dtg/timebase.git 

9. Select "import existing project" and click through to finish.

10. In Eclipse, go to File -> Import... -> Git -> Projects from Git. Select URI as the method of accessing the repository and enter: https://github.com/ucam-cl-dtg/minibus.git

11. Select "import existing project" and click through to finish.

12. Associate the TIMEBase project with the minibus project by right-clicking on the minibus project in the Package Explorer. Select Properties from the pop-up menu, then select Java Build Path in the dialog box which appears. In the Projects tab inside the dialog box, add TIMEBase as a dependent project. In the Libraries tab, select "Add class folder ... " and add TIMEBase/target/classes. In the Order and Export tab, enable the tickbox next to TIMEBase to tell the compiler to export TIMEBase as part of the minibus application.

13. Associate the JUnit 4 library with TIMEBase by right-clicking on the project, selecting Properties -> Java Build Path -> Libraries (it's a tab) -> Add Library. The select JUnit and choose JUnit 4.

14. Select Window -> AVD Manager and create a virtual device which supports Android 1.6 (API 4), or plug a device in to your workstation via a USB cable and ensure that "USB debugging" is enabled. You will not be able to test MiniBus on a physical device which already has a copy of MiniBus installed from Google Play (formerly Android Market).

15. Right-click on the minibus project, select Run -> Android Application. Select the virutal device or handset on which to run MiniBus (if offered the choice).

16. MiniBus should now load. It will not be able to display Map data or bus information as the source repository does not contain the API keys for these services. You should create your own keys for these services. **If you do add your own keys to the source code, and you want to make contributions to our repository, do not commit your keys!**


Instructions on installing and building MiniBus using Maven.
------------------------------------------------------------
1. Install the Android SDK using the [developer installation instructions](http://developer.android.com/sdk/installing.html)
2. Use the [android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer) to install the google maps jars to your local maven repository (you want  Android 1.6 (API 4) and Google APIs.
3. To have minibus build correctly with maven you will need to specify the android.keystores property to be a directory containing debug.store (for non release builds) and minibus.keystore (for release builds).
