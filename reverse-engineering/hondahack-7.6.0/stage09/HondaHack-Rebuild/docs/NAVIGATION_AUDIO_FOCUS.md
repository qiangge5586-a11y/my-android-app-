# Navigation audio-focus test boundary

The navigation screen sends destinations to installed map applications with the public `geo:` intent. Stage 93 adds a manual audio-focus test using Android's public `AudioManager` API and `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`.

## Expected behavior

- The user explicitly taps 请求临时音频焦点.
- The app asks Android for transient focus on the music stream.
- Other media apps may lower their volume if they honor the audio-focus request and the head unit's audio policy supports ducking.
- The user can release focus manually; the Activity also abandons focus when destroyed.

## Limitations

- The app does not directly change another app's volume.
- The app does not observe navigation state from external map applications.
- The app does not intercept OEM navigation broadcasts or bind private car audio services.
- Actual ducking behavior varies by Android version, map/player implementation and OEM audio policy.

This is a manual compatibility test, not a claim of automatic parity with the original MyService audio-focus workflow.
