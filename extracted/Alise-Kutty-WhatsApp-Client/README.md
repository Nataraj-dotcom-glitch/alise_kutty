# Alise×Kutty — Black & Gold + OGxISAI

Unofficial Android WebView client for WhatsApp Web. It embeds the supplied OGxISAI `injector.js` into the WhatsApp Web page and requests Android microphone permission when the page asks for audio capture.

## Build APK
1. Open this folder in Android Studio (JDK 17, Android SDK platform 35).
2. Allow Gradle sync.
3. Build > Build Bundle(s) / APK(s) > Build APK(s).
4. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Or push to GitHub and run **Actions → Alise Kutty APK Build**. Download the `Alise-Kutty-APK` artifact.

## OGxISAI limitations
- The supplied Chrome/Firefox extension is not a native Android app. This project embeds its page injector in the WhatsApp Web WebView instead of installing the browser extension.
- Microphone permission is requested at runtime. Use the OGxISAI panel only after allowing permission.
- This does not affect calls in the official WhatsApp Android app. It only targets WhatsApp Web in this app.
- WhatsApp Web may change its WebRTC implementation; Android WebView may restrict audio-track replacement. Therefore processed voice reaching the other caller is **not guaranteed**. Test a call with a second device and obtain the other participant's consent.
- Instagram and ShareChat targets from the extension are not wired into this WhatsApp-only WebView.
- Only import CSS theme ZIPs through the in-app theme importer; the OGxISAI JavaScript is bundled as app assets and is not treated as a user-importable theme.

This is an unofficial third-party client and is not affiliated with or endorsed by WhatsApp/Meta. Never share your WhatsApp verification code or QR login with anyone.

## Owner tools (prototype)
The black-and-gold top bar now includes an **OWNER** panel configured with the supplied owner phone number, Discord owner ID, support server ID, and Discord invite. The panel includes:
- Generate random 8-digit license codes with a validity period in days (1–3650).
- Revoke or restore a locally stored license code.
- Add/remove phone numbers from a local ban list.
- View local license and ban records.
- Open the Discord support invite.

### Critical security limitation
These owner tools are a **local prototype only**. The owner phone number is embedded in the APK and is not a secret. License and ban records are stored in the app's private local preferences, so they only exist on that installation and can be modified by a determined attacker. They do not validate users across devices and do not enforce a global ban. A client-only APK cannot be made uncrackable.

For production licensing, move owner authentication, license generation/validation/expiry/revocation, and ban/unban decisions to a trusted HTTPS backend. Protect owner actions with real authentication (for example, Discord OAuth plus server-side role checks or passkeys), store signing keys only on the server, and have the app verify short-lived signed license tokens. Do not put API secrets, signing private keys, or Discord bot tokens in the APK. Use Play Integrity or a comparable attestation service as an additional signal, not as the only protection.
