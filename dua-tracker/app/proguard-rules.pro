# Daily Dua — R8 rules. Libraries (Compose, Media3, DataStore) ship their own consumer rules.
# Keep the playback service and receivers referenced only from the manifest (R8 keeps these by default),
# and keep line numbers so crash reports stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
