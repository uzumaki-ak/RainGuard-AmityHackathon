# RainGuardAI ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?logo=Kotlin&logoColor=white) ![Android](https://img.shields.io/badge/Android-14.0-3DDC84.svg?logo=android&logoColor=white) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.11-4285F4?logo=jetpackcompose&logoColor=white) ![ARCore](https://img.shields.io/badge/ARCore-1.41.0-4285F4?logo=google&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase-32.7.2-FFCA28?logo=firebase&logoColor=white)

---

## 📖 Introduction

**RainGuardAI** is an advanced disaster management and safety application built to empower users during flood and water-related emergencies. Leveraging cutting-edge technologies like **Augmented Reality (AR)** for navigation and **Generative AI (Gemini)** for emergency assistance, the app provides a comprehensive toolkit for survival and response. 

The application integrates real-time geospatial data, localized risk assessment, and community-driven reporting to create a resilient safety network. Whether it's finding the safest route to a shelter using AR or getting instant safety advice from an AI assistant, RainGuardAI is designed to be a reliable companion in critical situations.

---

## ✨ Features

- **AR Navigation & VR Guidance:** Uses ARCore and SceneView to provide intuitive, real-world overlays for evacuation routes, ensuring users stay on the safest path even when visibility is low.
- **AI Chat Assistant:** Integrated with Google's Gemini API to provide instant answers to safety queries, emergency protocols, and first-aid instructions.
- **Real-time Disaster Alerts:** Fetches and displays urgent flood alerts and weather warnings with severity levels and recommended actions.
- **Geospatial Risk Mapping:** Visualizes high-risk flood zones and safe areas using OpenStreetMap (OSMDroid) with custom data overlays.
- **Shelter & Resource Locator:** Identifies nearby emergency shelters with real-time capacity updates, accessibility details, and contact information.
- **Community Reporting:** Allows users to report hazards (blocked roads, rising water levels) with photo uploads via CameraX and precise location tagging.
- **Authority Dashboard:** Specialized interface for emergency responders to monitor reports and manage disaster response efforts.
- **Offline Persistence:** Utilizes Room Database for reliable access to critical information like emergency contacts and shelter locations even without internet connectivity.
- **Background Monitoring:** Uses WorkManager for periodic alert checks and location-based safety notifications.

---

## 🛠️ Tech Stack

| Library/Technology            | Purpose                                                | Version / Details                        |
|------------------------------|--------------------------------------------------------|------------------------------------------|
| **Kotlin**                   | Primary Programming Language                            | 1.9.23                                   |
| **Jetpack Compose**          | Modern UI Toolkit                                       | 1.5.11 (Compiler)                        |
| **ARCore / SceneView**       | Augmented Reality Navigation                            | 1.41.0 / 0.10.0                          |
| **Gemini API**               | AI-powered Chat Assistant                               | Integrated via Generative AI SDK         |
| **Firebase**                 | Analytics, Firestore (Database), Cloud Messaging        | BOM 32.7.2                               |
| **Room Persistence**         | Local Database for offline data                         | 2.6.1                                    |
| **Hilt / Dagger**            | Dependency Injection                                    | 2.51                                     |
| **OSMDroid**                 | Open-source Mapping and Navigation                      | 6.1.18                                   |
| **Retrofit / Moshi**         | REST API Communication & JSON Parsing                   | 2.9.0 / 1.15.1                           |
| **Coroutines / Flow**        | Asynchronous Programming                                | 1.7.3                                    |
| **CameraX**                  | Photo Capture for Incident Reporting                    | 1.3.2                                    |
| **Coil**                     | Image Loading and Caching                               | 2.6.0                                    |

---

## 🚀 Quick Start / Installation

### Prerequisites

- Android Studio Iguana or newer
- JDK 17
- An Android device with ARCore support (for AR features) or an AR-enabled emulator.

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/uzumaki-ak/RainGuardAI.git
   cd RainGuardAI
   ```

2. **API Configuration:**
   - The project uses Google Gemini. Ensure you have a valid API key.
   - For demo purposes, a key may be present in `app/build.gradle.kts`, but it is recommended to move this to a secure `local.properties` or environment variable.

3. **Firebase Setup:**
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.rainguard.ai`.
   - Download `google-services.json` and place it in the `app/` directory.

4. **Build & Run:**
   - Open the project in Android Studio.
   - Sync Gradle files.
   - Click **Run** or use the command line:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📁 Project Structure

```
/app
  ├── /src/main
  │   ├── /java/com/rainguard/ai/
  │   │   ├── /data/                # Repository, Data Sources, DTOs
  │   │   │   ├── /local/           # Room Database, DAOs, Entities
  │   │   │   ├── /remote/          # Retrofit Services, API Definitions
  │   │   ├── /di/                  # Hilt Modules
  │   │   ├── /ui/                  # Compose UI Layer
  │   │   │   ├── /screens/         # Feature-specific screens (AR, Chat, Home, etc.)
  │   │   │   ├── /components/      # Reusable UI components
  │   │   │   ├── /theme/           # Material3 Theme & Styles
  │   │   ├── /worker/              # Background WorkManager Tasks
  │   │   ├── MainActivity.kt       # Root Activity & Navigation
  │   │   └── RainGuardApplication.kt # Hilt Application Class
  │   ├── /assets/mock/             # Static JSON data for initial setup
  │   │   ├── alerts.json
  │   │   ├── risk_zones.json
  │   │   ├── shelters.json
  │   │   └── routes.json
  │   └── /res/                     # Drawables, Layouts, Values, XML
  ├── build.gradle.kts              # Module-level build config
  └── AndroidManifest.xml           # App Manifest
```

---

## 🔧 Configuration

### Environment Variables
- `GEMINI_API_KEY`: Required for the AI Chat Assistant. Defined in `app/build.gradle.kts`.

### Mock Data
The app initially populates its data from the `/assets/mock/` folder. These files (`alerts.json`, `shelters.json`, etc.) can be modified to test different disaster scenarios and locations.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Google ARCore** for the augmented reality capabilities.
- **Google Gemini** for powering the intelligent chat assistant.
- **OSMDroid** for providing flexible and free mapping solutions.
- **Jetpack Compose** team for the modern UI toolkit.

---

*RainGuardAI - Navigating Safety, Powered by Intelligence.*
