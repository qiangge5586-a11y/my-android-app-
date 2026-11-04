# Stage 74 — App catalog search and system details

- Added local filtering by launcher app label and package name.
- Long-pressing an app row opens Android's public App Info settings screen for that package.
- Normal tap still launches the selected app.
- The catalog only lists launcher-visible activities; it does not enumerate hidden packages, install/uninstall packages, or change permissions.
- Added CI assertions for search and app-details behavior.
