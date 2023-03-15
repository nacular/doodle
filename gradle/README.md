# How to upgrade the Gradle Wrapper version

Visit the [Gradle website](https://gradle.org/releases) and decide the:

- desired version
- desired distribution type
- what is the sha256 for the version and type chosen above

Adjust the following command with tha arguments above and execute it twice:

```
./gradlew wrapper --gradle-version 8.0.2 \
    --distribution-type bin \
    --gradle-distribution-sha256-sum ff7bf6a86f09b9b2c40bb8f48b25fc19cf2b2664fd1d220cd7ab833ec758d0d7
```

---

The first execution should automatically update:

- `doodle/gradle/wrapper/gradle-wrapper.properties`

---

The second execution should then update:

- `doodle/gradle/wrapper/gradle-wrapper.jar`
- `doodle/gradlew`
- `doodle/gradlew.bat`

---

Verify the upgraded `gradle/wrapper/gradle-wrapper.jar` checksum:

```
export WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
export CHECKSUM="91941f522fbfd4431cf57e445fc3d5200c85f957bda2de5251353cf11174f4b5"
echo "$CHECKSUM $WRAPPER_JAR" | sha256sum -c
```

---

The four updated files are ready to be committed.
