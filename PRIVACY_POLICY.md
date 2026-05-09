# Privacy Policy for Pixel Meter

**Effective Date:** 2025-12-27

Pixel Meter ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy
explains how our application handles your information.

## 1. Information Collection and Use

**We do not collect, store, or transmit any Service Personal Data or Device Data.**

Pixel Meter is designed to process all network traffic monitoring solely on your device ("local
processing"). No data regarding your network usage, websites visited, or app usage is sent to our
servers or any third parties.

## 2. Permissions and Their Usage

To provide its core functionality, Pixel Meter requests the following permissions. These permissions
are used strictly for the purposes described below:

* **`android.permission.ACCESS_NETWORK_STATE`**: Used to detect network connection status and
  identify physical network interfaces (Wi-Fi, Cellular) versus virtual ones (VPN).
* **`android.permission.INTERNET`**: Used **only** when you explicitly initiate the "Speed Test"
  feature (via Cloudflare). The app does not maintain a constant internet connection for background
  data reporting.
* **`android.permission.FOREGROUND_SERVICE`**: Required to keep the network monitor service active
  in the background to provide real-time updates to the notification bar or overlay.
* **`android.permission.SYSTEM_ALERT_WINDOW`**: Required if you choose to enable the "Floating
  Window" (Overlay) feature to display network speed over other apps.
* **`android.permission.POST_NOTIFICATIONS`**: Used to display the real-time network speed in the
  status bar/notification shade.

## 3. Third-Party Services

Pixel Meter does not include third-party analytics, advertising SDKs, or tracking tools.

The "Speed Test" feature allows you to open a web page (`speed.cloudflare.com`) using Chrome Custom
Tabs. This interaction is subject to Cloudflare's privacy policy and your browser's privacy
settings.

## 4. Children’s Privacy

Our Service does not address anyone under the age of 13. We do not knowingly collect personally
identifiable information from anyone under the age of 13.

## 5. Changes to This Privacy Policy

We may update our Privacy Policy from time to time. Thus, you are advised to review this page
periodically for any changes. We will notify you of any changes by posting the new Privacy Policy on
this page.

## 6. Contact Us

If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us by
submitting an issue on our [GitHub Repository](https://github.com/Pixel-Tailor-CN/PixelMeter).
