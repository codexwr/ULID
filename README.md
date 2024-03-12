# Universally Unique Lexicographically Sortable Identifier
This is a ULID library for the Kotlin version. <br/>

It has been converted for use in JVM Kotlin projects from the Java source code [huxi ULID](https://github.com/huxi/sulky/tree/master/sulky-ulid), which is written according to the [ULID spec](https://github.com/ulid/spec).


## Installation
Add the *GitHub repository* or *Jitpack* to your `build.gradle.kts`:
```kotlin
repositories {
    // github packages
    maven {
        url = uri("https://maven.pkg.github.com/codexwr/ULID")
        credentials {
            username = "github username"
            password = "access token"  // The access token must contain 'read:packages' permission.
        }
    }
    // or Jitpack
    maven {
        url = uri("https://jitpack.io")
    }
}
```

Add the dependency to your `build.gradle.kts`:
```kotlin
dependencies {
    // github packages
    implementation("com.chans.codexwr:ulid:1.0.1")
    // or Jitpack 
    implementation("com.github.codexwr:ULID:1.0.1")
}
```
## Usage
ULID generation example:

```kotlin
/// using new instance
val ulidInstance = ULID(SecureRandom())
val ulidString = ulidInstance.nextULID()
val ulid2String = ulidInstance.nextULID(System.currentTimeMillis())
val ulidValue = ulidInstance.nextValue()
val ulid2Value = ulidInstance.nextValue(System.currentTimeMillis())

/// using singleton instance
val ulidString = ULID.nextULID()
val ulidValue = ULID.nextValue()
```

ULID parsing example:
```kotlin
// parsing from ULID string
val ulidString = ULID.nextULID()
val ulidValue = ULID.parseULID(ulidString)

// parsing from UUID
val ulidValue = ULID.fromUUID(UUID.randomUUID())
```

ULID Value to ULID String:
```kotlin
val ulidString = ULID.nextValue().toString()
```
