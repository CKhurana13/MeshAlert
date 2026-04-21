# MeshAlert

A Bluetooth Low Energy (BLE) mesh networking Android application for emergency alert broadcasting and location tracking over a decentralized mesh network.

## Overview

MeshAlert is an Android app that leverages Bluetooth Low Energy mesh technology to create a decentralized communication network. The app enables users to:

- **Send Alerts** - Broadcast emergency alerts across the BLE mesh network
- **Track Locations** - Share and view locations using integrated mapping
- **Offline-First** - Operate without internet connectivity (after initial setup)
- **Mesh Networking** - Extend communication range through multi-hop mesh relay

## Features

- ✅ BLE mesh scanning and device discovery
- ✅ User authentication (Login Activity)
- ✅ Interactive offline map viewing with OpenStreetMap (via OSMDroid)
- ✅ Real-time location tracking
- ✅ GPS integration for precise positioning
- ✅ Permission handling for BLE, location, and storage
- ✅ Offline tile caching for maps (100% offline after initial cache)

## Requirements

- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 14+
- **Java Version**: 11+
- **Gradle**: 8.0+

### Permissions

The app requires the following permissions:

- **Bluetooth**: `BLUETOOTH`, `BLUETOOTH_ADMIN`, `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`
- **Location**: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
- **Storage**: `WRITE_EXTERNAL_STORAGE` (Android 10 and below)
- **Internet**: `INTERNET` (for downloading map tiles)

## Project Structure

```
MeshAlert/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/blemeshapp/
│   │   │   │   ├── LoginActivity.kt
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       ├── drawable/
│   │   │       ├── mipmap/
│   │   │       ├── values/
│   │   │       └── xml/
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

## Setup & Installation

### Prerequisites

- Android Studio 2023.1+
- Android SDK (API 29+)
- Gradle 8.0+

### Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/CKhurana13/MeshAlert.git
   cd MeshAlert
   ```

2. **Open in Android Studio**
   - File > Open > Select the MeshAlert project
   - Android Studio will automatically download dependencies

3. **Build the Project**
   ```bash
   ./gradlew build
   ```

4. **Run on Emulator/Device**
   ```bash
   ./gradlew installDebug
   ```

## Usage

### Login
1. Launch the app - the LoginActivity is the entry point
2. Authenticate with your credentials

### Main Interface
1. View the interactive map powered by OpenStreetMap
2. Devices in the mesh network will appear as markers
3. Use the map to track locations and coordinate alerts

### BLE Mesh Operations
- The app scans for nearby BLE mesh devices
- Automatically relays messages through intermediate nodes
- All communication is encrypted and decentralized

## Technologies Used

- **Language**: Kotlin
- **BLE Framework**: Android Bluetooth API
- **Mapping**: OSMDroid (offline OpenStreetMap)
- **Architecture**: AppCompat + Material Design
- **Build System**: Gradle with Kotlin DSL

## Configuration

### Gradle Build Properties

Edit `gradle.properties` to configure:
- `compileSdk` - Target API level
- `minSdk` - Minimum API level
- `versionCode` - Build version
- `versionName` - Release version

### Proguard Rules

`app/proguard-rules.pro` configures code obfuscation for release builds.

## Development

### Adding Dependencies

Edit `gradle/libs.versions.toml` to manage library versions:

```toml
[libraries]
# Add new dependencies here
```

Then reference in `app/build.gradle.kts`.

### Building Variants

- **Debug**: Unoptimized, debuggable build for development
- **Release**: Optimized, obfuscated build for production

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests on device/emulator:
```bash
./gradlew connectedAndroidTest
```

## Known Limitations

- ⚠️ Mesh range depends on BLE range (typically 50-240m)
- ⚠️ Map tiles require internet on first download
- ⚠️ Battery consumption increases with active scanning
- ⚠️ Requires explicit permission grants on Android 6.0+

## Future Enhancements

- [ ] Push notifications for emergency alerts
- [ ] Message encryption and signing
- [ ] Mesh network topology visualization
- [ ] Battery optimization improvements
- [ ] Multi-user mesh collaboration
- [ ] Cloud sync for user data

## Troubleshooting

### BLE Scanning Not Working
- Ensure Location permission is granted
- Check BLE is enabled on device
- Verify other devices are in range

### Map Not Loading
- Check Internet permission
- Verify tile download is not blocked
- Clear app cache: Settings > Apps > MeshAlert > Clear Cache

### Build Fails
```bash
# Clean build
./gradlew clean build

# Update dependencies
./gradlew dependencies --refresh-dependencies
```

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit changes (`git commit -m 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues, questions, or suggestions, please open an issue on [GitHub Issues](https://github.com/CKhurana13/MeshAlert/issues).

## Author

**CKhurana13** - [GitHub Profile](https://github.com/CKhurana13)

---

**Last Updated**: April 2026  
**Version**: 1.0.0
