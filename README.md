# [CurrexEZ](https://github.com/kangwennlee/CurrexEZ)

![CurrexEZ Logo](https://github.com/kangwennlee/CurrexEZ/blob/master/app/src/main/res/drawable/logo.png)

CurrexEZ is an app developed by Tunku Abdul Rahman University Students for SAS Fintech Challenge 2017-2018.<br>
Our idea is to make currency exchange easier and convenient.

## Quick Start

The whole project is an **Android Studio** project file.<br>
Download the **master branch** to run the project.<br>
Please make sure you have the **latest** Android Studio installed on your computer.<br>
We code the application using Java.

### Prerequisite
* Valid Google account (for firebase)
* Latest Android Studio

## Steps to replicate this project

1. Download this project onto your Desktop
2. Install Android Studio (For more info on how to install Android Studio, please refer [here](https://developer.android.com/studio/install)).
3. Create a Firebase project
* Create a Firebase project in the [Firebase console](https://console.firebase.google.com/), if you don't already have one. Click **Add project**. If you already have an existing Google project associated with your mobile app, select it from the Project name drop down menu. Otherwise, enter a **project name** to create a new project.
* Optional: Edit your **Project ID**. Your project is given a unique ID automatically, and it's used in publicly visible Firebase features such as database URLs and your Firebase Hosting subdomain. You can change it now if you want to use a specific subdomain.
* Follow the remaining setup steps and click **Create project** (or **Add Firebase** if you're using an existing project) to begin provisioning resources for your project. This typically takes a few minutes. When the process completes, you'll be taken to the project overview.
4. Now that you have a project, you can add your Android app to it:
* Click **Add Firebase to your Android app** and follow the setup steps. If you're importing an existing Google project, this may happen automatically and you can just [download the config file](http://support.google.com/firebase/answer/7015592).
* When prompted, enter your app's package name. It's important to enter the package name your app is using; this can only be set when you add an app to your Firebase project. Your package name is generally the applicationId in your app-level build.gradle file. In this case, it's **com.example.kangwenn.currexez**.
* During the process, you'll download a ```google-services.json``` file. You can download this file again at any time. Follow the instruction given. Switch to the Project view in Android Studio to see your project root directory. Move the google-service.json file you just downloaded into the Android app module root directory.
* The firebase SDK is already added into the project, so this step can be skipped.
* After you add the initialization code, run your app to send verification to the Firebase console that you've successfully installed Firebase.
* For more info on how to create a Firebase project, please refer [here](https://firebase.google.com/docs/android/setup).
5. 

## Functions
* View latest currency rates
* Rates calculator
* Sell currency
* Purchase currency
* Sell Ringgit
* Purchase Ringgit
* Fingerprint authentication
* Browse flight
* Browse hotels
* Nearby attractions
* QR Scanner

## API/Library used
### Firebase
We're using [Firebase](https://firebase.google.com/) to deploy our backend infrastructure. We are using:

* [Firebase UI](https://github.com/firebase/FirebaseUI-Android)
* Firebase Authentication (login and logout using Google/Email)
* Firebase Real-time Database (store user's transaction data, user profile)
* Firebase Crashlytics (view app crashes, performance)
* Firebase Storage (store user's IC photos)
* Firebase Analytics (analyze user's data in **REAL-TIME**)
* Firebase ML Kit (text recognition to identify words in Identity Card)

### Fabrics
We're also using [Fabric](https://fabric.io/kits) to analyse user's app data in **REAL-TIME**.
The **Kit** we are using are:

* Answers (for **REAL-TIME** analytics)
* Beta (for internal app publishing and testing)

### Google Cloud

* Google Maps API (for nearby attractions and location positioning)

### Third Party Open-sourced Component

* [Butterknife](https://github.com/JakeWharton/butterknife) (simplify android code)
* [Fixer](https://github.com/fixerAPI/fixer) (Currency rates API)
* [card.io](https://github.com/card-io/card.io-Android-SDK) (Scan credit card using camera)
* [Volley](https://github.com/google/volley) (Library to handle JSON/Image file)
* [FingerprintManager](https://github.com/JesusM/FingerprintManager) (Library for fingerprint authentication)

## Automated kiosk
We're using a raspberry pi with a webcam, 1 motor, GroovePi, 2 LEDs, 1 buzzer, 1 dot-matrix screen installed, running a script with Zxing QR reader app that reads line by line. <br>
The motor will move when there's more than 4 lines in the QR. <br>

:octocat:
