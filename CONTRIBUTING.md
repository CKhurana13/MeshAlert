# Contributing to MeshAlert

We appreciate your interest in contributing to MeshAlert! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

Please be respectful and professional in all interactions within this project.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/MeshAlert.git
   cd MeshAlert
   ```
3. **Add upstream remote** for syncing:
   ```bash
   git remote add upstream https://github.com/CKhurana13/MeshAlert.git
   ```
4. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

1. **Open in Android Studio**
2. **Sync Gradle files** (Android Studio will prompt)
3. **Install Build Tools** if needed
4. **Run on emulator or device**

## Making Changes

### Code Style

- Follow [Android Code Style Guidelines](https://developer.android.com/kotlin/style-guide)
- Use **Kotlin** for new code
- Maintain consistent indentation (4 spaces)
- Write descriptive variable and function names

### Git Workflow

1. **Make your changes** in the feature branch
2. **Test thoroughly** on emulator/device
3. **Commit with clear messages**:
   ```bash
   git commit -m "feat: Add feature description"
   git commit -m "fix: Resolve issue #123"
   git commit -m "docs: Update README"
   ```
4. **Keep commits atomic** (one logical change per commit)

### Commit Message Format

Use conventional commits for clarity:

```
type(scope): subject

description

Fixes #issue-number
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding/updating tests
- `chore`: Build, dependencies, etc.

## Submitting Changes

1. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
2. **Open a Pull Request** on GitHub
3. **Fill out PR template** with:
   - Description of changes
   - Related issues/discussions
   - Testing performed
   - Screenshots (if UI changes)

4. **Address code review feedback** promptly

## Reporting Issues

### Bug Reports

Include:
- Clear title describing the issue
- Steps to reproduce
- Expected vs actual behavior
- Device/Android version info
- Screenshots/logs if applicable

### Feature Requests

Include:
- Clear description of the feature
- Use case/motivation
- Potential alternatives
- Any relevant references

## Testing

### Before Submitting PR

1. **Run unit tests**:
   ```bash
   ./gradlew test
   ```

2. **Run instrumented tests**:
   ```bash
   ./gradlew connectedAndroidTest
   ```

3. **Manual testing** on multiple API levels (29+)

4. **Check for BLE issues**:
   - Test on real device (not just emulator)
   - Verify permissions are granted
   - Test with actual BLE devices

### Test Coverage

Aim for reasonable test coverage:
- Unit tests for business logic
- Instrumented tests for UI components
- Integration tests for BLE operations

## Documentation

### JavaDoc Comments

Add for public methods/classes:

```kotlin
/**
 * Scans for BLE mesh devices within range.
 *
 * @param timeout Duration to scan in milliseconds
 * @return List of discovered BluetoothDevice objects
 */
fun scanForDevices(timeout: Long): List<BluetoothDevice>
```

### Update Documentation

- Update README if changing features
- Update API docs for new classes/methods
- Add comments for complex logic

## Project Maintainers

MeshAlert is maintained by **CKhurana13**. For major decisions or design discussions, please reach out to the maintainers.

## Review Process

- Maintainers will review your PR
- Feedback will be provided for improvements
- Once approved, your changes will be merged
- Your contribution will be recognized (if desired)

## Development Tips

### Useful Gradle Commands

```bash
# Build debug version
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease

# Run specific test
./gradlew test --tests com.example.blemeshapp.*

# Check for lint warnings
./gradlew lint

# Format code
./gradlew spotlessApply
```

### Debugging BLE Issues

- Enable Bluetooth logs: Settings > Developer Options > Enable BLE logging
- Use Android Profiler in Android Studio
- Check logcat for error messages:
  ```bash
  adb logcat | grep bluetooth
  ```

## Performance Considerations

- Minimize battery drain from BLE scanning
- Optimize map rendering for large tile caches
- Profile memory usage with Android Profiler
- Avoid memory leaks in Activities/Services

## Questions?

- Open an issue on GitHub
- Discuss in pull requests
- Check existing documentation

---

Thank you for contributing to MeshAlert! 🙏
