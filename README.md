# [CurrexEZ](https://github.com/kangwennlee/CurrexEZ)

CurrexEZ is an app developed by Tunku Abdul Rahman University Students for SAS Fintech Challenge 2017-2018.<br>
Our idea is to make currency exchange easier and convenient.

## Quick Start

The whole project is an **Android Studio** project file.<br>
Download the **master branch** to run the project.<br>
Please make sure you have the **latest** Android Studio installed on your computer.<br>
We code the application using Java.

## User Requirements
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
