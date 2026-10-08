# 🌱 EcoShare - Full Platform Suite

EcoShare is an end-to-end community resource sharing platform designed to promote local sustainability, circular economy, and neighborhood collaboration.

---

## 📁 Repository Structure

This repository contains both components of the EcoShare platform:

### 1. `eco-share-main/` — Native Android Application
A 100% native Android mobile application built with **Kotlin** and **Jetpack**:
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **UI**: Material Design 3, XML Layouts, ViewBinding
- **Backend**: Firebase Auth, Cloud Firestore, Firebase Storage, Firebase Cloud Messaging
- **Features**: Resource catalog, real-time 1-on-1 and lobby chat, GPS-based nearby item discovery, community events, carbon footprint / CO2 offset tracking.

### 2. `all sharing/` — Web Application & AI Backend
A modern web application and Python backend service:
- **Frontend**: React + TypeScript + Vite + Tailwind CSS
- **AI Backend**: Python FastAPI service for automated image labeling, object detection, and sustainability categorization.
- **Features**: Web dashboard, resource listings, user authentication, and interactive map views.

---

## 🚀 Getting Started

### Android App (`eco-share-main/`)
1. Open the `eco-share-main/android` directory in **Android Studio**.
2. Sync Gradle dependencies.
3. Ensure `google-services.json` is properly configured.
4. Run on an Android device or emulator (Android 8.0+ / API 26+).

### Web App (`all sharing/`)
```bash
cd "all sharing"
npm install
npm run dev
```

### AI Backend (`all sharing/backend/`)
```bash
cd "all sharing/backend"
pip install -r requirements.txt
python main.py
```
