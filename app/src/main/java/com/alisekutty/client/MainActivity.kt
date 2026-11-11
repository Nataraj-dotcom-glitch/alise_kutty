package com.alisekutty.client

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.zip.ZipInputStream

class MainActivity : Activity() {

    private lateinit var web: WebView

    private val pickZip = 4102
    private val permissionRequestCode = 4103

    private var pendingWebPermission: PermissionRequest? = null

    private val gold = Color.rgb(212, 175, 55)

    private val ownerPhone = "6382241833"
    private val ownerDiscordId = "1353995912006860871"
    private val supportServerId = "1569616110318526535"
    private val supportInvite = "https://discord.gg/AeQbSFMapG"

    private val extensionsDir by lazy {
        File(filesDir, "extensions").apply { mkdirs() }
    }

    private val adminPrefs by lazy {
        getSharedPreferences("owner_admin_prototype", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CookieManager.getInstance().setAcceptCookie(true)

        buildScreen()

        web.loadUrl("https://web.whatsapp.com/")
    }

    // --------------------------------------------------
    // MAIN SCREEN
    // --------------------------------------------------

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Alise×Kutty ◆"
            textSize = 19f
            setTextColor(gold)
            layoutParams =
                LinearLayout.LayoutParams(0, -2, 1f)
        }

        val owner = Button(this).apply {
            text = "OWNER"
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener {
                showOwnerAccess()
            }
        }

        val extension = Button(this).apply {
            text = "OGxISAI"
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener {
                showExtensions()
            }
        }

        bar.addView(title)
        bar.addView(owner)
        bar.addView(extension)

        val status = TextView(this).apply {
            text = "BLACK & GOLD • WhatsApp Desktop + OGxISAI"
            textSize = 12f
            setTextColor(gold)
            setPadding(10, 7, 10, 7)
            setBackgroundColor(Color.rgb(25, 22, 12))
        }

        web = WebView(this).apply {

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false

            // Desktop browser identity
            settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/131.0.0.0 Safari/537.36"

            // Display desktop layout in portrait
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            // Disable manual zoom
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false

            settings.textZoom = 100

            // Let WebView calculate the initial fit
            setInitialScale(0)

            CookieManager.getInstance()
                .setAcceptThirdPartyCookies(this, true)

            webChromeClient = object : WebChromeClient() {

                override fun onPermissionRequest(
                    request: PermissionRequest
                ) {
                    runOnUiThread {

                        val required = mutableListOf<String>()

                        if (
                            request.resources.contains(
                                PermissionRequest.RESOURCE_AUDIO_CAPTURE
                            ) &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            required.add(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }

                        if (
                            request.resources.contains(
                                PermissionRequest.RESOURCE_VIDEO_CAPTURE
                            ) &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            required.add(
                                Manifest.permission.CAMERA
                            )
                        }

                        if (required.isNotEmpty()) {

                            pendingWebPermission = request

                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                required.toTypedArray(),
                                permissionRequestCode
                            )

                        } else {

                            val allowed = request.resources.filter {
                                it == PermissionRequest.RESOURCE_AUDIO_CAPTURE ||
                                it == PermissionRequest.RESOURCE_VIDEO_CAPTURE
                            }

                            if (allowed.isNotEmpty()) {
                                request.grant(allowed.toTypedArray())
                            } else {
                                request.deny()
                            }
                        }
                    }
                }
            }

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
                        applyEnabledStyles()
                        injectOgxIsai()
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ): Boolean {

                    if (
                        url.startsWith("https://web.whatsapp.com") ||
                        url.startsWith("https://www.whatsapp.com")
                    ) {
                        return false
                    }

                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    )

