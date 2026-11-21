# ClipForge AI — Android V1

## V1 features
- Select a video from Android storage.
- Download a **direct video-file URL** (for example, a public MP4 URL).
- Enter start/end time in seconds.
- Export the selected segment as MP4 on the phone.
- Share the exported clip.

## Important limitations
- YouTube watch/live URLs are not direct video-file URLs and are not supported by this app.
- This is a manual clip cutter, not an AI highlight detector yet.
- Video export uses Android Media3 Transformer and may take time or fail for unsupported codecs.
- Exported files are stored in the app's Movies folder. Share the file from the completion prompt.
- Keep at least 150 MB free; more is recommended for large source videos.
- Use videos you own or have permission to edit and reuse.

## Build APK using only your phone
1. Create/sign in to a GitHub account in Chrome.
2. Create a **public** repository named `ClipForgeAI`.
3. Upload all files and folders from this ZIP, preserving their folder structure.
4. Open the repository's **Actions** tab.
5. If prompted, enable GitHub Actions.
6. Open **Build ClipForge APK**, tap **Run workflow**.
7. Wait for the green check mark, open the completed run, scroll to **Artifacts**, and download `ClipForgeAI-debug-apk`.
8. Extract the ZIP on your phone and tap `app-debug.apk` to install. Android may ask you to allow installs from Chrome or your file manager.

## Phone use
1. Open ClipForge AI.
2. Select a local video, or enter a direct MP4/WebM file URL and download it.
3. Enter start and end times in seconds.
4. Tap EXPORT MP4 CLIP.
5. Share the result when export completes.

## Free-tier note
GitHub Actions has usage limits and policies that can change. Public repositories generally have free standard runner usage, but check GitHub's current limits. The app's video processing itself is local on your phone.
