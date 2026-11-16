<div align="center">

# COSUP 

**A full-stack web and Android platform built for a real community organisation in Tshwane, South Africa.**

![PHP](https://img.shields.io/badge/PHP-777BB4?logo=php&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Firebase Auth](https://img.shields.io/badge/Firebase_Auth-FFCA28?logo=firebase&logoColor=black)
![Leaflet](https://img.shields.io/badge/Leaflet.js-199900?logo=leaflet&logoColor=white)
![OpenStreetMap](https://img.shields.io/badge/OpenStreetMap-7EBC6F?logo=openstreetmap&logoColor=white)

Built by **The 5 Watermelons** for the **XISD5319 Work Integrated Learning (WIL)** module.

</div>

---

## About

COSUP V2 pairs a **PHP/MySQL web platform** with a native **Kotlin Android app**, built for an actual community organisation in Tshwane rather than a hypothetical brief. Both surfaces use **OpenStreetMap** for mapping, so there is no Google Maps API key or billing account to manage.

## Features

<!-- TODO: replace or extend with the real feature list (screens, user roles, modules). -->

- **Authentication** through Firebase Authentication
- **Interactive maps** on both platforms: Leaflet.js on the web and OSMDroid on Android
- **Local data persistence** on Android using Room
- **Networking** on Android via Retrofit2
- **Consistent, professional branding** across web and mobile

## Tech Stack

| Area | Technology |
| --- | --- |
| Web platform | PHP, HTML/CSS/JavaScript, Leaflet.js |
| Database | MySQL (XAMPP for local development) |
| Authentication | Firebase Authentication |
| Android app | Kotlin, Retrofit2, Room, OSMDroid |
| Maps | OpenStreetMap |
| UI | Playfair Display, DM Sans, Font Awesome |

## Getting Started

### Prerequisites

- [XAMPP](https://www.apachefriends.org/) (Apache, MySQL and PHP)
- [Android Studio](https://developer.android.com/studio)
- A [Firebase](https://firebase.google.com/) project with **Authentication** enabled
- Git

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>
```

### 2. Set up the web platform and database

<!-- TODO: confirm the web folder name, SQL file name and config file path. -->

1. Copy the web project folder into XAMPP's `htdocs` directory.
2. Start **Apache** and **MySQL** from the XAMPP Control Panel.
3. Open phpMyAdmin (`http://localhost/phpmyadmin`), create a database and import the SQL schema from the repository.
4. Update the database credentials in the PHP configuration file.
5. Visit `http://localhost/<web-folder>/` in your browser.

### 3. Run the Android app

1. Open the Android project in Android Studio and let Gradle sync.
2. Add your Firebase configuration file (`google-services.json`) to the `app/` module.
3. Set the Retrofit2 base URL to your local server:
   - Emulator: `http://10.0.2.2/<web-folder>/`
   - Physical device: `http://<your-computer-LAN-IP>/<web-folder>/`
4. Run the app on an emulator or a connected device.

> **Note:** If this repository is public, keep `google-services.json` and your database credentials out of version control (add them to `.gitignore`).

## Design Notes

- **Typography:** Playfair Display and DM Sans
- **Colours:** ![#3BA53E](https://img.shields.io/badge/%233BA53E-3BA53E?style=flat-square) brand green and white
- **Icons:** Font Awesome throughout, with no emoji in the UI
- **Maps:** OpenStreetMap only. Google Maps is intentionally not used, which keeps the project free of billing requirements

## Team

Developed by **The 5 Watermelons**.

<!--
TODO: uncomment and fill in each member.

| Name | Role | GitHub |
| --- | --- | --- |
| Member 1 | Role | [@handle](https://github.com/handle) |
-->

## Acknowledgements

- Map data &copy; [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors
- [Leaflet](https://leafletjs.com/) and [OSMDroid](https://github.com/osmdroid/osmdroid) for open-source mapping
- [Font Awesome](https://fontawesome.com/) and [Google Fonts](https://fonts.google.com/) (Playfair Display, DM Sans)
- The community organisation in Tshwane this platform was built for
