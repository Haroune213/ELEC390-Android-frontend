plugins {
    // Gardez vos plugins existants
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // AJOUTEZ CETTE LIGNE :
    id("com.google.gms.google-services") version "4.4.0" apply false
}