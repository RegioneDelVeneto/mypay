# Upgrade Gradle

Verify current version with command:

`./gradlew --version`

Then upgrade it with command (ex. in case desired version is `7.3.3`):

`./gradlew wrapper --gradle-version 7.3.3`

# Upgrade dependencies

Check if some dependencies may be upgraded using [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin) with command:

`./gradlew dependencyUpdates -Drevision=release`

E