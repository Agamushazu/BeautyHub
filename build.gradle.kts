// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    // ADD THIS LINE:
    alias(libs.plugins.google.gms.google.services) apply false
}
