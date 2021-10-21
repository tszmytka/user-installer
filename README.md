# User Installer

Command line application used to install other tools and apps as a regular (non-admin) user.

## Technology Stack

- Java 17
- GraalVM CE 21.1.0
- Gradle 7.0
- PicoCli 4.6

## Building a GraalVM Native Image

After successfully building the jar a GraalVM Native Image can be built with

```
native-image --no-fallback --allow-incomplete-classpath -jar "user-installer.jar"
```

## Supported Actions

- Recursively copying directories with files
- Resolving template variables to specified values
- Installing settings from a git repository
- Deleting Windows Registry Keys
- Removing directories
- Installing application plugins
