# Contributing to MeshAlert

Thank you for your interest in contributing to MeshAlert! We appreciate your help in improving this project.

## Code of Conduct

Be respectful and professional in all interactions within this project.

## How to Contribute

### Reporting Bugs

**Before submitting a bug report:**
- Check if the issue already exists
- Use a clear, descriptive title
- Provide steps to reproduce
- Describe the expected vs actual behavior
- Include screenshots if applicable

**Include:**
- Device model and Android version
- App version
- Detailed steps to reproduce
- Error logs or stack traces (if available)

### Suggesting Enhancements

**Before suggesting an enhancement:**
- Check if it's already been requested
- Provide a clear, descriptive title
- Explain the motivation and use case
- List any alternative solutions you've considered

### Pull Requests

1. **Fork the repository**
   ```bash
   git clone https://github.com/YOUR-USERNAME/MeshAlert.git
   cd MeshAlert
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow the code style guidelines
   - Keep commits atomic and well-documented
   - Add tests for new functionality

4. **Commit with clear messages**
   ```bash
   git commit -m "feat: Add your feature"
   git commit -m "fix: Resolve issue #123"
   git commit -m "docs: Update documentation"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open a Pull Request on GitHub**
   - Write a clear PR description
   - Reference related issues
   - Describe testing performed
   - Include screenshots for UI changes

## Development Setup

### Prerequisites

- Android Studio 2023.1+
- Java 11+
- Android SDK (API 29+)
- Git

### Setup Steps

1. Clone your fork
2. Open in Android Studio
3. Let Gradle sync dependencies automatically
4. Run `./gradlew build` to verify setup

## Code Guidelines

### Kotlin Style

- Follow [Google's Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- Use descriptive variable and function names
- Add comments for complex logic
- Avoid deep nesting

### Commit Messages

```
type(scope): subject

description

Fixes #issue-number
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`

### Testing

Before submitting a PR:

```bash
# Run unit tests
./gradlew test

# Run lint checks
./gradlew lint

# Build debug version
./gradlew assembleDebug
```

## Review Process

1. Maintainers review your PR
2. Address feedback and make changes
3. Re-request review once updates are complete
4. Once approved, your changes will be merged

## Development Tips

### Useful Commands

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Check for issues
./gradlew lint

# Debug on device
./gradlew installDebug
adb logcat
```

### Debugging BLE

- Check logcat: `adb logcat | grep bluetooth`
- Use Android Profiler in Android Studio
- Test on real devices (not just emulator for BLE)
- Enable Developer Options for detailed logging

### Performance Tips

- Profile memory: Android Studio → Profiler
- Monitor battery usage: Settings → Battery
- Test on minimum SDK version (API 29)

## Questions?

- Open an issue on GitHub
- Start a discussion on GitHub Discussions
- Comment on related pull requests

## Recognition

Contributors will be recognized in the project's contributor list!

---

Thank you for contributing to MeshAlert! 🙏
