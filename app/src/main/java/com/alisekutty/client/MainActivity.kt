package com.alisekutty.client

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.SecureRandom
import java.util.zip.ZipInputStream

class MainActivity : Activity() {

    private lateinit var web: WebView

    private val gold = Color.rgb(212, 175, 55)
    private val pickZip = 4102
    private val micRequest = 4103

    private var pendingWebPermission: PermissionRequest? = null

    private val ownerPhone = "6382241833"
    private val ownerDiscordId = "1353995912006860871"
    private val supportServerId = "1569616110318526535"
    private val supportInvite = "https://discord.gg/AeQbSFMapG"

    private val adminPrefs by lazy {
        getSharedPreferences("owner_admin_prototype", MODE_PRIVATE)
    }

    private val extensionsDir by lazy {
        File(filesDir, "extensions").apply { mkdirs() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CookieManager.getInstance().setAcceptCookie(true)

        buildScreen()

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState)
        } else {
            web.loadUrl("https://web.whatsapp.com/")
        }
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Alise×Kutty ◆"
            textSize = 20f
            setTextColor(gold)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        val owner = Button(this).apply {
            text = "OWNER"
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener { showOwnerAccess() }
        }

        val extension = Button(this).apply {
            text = "OGxISAI"
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener { showExtensions() }
        }

        bar.addView(title)
        bar.addView(owner)
        bar.addView(extension)

        val status = TextView(this).apply {
            text = "BLACK & GOLD • WhatsApp Web + OGxISAI"
            textSize = 11f
            setTextColor(gold)
            setPadding(12, 8, 12, 8)
            setBackgroundColor(Color.rgb(25, 22, 12))
        }

