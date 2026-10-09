# Room
-keep class * extends androidx.room.RoomDatabase

# PDFBox Android
-keep class com.tom_roush.pdfbox.pdmodel.** { *; }
-keep class com.tom_roush.pdfbox.text.** { *; }
-dontwarn com.tom_roush.pdfbox.**
