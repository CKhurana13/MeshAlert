# MeshAlert - BLE Mesh Emergency Alert System

A Bluetooth Low Energy (BLE) mesh networking Android application for emergency alert broadcasting and location tracking over a decentralized mesh network.

## Overview

MeshAlert enables users to send emergency alerts across a decentralized BLE mesh network, track locations in real-time, and operate in offline-first mode after initial setup.

## Key Features

- BLE mesh scanning and device discovery
- Emergency alert broadcasting across mesh network
- Real-time location tracking with GPS integration
- Offline map viewing (OpenStreetMap via OSMDroid)
- User authentication system
- Multi-hop mesh relay for extended range
- Offline tile caching (100% offline capable)
- Permission handling for BLE, location, and storage

## Requirements

- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 14+
- **Java Version**: 11+
- **Gradle**: 8.0+

## Required Permissions

```xml
<!-- Bluetooth Permissions -->
android.permission.BLUETOOTH
android.permission.BLUETOOTH_ADMIN
android.permission.BLUETOOTH_SCAN
android.permission.BLUETOOTH_ADVERTISE
android.permission.BLUETOOTH_CONNECT

<!-- Location Permissions -->
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_COARSE_LOCATION

<!-- Storage & Internet -->
android.permission.WRITE_EXTERNAL_STORAGE (Android 10 and below)
android.permission.INTERNET (for map tile downloads)
```

## Project Structure

```
MeshAlert/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/blemeshapp/
│   │   │   │   ├── LoginActivity.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap/
│   │   │   │   └── values/
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Getting Started

### Prerequisites

- Android Studio 2023.1 or later
- Android SDK with API 29+
- Gradle 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/CKhurana13/MeshAlert.git
   cd MeshAlert
   ```

2. **Open in Android Studio**
   - File → Open → Select MeshAlert folder
   - Android Studio will auto-sync dependencies

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   ```bash
   ./gradlew installDebug
   ```

## Usage

1. **Launch the app** - LoginActivity appears first
2. **Authenticate** - Enter your credentials
3. **View the map** - Interactive OpenStreetMap interface
4. **Send alerts** - Broadcast emergency messages to the mesh
5. **Track devices** - See nearby devices and their locations

## Technologies Used

- **Language**: Kotlin
- **Framework**: Android AppCompat + Material Design
- **BLE**: Android Bluetooth API
- **Mapping**: OSMDroid (offline OpenStreetMap)
- **Build**: Gradle with Kotlin DSL

## Configuration

### Build Properties

Edit `gradle.properties` to customize:
- `compileSdk` - Target API level
- `minSdk` - Minimum API level  
- `versionCode` - Build version
- `versionName` - Release version

### Dependencies

Manage versions in `gradle/libs.versions.toml`

## Development

### Code Style

- Follow [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use 4-space indentation
- Write descriptive names for variables and functions

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Known Limitations

- BLE range typically 50-240m depending on device
- Map tiles require internet on first download
- Battery consumption increases with active BLE scanning
- Requires explicit permissions on Android 6.0+

## Troubleshooting

### BLE Scanning Issues
- Verify Location permission is granted
- Ensure Bluetooth is enabled
- Check if other devices are in range

### Map Not Loading
- Check Internet permission
- Verify network connectivity
- Clear app cache: Settings → Apps → MeshAlert → Clear Cache

### Build Errors
```bash
./gradlew clean build --refresh-dependencies
```

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit changes (`git commit -m 'Add YourFeature'`)
4. Push to branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file for details.

## Issues & Support

- [Report bugs](https://github.com/CKhurana13/MeshAlert/issues)
- [Suggest features](https://github.com/CKhurana13/MeshAlert/discussions)

## Author

**CKhurana13** - [GitHub](https://github.com/CKhurana13)

---

**Status**: Active Development  
**Last Updated**: April 2026  
**Version**: 1.0.0
