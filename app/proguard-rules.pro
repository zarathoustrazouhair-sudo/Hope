# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# --- Hilt ---
-keep class com.syndic.app.SyndicApplication { *; }
-keep class com.syndic.app.di.** { *; }
-keep @dagger.hilt.EntryPoint class *
-keep @dagger.hilt.InstallIn class *
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
    @androidx.room.Ignore *;
}

# --- Retrofit & OkHttp ---
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keep class com.syndic.app.data.repository.** { *; }
-keep class com.syndic.app.domain.repository.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn okio.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}

# --- Supabase (Serialization) ---
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class com.syndic.app.data.repository.*Dto { *; }

# --- Compose ---
-keep class androidx.compose.** { *; }
