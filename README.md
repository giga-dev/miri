# What is this?

Miri is an Assistant for GigaSpaces R&D developers. Basically an IntelliJ IDEA plugin which helps with common tasks.

# Getting Started

## Building

IntelliJ plugins are built using `Gradle`. Use the following command to build the plugin:

```
gradlew buildPlugin
```

The build creates a zip file under `build/distributions`.

## Installing the plugin

To install the plugin, simply unzip it in the `plugins` folder under IntelliJ (e.g. `IntelliJ IDEA Community Edition 2018.1.1/plugins/miri` or `.IdeaIC2019.3/config/plugins`), and restart IntelliJ.
