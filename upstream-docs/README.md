# Hexal

A minecraft mod which is an addon for [Hex Casting](https://github.com/gamma-delta/HexMod/), adding new patterns (as well as whatever else I feel like).

## Branches

* `main`: Active development for Minecraft 1.20.1.
* `1.19.2`: Long-term support for Minecraft 1.19.2.
* `1.18.2`: Long-term support for Minecraft 1.18.2.

## Maven

Hexal is available on https://maven.hexxy.media. To depend on it, add something like this to your Gradle build script:

```kotlin
repositories {
    maven {
        url = uri("https://maven.hexxy.media")
    }
}

dependencies {
    // common (xplat template)
    modImplementation("ram.talia.hexal:hexal-common-$minecraftVersion:$hexalVersion")

    // common (Architectury)
    modImplementation("ram.talia.hexal:hexal-fabric-$minecraftVersion:$hexalVersion")

    // fabric
    modImplementation("ram.talia.hexal:hexal-fabric-$minecraftVersion:$hexalVersion")

    // forge (ForgeGradle)
    modImplementation(fg.deobf("ram.talia.hexal:hexal-forge-$minecraftVersion:$hexalVersion"))

    // forge (Architectury)
    modImplementation("ram.talia.hexal:hexal-forge-$minecraftVersion:$hexalVersion")
}
```