        web = WebView(this).apply {

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false

            // DESKTOP MODE
            settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/131.0.0.0 Safari/537.36"

            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            // DISABLE ZOOM
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false

            CookieManager.getInstance()
                .setAcceptThirdPartyCookies(this, true)

            webChromeClient = object : WebChromeClient() {

                override fun onPermissionRequest(request: PermissionRequest) {

                    runOnUiThread {

                        val wantsAudio =
                            request.resources.contains(
                                PermissionRequest.RESOURCE_AUDIO_CAPTURE
                            )

                        if (
                            wantsAudio &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.RECORD_AUDIO
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            pendingWebPermission = request

                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                micRequest
                            )
                        } else {
                            val allowed = request.resources.filter {
                                it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
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

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    if (url?.startsWith("https://web.whatsapp.com") == true) {
                        applyEnabledStyles()
                        injectOgxIsai()
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ): Boolean {

                    return !(
                        url.startsWith("https://web.whatsapp.com") ||
                        url.startsWith("https://www.whatsapp.com") ||
                        url.startsWith("https://whatsapp.com")
                    )
                }
            }
        }

        root.addView(bar)
        root.addView(status)
        root.addView(web, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
    }

    // OWNER LOGIN
    // This phone number is ONLY for the owner panel.
    // It is not used for WhatsApp login.

    private fun showOwnerAccess() {

        val input = EditText(this).apply {
            hint = "Enter owner phone number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(Color.BLACK)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)

            addView(TextView(this@MainActivity).apply {
                text = "Owner access verification"
                setTextColor(gold)
            })

            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Alise×Kutty • Owner Access")
            .setView(layout)
            .setPositiveButton("CONTINUE") { _, _ ->

                val entered = input.text.toString().filter { it.isDigit() }

                if (entered == ownerPhone) {
                    showOwnerDashboard()
                } else {
                    Toast.makeText(
                        this,
                        "Access denied • Owner only",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showOwnerDashboard() {

        val options = arrayOf(
            "Generate license key",
            "Revoke license",
            "Restore license",
            "Ban phone number",
            "Unban phone number",
            "View records",
            "Owner and support details"
        )

        AlertDialog.Builder(this)
            .setTitle("OWNER CONTROL • BLACK & GOLD")
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
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun generateLicenseKey() {

        val input = EditText(this).apply {
            hint = "Validity in days"
            setText("30")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.BLACK)
        }

        AlertDialog.Builder(this)
            .setTitle("Generate license")
            .setView(input)
            .setPositiveButton("GENERATE") { _, _ ->

                val days = input.text.toString().toLongOrNull()

                if (days == null || days !in 1..3650) {
                    Toast.makeText(this, "Enter 1–3650 days", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val key = (10000000 + SecureRandom().nextInt(90000000)).toString()

                val records = readArray("licenses")

                records.put(
                    JSONObject()
                        .put("code", key)
                        .put("createdAt", System.currentTimeMillis())
                        .put("expiresAt", System.currentTimeMillis() + days * 86400000L)
                        .put("revoked", false)
                )

                adminPrefs.edit()
                    .putString("licenses", records.toString())
                    .apply()

                AlertDialog.Builder(this)
                    .setTitle("LICENSE GENERATED")
                    .setMessage("License: $key\nValidity: $days days")
                    .setPositiveButton("OK", null)
                    .show()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun changeLicenseStatus(revoke: Boolean) {

        val input = EditText(this).apply {
            hint = "Enter 8-digit license"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.BLACK)
        }

        AlertDialog.Builder(this)
            .setTitle(if (revoke) "Revoke license" else "Restore license")
            .setView(input)
            .setPositiveButton("APPLY") { _, _ ->

                val records = readArray("licenses")
                var found = false

                for (i in 0 until records.length()) {
                    val item = records.optJSONObject(i) ?: continue

                    if (item.optString("code") == input.text.toString()) {
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
                    if (found) "Updated" else "License not found",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun changeBanStatus(ban: Boolean) {

        val input = EditText(this).apply {
            hint = "Phone number with country code"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(Color.BLACK)
        }

        AlertDialog.Builder(this)
            .setTitle(if (ban) "Ban number" else "Unban number")
            .setView(input)
            .setPositiveButton("APPLY") { _, _ ->

                val number = input.text.toString().filter { it.isDigit() }

                if (number.length < 8) {
                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val old = readArray("bans")
                val updated = JSONArray()

                for (i in 0 until old.length()) {
                    val value = old.optString(i)
                    if (value != number) updated.put(value)
                }

                if (ban && (0 until old.length()).none {
                    old.optString(it) == number
                }) {
                    updated.put(number)
                }

                adminPrefs.edit()
                    .putString("bans", updated.toString())
                    .apply()

                Toast.makeText(
                    this,
                    if (ban) "Number banned locally" else "Number unbanned locally",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun readArray(key: String): JSONArray {
        return runCatching {
            JSONArray(adminPrefs.getString(key, "[]"))
        }.getOrElse {
            JSONArray()
        }
    }

    private fun showAdminRecords() {

        val licenses = readArray("licenses")
        val bans = readArray("bans")

        val text = StringBuilder("LICENSES\n")

        for (i in 0 until licenses.length()) {
            val item = licenses.optJSONObject(i) ?: continue

            val state = when {
                item.optBoolean("revoked") -> "REVOKED"
                item.optLong("expiresAt") < System.currentTimeMillis() -> "EXPIRED"
                else -> "ACTIVE"
            }

            text.append(item.optString("code"))
                .append(" • ")
                .append(state)
                .append("\n")
        }

        text.append("\nBANNED NUMBERS\n")

        for (i in 0 until bans.length()) {
            text.append(bans.optString(i)).append("\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Owner records")
            .setMessage(text.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSupportDetails() {

        AlertDialog.Builder(this)
            .setTitle("OWNER DETAILS")
            .setMessage(
                "Owner: $ownerPhone\n" +
                "Discord ID: $ownerDiscordId\n" +
                "Server ID: $supportServerId\n" +
                "Support: $supportInvite\n\n" +
                "Owner access is a local prototype."
            )
            .setPositiveButton("OPEN DISCORD") { _, _ ->
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(supportInvite))
                )
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    // OGxISAI

    private fun injectOgxIsai() {

        val js = assets.open("ogxisai/injector.js")
            .bufferedReader()
            .use { it.readText() }

        val encoded = android.util.Base64.encodeToString(
            js.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )

        val wrapper =
            "(function(){" +
            "var old=document.getElementById('ogxisai-asset-marker');" +
            "if(old)return;" +
            "var m=document.createElement('script');" +
            "m.id='ogxisai-asset-marker';" +
            "(document.head||document.documentElement).appendChild(m);" +
            "var code=atob('$encoded');" +
            "(0,Function)(code)();" +
            "})();"

        web.evaluateJavascript(wrapper, null)
    }

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
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    1 -> openZipPicker()

                    2 -> AlertDialog.Builder(this)
                        .setTitle("Compatibility")
                        .setMessage(
                            "OGxISAI is injected into WhatsApp Web. " +
                            "WhatsApp Web and Android WebView may restrict " +
                            "some microphone and audio processing features."
                        )
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    // CSS ZIP IMPORT

    private fun openZipPicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }

        startActivityForResult(intent, pickZip)
    }

    @Deprecated("Deprecated Android API retained for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickZip && resultCode == Activity.RESULT_OK) {

            val uri = data?.data ?: return

            try {
                importCssZip(uri)
                applyEnabledStyles()

                Toast.makeText(
                    this,
                    "CSS theme imported",
                    Toast.LENGTH_LONG
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

        val target = File(
            extensionsDir,
            "ext_${System.currentTimeMillis()}"
        ).apply { mkdirs() }

        var manifestFound = false
        var cssFound = false

        contentResolver.openInputStream(uri).use { raw ->

            requireNotNull(raw) { "Cannot open ZIP" }

            ZipInputStream(raw).use { zip ->

                var entry = zip.nextEntry

                while (entry != null) {

                    if (!entry.isDirectory) {

                        val name = entry.name.substringAfterLast('/')

                        require(
                            name == "manifest.json" || name == "style.css"
                        ) {
                            "Only manifest.json and style.css are accepted"
                        }

                        val bytes = zip.readBytes()

                        require(bytes.size <= 512 * 1024) {
                            "File too large"
                        }

                        File(target, name).writeBytes(bytes)

                        if (name == "manifest.json") manifestFound = true
                        if (name == "style.css") cssFound = true
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
            "Invalid CSS entry"
        }

        File(target, "enabled.flag").writeText("enabled")
    }

    private fun applyEnabledStyles() {

        val css = extensionsDir.listFiles()
            ?.filter {
                it.isDirectory && File(it, "enabled.flag").exists()
            }
            ?.mapNotNull {
                runCatching {
                    File(it, "style.css").readText()
                }.getOrNull()
            }
            ?.joinToString("\n") ?: ""

        val encoded = android.util.Base64.encodeToString(
            css.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )

        web.evaluateJavascript(
            "(function(){" +
            "var c=atob('$encoded');" +
            "var s=document.getElementById('alisekutty-extension-css');" +
            "if(!s){" +
            "s=document.createElement('style');" +
            "s.id='alisekutty-extension-css';" +
            "document.head.appendChild(s);" +
            "}" +
            "s.textContent=c;" +
            "})();",
            null
        )
    }

    // MICROPHONE PERMISSION

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

        if (requestCode == micRequest) {

            val request = pendingWebPermission
            pendingWebPermission = null

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                request != null
            ) {
                val allowed = request.resources.filter {
                    it == PermissionRequest.RESOURCE_AUDIO_CAPTURE
                }

                if (allowed.isNotEmpty()) {
                    request.grant(allowed.toTypedArray())
                } else {
                    request.deny()
                }
            } else {
                request?.deny()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::web.isInitialized) web.saveState(outState)
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
