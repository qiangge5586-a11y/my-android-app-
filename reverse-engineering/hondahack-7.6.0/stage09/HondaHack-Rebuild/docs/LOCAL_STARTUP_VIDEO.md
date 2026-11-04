# Local startup-video preset boundary

The MediaActivity allows a user to choose a video, preview it in an installed player, and (on Android 4.4/API 19 or later) save its content URI as a local preference using persistable read permission.

The saved preset is opened only when the user taps 打开已保存的启动视频. It does not run at device boot, register BOOT_COMPLETED, start a background service, or take over the OEM boot animation.

Android 4.2/4.3 picker fallback supports immediate preview only because persistable document permissions are unavailable. The app does not copy, scan, or upload the selected video.
