# HikingHappy ProGuard Rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn dagger.hilt.**

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.happyclaw.hikinghappy.**$$serializer { *; }
-keepclassmembers class com.happyclaw.hikinghappy.** {
    *** Companion;
}
-keepclasseswithmembers class com.happyclaw.hikinghappy.** {
    kotlinx.serialization.KSerializer serializer(...);
}
