# TruthChain - Android Client (Source of Trust)

**TruthChain** is a content authenticity platform designed to combat deepfakes and AI-generated misinformation. This Android application acts as the **"Source of Trust,"** capturing images and cryptographically signing them using hardware-backed security before they ever leave the device.

## Key Features

* **Hardware-Backed Security:** Utilizes the **Android Keystore System** to generate asymmetric key pairs (EC/RSA) inside the device's Trusted Execution Environment (TEE). The private key *never* leaves the hardware.
* **Content Authenticity:** Captures photos using **CameraX** and immediately generates a digital signature of the image data.
* **Tamper Proofing:** If a single pixel of the image is altered after capture, the signature verification will fail.
* **Local Buffer:** Images are temporarily cached locally to ensure no data loss during the signing process.
* **Seamless Upload:** Automatically uploads the image, signature, and public key to the TruthChain Backend for verification.

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Camera:** CameraX API
* **Security:** Android Keystore System (Hardware Security Module integration)
* **Networking:** Retrofit & OkHttp
* **Offline Caching:** Images and metadata are stored locally first, ensuring no data is lost during capture.
* **Concurrency:** Kotlin Coroutines & Flow

## Architecture

The app follows a Clean Architecture approach with MVVM:

1.  **Capture:** User takes a photo via the in-app camera.
2.  **Sign:** The app calculates a SHA-256 hash of the image bytes.
3.  **Encrypt:** The hash is signed using the hardware-backed Private Key.
4.  **Transmit:** The Image + Signature + Public Key are sent to the Spring Boot backend.
5.  **Sync:** WorkManager detects network availability and sends the payload to the
   
## Getting Started

### Prerequisites
* Android Studio Ladybug (or newer)
* Android Device (Minimum SDK 26 recommended for full Keystore support)

### Installation

1.  Clone the repository:
2.  Open the project in **Android Studio**.
3.  Sync Gradle files.
4.  Connect a physical device (Emulators may not support all hardware-backed keystore features).
5.  Run the application.

## How it Works (Under the Hood)

```kotlin
// Simplified Logic
val keyStore = KeyStore.getInstance("AndroidKeyStore")
val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
val signature = Signature.getInstance("SHA256withECDSA")
signature.initSign(privateKey)
signature.update(imageBytes)
val digitalSignature = signature.sign()
