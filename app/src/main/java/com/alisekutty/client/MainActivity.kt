package com.alisekutty.client

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

class MainActivity : Activity() {

    companion object {
        private const val OWNER_PHONE = "6382241833"
        private const val MIC_CAMERA_REQUEST = 4103
        private const val PREFS = "alisekutty_local_licenses"
        private const val WA_URL = "https://web.whatsapp.com/"

        // Desktop page scaling percentage
        private const val DESKTOP_SCALE = 35
    }

    private val gold = Color.rgb(212, 175, 55)
    private val dark = Color.rgb(8, 8, 8)

    private val prefs by lazy {
        getSharedPreferences(PREFS, MODE_PRIVATE)
    }

    private lateinit var root: LinearLayout
    private lateinit var web: WebView

    private var isOwner = false

    private var pendingWebPermission: PermissionRequest? = null
    private var pendingWebResources: Array<String> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        CookieManager.getInstance().setAcceptCookie(true)

        showLogin()
    }

    // LOGIN SCREEN

    private fun showLogin() {

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 48, 28, 24)
            setBackgroundColor(dark)
        }

        panel.addView(TextView(this).apply {
            text = "Alise×Kutty ◆"
            textSize = 30f
            setTextColor(gold)
        })

        panel.addView(TextView(this).apply {
            text = "BLACK & GOLD • App access"
            textSize = 14f
            setTextColor(gold)
            setPadding(0, 8, 0, 28)
        })

        val accessInput = EditText(this).apply {
            hint = "Owner phone number OR license key"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setSingleLine(true)
        }

        panel.addView(accessInput)

        panel.addView(Button(this).apply {
            text = "LOGIN"

            setOnClickListener {

                val value = accessInput.text.toString().filter {
                    it.isDigit()
                }

                when {

                    value == OWNER_PHONE -> {
                        isOwner = true
                        showMainScreen()
                    }

                    isValidLicense(value) -> {
                        isOwner = false
                        showMainScreen()
                    }

                    else -> Toast.makeText(
                        this@MainActivity,
                        "Invalid owner number or license key",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })

        panel.addView(TextView(this).apply {
            text = "Owner access: enter the configured owner number. Other users must enter a valid generated license."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, 22, 0, 0)
        })

        setContentView(panel)
    }

    // LICENSE VALIDATION

    private fun isValidLicense(code: String): Boolean {

        if (code.length != 8) return false

        val records = readArray("licenses")
        val now = System.currentTimeMillis()

        for (i in 0 until records.length()) {

            val item = records.optJSONObject(i) ?: continue

            if (
                item.optString("code") == code &&
                !item.optBoolean("revoked") &&
                item.optLong("expiresAt") > now
            ) {
                return true
            }
        }

        return false
    }

    // MAIN SCREEN

    private fun showMainScreen() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(dark)
        }

        // TOP HEADER

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(10, 7, 10, 7)
            setBackgroundColor(Color.BLACK)
        }

        // APP LOGO

        top.addView(ImageView(this).apply {

            setImageResource(R.drawable.app_logo)

            contentDescription = "Alise×Kutty logo"

            adjustViewBounds = true

            scaleType = ImageView.ScaleType.FIT_CENTER

            layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                rightMargin = 8
            }
        })

        // APP NAME

        top.addView(TextView(this).apply {

            text = "Alise×Kutty ◆"

            textSize = 18f

            setTextColor(gold)

            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        })

        // OWNER BUTTON

        if (isOwner) {
            top.addView(
                makeGoldButton("OWNER") {
                    showOwnerDashboard()
                }
            )
        }

        // OGXISAI BUTTON

        top.addView(
            makeGoldButton("OGxISAI") {
                injectOgxIsai()
            }
        )

        // STATUS BAR

        val status = TextView(this).apply {

            text = "BLACK & GOLD • WhatsApp Web desktop view"

            textSize = 12f

            setTextColor(gold)

            setPadding(10, 7, 10, 7)

            setBackgroundColor(Color.rgb(25, 22, 12))
        }

        // WHATSAPP WEBVIEW

        web = WebView(this).apply {

            setBackgroundColor(Color.BLACK)

            settings.apply {

                javaScriptEnabled = true

                domStorageEnabled = true

                databaseEnabled = true

                loadsImagesAutomatically = true

                javaScriptCanOpenWindowsAutomatically = true

                mediaPlaybackRequiresUserGesture = false

                // DESKTOP USER AGENT

                userAgentString =
                    "Mozilla/5.0 (X11; Linux x86_64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/131.0.0.0 Safari/537.36"

                // DESKTOP PAGE FITTING

                useWideViewPort = true

                loadWithOverviewMode = true

                setSupportZoom(false)

                builtInZoomControls = false

                displayZoomControls = false

                textZoom = 100

                allowContentAccess = true

                allowFileAccess = true
            }

            // IMPORTANT SCREEN WIDTH FIX

            setInitialScale(DESKTOP_SCALE)

            isHorizontalScrollBarEnabled = false

            isVerticalScrollBarEnabled = true

            overScrollMode = WebView.OVER_SCROLL_NEVER

            CookieManager.getInstance()
                .setAcceptThirdPartyCookies(this, true)

            // WEB PERMISSIONS

            webChromeClient = object : WebChromeClient() {

                override fun onPermissionRequest(
                    request: PermissionRequest
                ) {

                    runOnUiThread {

                        val resources = request.resources

                        val needed = mutableListOf<String>()

                        if (
                            resources.contains(
                                PermissionRequest.RESOURCE_AUDIO_CAPTURE
                            )
                        ) {
                            needed.add(Manifest.permission.RECORD_AUDIO)
                        }

                        if (
                            resources.contains(
                                PermissionRequest.RESOURCE_VIDEO_CAPTURE
                            )
                        ) {
                            needed.add(Manifest.permission.CAMERA)
                        }

                        val missing = needed.distinct().filter {

                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                it
                            ) != PackageManager.PERMISSION_GRANTED
                        }

                        if (missing.isEmpty()) {

                            request.grant(resources)

                        } else {

                            pendingWebPermission = request

                            pendingWebResources = resources

                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                missing.toTypedArray(),
                                MIC_CAMERA_REQUEST
                            )
                        }
                    }
                }

                override fun onPermissionRequestCanceled(
                    request: PermissionRequest
                ) {

                    if (pendingWebPermission == request) {

                        pendingWebPermission = null

                        pendingWebResources = emptyArray()
                    }
                }
            }

            // WEB PAGE NAVIGATION

            webViewClient = object : WebViewClient() {

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {

                    super.onPageFinished(view, url)

                    if (
                        url?.startsWith(
                            "https://web.whatsapp.com"
                        ) == true
                    ) {

                        injectOgxIsai()
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val url = request?.url ?: return false

                    return if (
                        url.scheme == "https" &&
                        (
                            url.host == "web.whatsapp.com" ||
                            url.host == "www.whatsapp.com" ||
                            url.host == "whatsapp.com"
                        )
                    ) {

                        false

                    } else {

                        try {

                            startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    Uri.parse(url.toString())
                                )
                            )

                        } catch (_: Exception) {
                        }

                        true
                    }
                }
            }
        }

        root.addView(top)

        root.addView(status)

        root.addView(
            web,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        // LOAD WHATSAPP

        web.loadUrl(WA_URL)
    }

    // GOLD BUTTON

    private fun makeGoldButton(
        label: String,
        action: () -> Unit
    ): Button {

        return Button(this).apply {

            text = label

            setTextColor(Color.BLACK)

            setBackgroundColor(gold)

            setOnClickListener {
                action()
            }
        }
    }

    // OWNER DASHBOARD

    private fun showOwnerDashboard() {

        if (!isOwner) return

        val options = arrayOf(
            "Generate 8-digit license",
            "Revoke license",
            "View license records",
            "Log out"
        )

        AlertDialog.Builder(this)
            .setTitle("OWNER CONTROL • BLACK & GOLD")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> generateLicense()

                    1 -> revokeLicense()

                    2 -> showLicenseRecords()

                    3 -> {

                        isOwner = false

                        web.stopLoading()

                        web.destroy()

                        showLogin()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // GENERATE LICENSE

    private fun generateLicense() {

        val daysInput = EditText(this).apply {

            hint = "Validity in days"

            setText("30")

            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)

            .setTitle("Generate license")

            .setMessage("Create a license for another user.")

            .setView(daysInput)

            .setPositiveButton("Generate") { _, _ ->

                val days =
                    daysInput.text.toString().toLongOrNull()

                if (days == null || days !in 1..3650) {

                    Toast.makeText(
                        this,
                        "Enter 1–3650 days",
                        Toast.LENGTH_LONG
                    ).show()

                    return@setPositiveButton
                }

                val code = (
                    10000000 +
                    SecureRandom().nextInt(90000000)
                ).toString()

                val records = readArray("licenses")

                records.put(
                    JSONObject()
                        .put("code", code)
                        .put("createdAt", System.currentTimeMillis())
                        .put(
                            "expiresAt",
                            System.currentTimeMillis() +
                                days * 86_400_000L
                        )
                        .put("revoked", false)
                )

                prefs.edit()
                    .putString("licenses", records.toString())
                    .apply()

                val clipboard =
                    getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        "Alise Kutty license",
                        code
                    )
                )

                AlertDialog.Builder(this)

                    .setTitle("License generated")

                    .setMessage(
                        "$code\nValid for $days days.\nCopied to clipboard."
                    )

                    .setPositiveButton("OK", null)

                    .show()
            }

            .setNegativeButton("Cancel", null)

            .show()
    }

    // REVOKE LICENSE

    private fun revokeLicense() {

        val input = EditText(this).apply {

            hint = "8-digit license key"

            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)

            .setTitle("Revoke license")

            .setView(input)

            .setPositiveButton("Revoke") { _, _ ->

                val code =
                    input.text.toString().filter { it.isDigit() }

                val records = readArray("licenses")

                var found = false

                for (i in 0 until records.length()) {

                    val item =
                        records.optJSONObject(i) ?: continue

                    if (item.optString("code") == code) {

                        item.put("revoked", true)

                        found = true
                    }
                }

                if (found) {

                    prefs.edit()
                        .putString("licenses", records.toString())
                        .apply()
                }

                Toast.makeText(
                    this,
                    if (found)
                        "License revoked"
                    else
                        "License not found on this device",
                    Toast.LENGTH_LONG
                ).show()
            }

            .setNegativeButton("Cancel", null)

            .show()
    }

    // VIEW LICENSE RECORDS

    private fun showLicenseRecords() {

        val records = readArray("licenses")

        val text = StringBuilder()

        val now = System.currentTimeMillis()

        for (i in 0 until records.length()) {

            val item =
                records.optJSONObject(i) ?: continue

            val state = when {

                item.optBoolean("revoked") ->
                    "REVOKED"

                item.optLong("expiresAt") <= now ->
                    "EXPIRED"

                else ->
                    "ACTIVE"
            }

            text.append(item.optString("code"))
                .append(" • ")
                .append(state)
                .append('\n')
        }

        if (text.isEmpty()) {
            text.append("No licenses generated yet.")
        }

        AlertDialog.Builder(this)

            .setTitle("License records")

            .setMessage(text.toString())

            .setPositiveButton("OK", null)

            .show()
    }

    // READ LICENSE DATABASE

    private fun readArray(key: String): JSONArray {

        return runCatching {

            JSONArray(
                prefs.getString(key, "[]")
            )

        }.getOrElse {

            JSONArray()
        }
    }

    // OGXISAI INJECTOR

    private fun injectOgxIsai() {

        if (!::web.isInitialized) return

        val js = runCatching {

            assets.open("ogxisai/injector.js")
                .bufferedReader()
                .use { it.readText() }

        }.getOrElse {

            Toast.makeText(
                this,
                "OGxISAI injector asset is missing",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val encoded =
            android.util.Base64.encodeToString(
                js.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )

        val wrapper =
            "(function(){var code=atob('$encoded');(0,Function)(code)();})();"

        web.evaluateJavascript(wrapper, null)
    }

    // CAMERA AND MICROPHONE PERMISSIONS

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == MIC_CAMERA_REQUEST) {

            val request = pendingWebPermission

            val resources = pendingWebResources

            pendingWebPermission = null

            pendingWebResources = emptyArray()

            val allGranted =
                grantResults.isNotEmpty() &&
                grantResults.all {
                    it == PackageManager.PERMISSION_GRANTED
                }

            if (allGranted && request != null) {

                request.grant(resources)

            } else {

                request?.deny()

                Toast.makeText(
                    this,
                    "Camera/microphone permission was not granted",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // BACK BUTTON

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        if (
            ::web.isInitialized &&
            web.canGoBack()
        ) {

            web.goBack()

        } else {

            super.onBackPressed()
        }
    }

    // DESTROY WEBVIEW

    override fun onDestroy() {

        if (::web.isInitialized) {

            web.stopLoading()

            web.destroy()
        }

        super.onDestroy()
    }
}
