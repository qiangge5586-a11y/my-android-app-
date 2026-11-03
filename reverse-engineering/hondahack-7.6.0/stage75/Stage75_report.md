# Stage 75 — Shared preference consistency fix

The app-catalog count display now reads `show_app_count` from the same default SharedPreferences store used by SettingsActivity. Previously the catalog read Activity-private preferences, so the UI toggle could not affect it.

The static regression check now asserts use of PreferenceManager's shared default store.