                    return true
                }
            }
        }

        root.addView(bar)
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
    }

    // --------------------------------------------------
    // OGxISAI INJECTOR
    // --------------------------------------------------

    private fun injectOgxIsai() {

        runCatching {

            val js = assets.open(
                "ogxisai/injector.js"
            ).bufferedReader().use {
                it.readText()
            }

            val encoded = android.util.Base64.encodeToString(
                js.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )

            val script = """
                (function() {
                    if (document.getElementById('ogxisai-marker')) return;

                    var marker = document.createElement('script');
                    marker.id = 'ogxisai-marker';

                    var code = atob('$encoded');

                    (0, Function)(code)();
                })();
            """.trimIndent()

            web.evaluateJavascript(script, null)

        }.onFailure {
            Toast.makeText(
                this,
                "OGxISAI injector could not load",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // --------------------------------------------------
    // OWNER ACCESS
    // --------------------------------------------------

    private fun showOwnerAccess() {

        val input = EditText(this).apply {
            hint = "Enter owner phone number"
            inputType =
                android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(Color.BLACK)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)

            addView(
                TextView(this@MainActivity).apply {
                    text = "Owner access is configured"
                    setTextColor(gold)
                }
            )

            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Alise×Kutty • Owner Access")
            .setView(layout)
            .setPositiveButton("Continue") { _, _ ->

                val entered = input.text.toString()
                    .filter { it.isDigit() }

                if (entered == ownerPhone) {
                    showOwnerDashboard()
                } else {
                    Toast.makeText(
                        this,
                        "Owner number not recognised",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showOwnerDashboard() {

        val options = arrayOf(
            "Generate 8-digit license key",
            "Revoke license key",
            "Restore license key",
            "Ban phone number",
            "Unban phone number",
            "View records",
            "Support details"
        )

        AlertDialog.Builder(this)
            .setTitle("OWNER CONTROL")
            .setItems(options) { _, which ->

                when (which) {
                    0 -> generateLicenseKey()
                    1 -> changeLicenseStatus(true)
                    2 -> changeLicenseStatus(false)
                    3 -> changeBanStatus(true)
                    4 -> changeBanStatus(false)
                    5 -> showAdminRecords()
                    6 -> showSupportDetails()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // --------------------------------------------------
    // LICENSE GENERATION
    // --------------------------------------------------

    private fun generateLicenseKey() {

        val input = EditText(this).apply {
            hint = "Validity in days"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER
            setText("30")
        }

        AlertDialog.Builder(this)
            .setTitle("Generate license")
            .setView(input)
            .setPositiveButton("Generate") { _, _ ->

                val days = input.text.toString().toLongOrNull()

                if (days == null || days !in 1..3650) {
                    Toast.makeText(
                        this,
                        "Enter 1–3650 days",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val code = (
                    10000000 + SecureRandom().nextInt(90000000)
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

                adminPrefs.edit()
                    .putString("licenses", records.toString())
                    .apply()

                AlertDialog.Builder(this)
                    .setTitle("License generated")
                    .setMessage(
                        "Key: $code\nValidity: $days days\n\n" +
                        "Local prototype only."
                    )
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeLicenseStatus(revoke: Boolean) {

        val input = EditText(this).apply {
            hint = "8-digit license key"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (revoke) "Revoke license"
                else "Restore license"
            )
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->

                val key = input.text.toString()
                val records = readArray("licenses")

                var found = false

                for (i in 0 until records.length()) {

                    val item = records.optJSONObject(i)
                        ?: continue

                    if (item.optString("code") == key) {
                        item.put("revoked", revoke)
                        found = true
                    }
                }

                if (found) {
                    adminPrefs.edit()
                        .putString("licenses", records.toString())
                        .apply()
                }

                Toast.makeText(
                    this,
                    if (found) "License updated"
                    else "License not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --------------------------------------------------
    // BAN MANAGEMENT
    // --------------------------------------------------

    private fun changeBanStatus(ban: Boolean) {

        val input = EditText(this).apply {
            hint = "Phone number with country code"
            inputType =
                android.text.InputType.TYPE_CLASS_PHONE
        }

        AlertDialog.Builder(this)
            .setTitle(if (ban) "Ban number" else "Unban number")
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->

                val number = input.text.toString()
                    .filter { it.isDigit() }

                if (number.length < 8) {
                    Toast.makeText(
                        this,
                        "Invalid number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val old = readArray("bans")
                val updated = JSONArray()

                for (i in 0 until old.length()) {
                    val value = old.optString(i)

                    if (value != number) {
                        updated.put(value)
                    }
                }

                if (ban) {
                    updated.put(number)
                }

                adminPrefs.edit()
                    .putString("bans", updated.toString())
                    .apply()

                Toast.makeText(
                    this,
                    if (ban) "Number added to local list"
                    else "Number removed from local list",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun readArray(key: String): JSONArray {
        return runCatching {
            JSONArray(adminPrefs.getString(key, "[]"))
        }.getOrElse {
            JSONArray()
        }
    }

    // --------------------------------------------------
    // OWNER RECORDS
    // --------------------------------------------------

    private fun showAdminRecords() {

        val licenses = readArray("licenses")
        val bans = readArray("bans")

        val result = StringBuilder("LICENSES\n\n")

        for (i in 0 until licenses.length()) {

            val item = licenses.optJSONObject(i)
                ?: continue

            val state = when {
                item.optBoolean("revoked") -> "REVOKED"
                item.optLong("expiresAt") <
                    System.currentTimeMillis() -> "EXPIRED"
                else -> "ACTIVE"
            }

            result.append(item.optString("code"))
                .append(" • ")
                .append(state)
                .append("\n")
        }

        result.append("\nBANNED NUMBERS\n\n")

        for (i in 0 until bans.length()) {
            result.append(bans.optString(i)).append("\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Owner records")
            .setMessage(result.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSupportDetails() {

        AlertDialog.Builder(this)
            .setTitle("Support & Owner")
            .setMessage(
                "Owner phone: $ownerPhone\n" +
                "Owner Discord ID: $ownerDiscordId\n" +
                "Server ID: $supportServerId\n" +
                "Support: $supportInvite\n\n" +
                "License and ban records are stored locally."
            )
            .setPositiveButton("Open Discord") { _, _ ->

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(supportInvite)
                    )
                )
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // --------------------------------------------------
    // EXTENSION PANEL
    // --------------------------------------------------

    private fun showExtensions() {

        val options = arrayOf(
            "Open OGxISAI panel",
            "Import CSS theme ZIP",
            "Compatibility information"
        )

        AlertDialog.Builder(this)
            .setTitle("Alise×Kutty • OGxISAI")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> {
                        injectOgxIsai()

                        Toast.makeText(
                            this,
                            "OGxISAI injection requested",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    1 -> openZipPicker()

                    2 -> AlertDialog.Builder(this)
                        .setTitle("Compatibility")
                        .setMessage(
                            "This app uses WhatsApp Web inside Android WebView. " +
                            "Microphone and camera access depend on WebView support. " +
                            "Group calls, incoming calls and background calls " +
                            "are not guaranteed."
                        )
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    // --------------------------------------------------
    // CSS ZIP IMPORT
    // --------------------------------------------------

    private fun openZipPicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }

        startActivityForResult(intent, pickZip)
    }

    @Deprecated("Legacy file picker callback")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == pickZip &&
            resultCode == Activity.RESULT_OK
        ) {
            val uri = data?.data ?: return

            try {
                importCssZip(uri)

                applyEnabledStyles()

                Toast.makeText(
                    this,
                    "CSS theme imported",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Import failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun importCssZip(uri: Uri) {

        val id = "ext_${System.currentTimeMillis()}"

        val target = File(extensionsDir, id).apply {
            mkdirs()
        }

        var manifestFound = false
        var cssFound = false

        contentResolver.openInputStream(uri).use { raw ->

            requireNotNull(raw) {
                "Cannot open ZIP"
            }

            ZipInputStream(raw).use { zip ->

                var entry = zip.nextEntry

                while (entry != null) {

                    if (!entry.isDirectory) {

                        val name = entry.name.substringAfterLast('/')

                        require(
                            name == "manifest.json" ||
                            name == "style.css"
                        ) {
                            "Only manifest.json and style.css are accepted"
                        }

                        val bytes = zip.readBytes()

                        require(bytes.size <= 512 * 1024) {
                            "File too large"
                        }

                        File(target, name).writeBytes(bytes)

                        if (name == "manifest.json") {
                            manifestFound = true
                        }

                        if (name == "style.css") {
                            cssFound = true
                        }
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        require(manifestFound && cssFound) {
            "ZIP must contain manifest.json and style.css"
        }

        val manifest = JSONObject(
            File(target, "manifest.json").readText()
        )

        require(manifest.optString("entry") == "style.css") {
            "Manifest entry must be style.css"
        }

        File(target, "enabled.flag").writeText("enabled")
    }

    private fun applyEnabledStyles() {

        val css = extensionsDir.listFiles()
            ?.filter {
                it.isDirectory &&
                File(it, "enabled.flag").exists()
            }
            ?.mapNotNull {
                runCatching {
                    File(it, "style.css").readText()
                }.getOrNull()
            }
            ?.joinToString("\n")
            ?: ""

        val encoded = android.util.Base64.encodeToString(
            css.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )

        web.evaluateJavascript(
            """
            (function() {
                var css = atob('$encoded');
                var style = document.getElementById('alisekutty-css');

                if (!style) {
                    style = document.createElement('style');
                    style.id = 'alisekutty-css';
                    document.head.appendChild(style);
                }

                style.textContent = css;
            })();
            """.trimIndent(),
            null
        )
    }

    // --------------------------------------------------
    // CAMERA AND MICROPHONE PERMISSIONS
    // --------------------------------------------------

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

        if (requestCode == permissionRequestCode) {

            val request = pendingWebPermission
            pendingWebPermission = null

            val allGranted =
                grantResults.isNotEmpty() &&
                grantResults.all {
                    it == PackageManager.PERMISSION_GRANTED
                }

            if (request != null && allGranted) {

                val allowed = request.resources.filter {
                    it == PermissionRequest.RESOURCE_AUDIO_CAPTURE ||
                    it == PermissionRequest.RESOURCE_VIDEO_CAPTURE
                }

                request.grant(allowed.toTypedArray())

            } else {
                request?.deny()
            }
        }
    }

    // --------------------------------------------------
    // BACK BUTTON AND CLEANUP
    // --------------------------------------------------

    @Deprecated("Legacy Android back callback")
    override fun onBackPressed() {

        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {

        if (::web.isInitialized) {
            web.destroy()
        }

        super.onDestroy()
    }
}
