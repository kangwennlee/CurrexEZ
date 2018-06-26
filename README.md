# [CurrexEZ](https://github.com/kangwennlee/CurrexEZ)

![CurrexEZ Logo](https://github.com/kangwennlee/CurrexEZ/blob/master/app/src/main/res/drawable/logo.png)

CurrexEZ is an app developed by Tunku Abdul Rahman University Students for SAS Fintech Challenge 2017-2018.<br>
Our idea is to make currency exchange easier and convenient.

## Quick Start

The whole project is an **Android Studio** project file.<br>
Download the **master branch** to run the project.<br>
Please make sure you have the **latest** Android Studio installed on your computer.<br>
We code the application using Java.

## Procedures to replicate this project
### Prerequisite
* Valid Google account (for firebase)
* Latest Android Studio

1. Install Android Studio (For more info on how to install Android Studio, please refer [here](https://developer.android.com/studio/install)).
2. Enter Firebase [console](https://console.firebase.google.com/)
3. Create a Firebase project and add Firebase to the project. (For more info on how to create a Firebase project, please refer [here](https://firebase.google.com/docs/android/setup)).
4. The app's package name will be the applicationId in build.gradle file (in this case, it's com.example.kangwenn.currexez)
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
