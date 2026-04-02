# SmartTask AI ProGuard rules

# Keep Room entities
-keep class com.smarttask.database.entities.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Gson models
-keep class com.smarttask.utils.GeminiAIService$** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep Retrofit
-keep class retrofit2.** { *; }
-keepattributes Exceptions
-dontwarn retrofit2.**

# Keep OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Keep Coroutines
-keepnames class kotlinx.coroutines.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep WorkManager workers
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }

# Keep security crypto
-keep class androidx.security.crypto.** { *; }

# Suppress warnings
-dontwarn com.google.errorprone.**
-dontwarn sun.misc.**
