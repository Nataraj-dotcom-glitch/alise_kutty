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
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.util.zip.ZipInputStream
import java.security.SecureRandom

class MainActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var status: TextView

    private val pickZip = 4102
    private val micRequest = 4103

    private var pendingWebPermission: PermissionRequest? = null

    private val extensionsDir by lazy {
        File(filesDir, "extensions").apply { mkdirs() }
    }

    private val gold = Color.rgb(212, 175, 55)

    private val ownerPhone = "6382241833"
    private val ownerDiscordId = "1353995912006860871"
    private val supportServerId = "1569616110318526535"
    private val supportInvite = "https://discord.gg/AeQbSFMapG"

    private val adminPrefs by lazy {
        getSharedPreferences("owner_admin_prototype", MODE_PRIVATE)
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
            setBackgroundColor(Color.rgb(8, 8, 8))
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(14, 8, 14, 8)
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Alise×Kutty  ◆"
            textSize = 19f
            setTextColor(gold)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        val owner = Button(this).apply {
            text = "OWNER"
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener { showOwnerAccess() }
        }

        val ext = Button(this).apply {
            text = "OGxISAI"
            setTextColor(Color.BLACK)
            setBackgroundColor(gold)
            setOnClickListener { showExtensions() }
        }

        bar.addView(title)
        bar.addView(owner)
        bar.addView(ext)

        status = TextView(this).apply {
            text = "BLACK & GOLD  •  WhatsApp Web + OGxISAI mic panel"
            textSize = 11f
            setTextColor(gold)
            setPadding(12, 7, 12, 7)
            setBackgroundColor(Color.rgb(25, 22, 12))
        }

        web = WebView(this).apply {

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.mediaPlaybackRequiresUserGesture = false

            // DESKTOP USER AGENT
            settings.userAgentString =
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/131.0.0.0 Safari/537.36"

            // DESKTOP VIEWPORT
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

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

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {

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

                    val allowed =
                        url.startsWith("https://web.whatsapp.com") ||
                        url.startsWith("https://www.whatsapp.com") ||
                        url.startsWith("https://whatsapp.com")

                    return !allowed
                }
            }
        }

        root.addView(bar)
        root.addView(status)

        root.addView(
            web,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(root)
    }

    private fun injectOgxIsai() {

        val js = assets.open("ogxisai/injector.js")
            .bufferedReader()
            .use { it.readText() }

        val encoded = android.util.Base64.encodeToString(
            js.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP
        )

        val loading = "file:///android_asset/ogxisai/loading.gif"
        val header = "file:///android_asset/ogxisai/header.gif"

        val wrapper =
            "(function(){" +
            "var old=document.getElementById('ogxisai-asset-marker');" +
            "if(old)return;" +
            "var m=document.createElement('script');" +
            "m.id='ogxisai-asset-marker';" +
            "m.dataset.loadingGif='$loading';" +
            "m.dataset.headerGif='$header';" +
            "(document.head||document.documentElement).appendChild(m);" +
            "var code=atob('$encoded');" +
            "(0,Function)(code)();" +
            "})();"

        web.evaluateJavascript(wrapper, null)
    }

    // OWNER ACCESS

    private fun showOwnerAccess() {

        val input = EditText(this).apply {
            hint = "Enter owner phone number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(Color.WHITE)
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)

            addView(
                TextView(this@MainActivity).apply {
                    text = "Owner access is configured for $ownerPhone"
                    setTextColor(gold)
                }
            )

            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Alise×Kutty • Owner Access")
            .setView(box)
            .setPositiveButton("Continue") { _, _ ->

                if (
                    input.text.toString().filter { it.isDigit() } == ownerPhone
                ) {
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
            "Ban phone number",
            "Unban phone number",
            "View license / ban records",
            "Support server and owner details"
        )

        AlertDialog.Builder(this)
            .setTitle("OWNER CONTROL • BLACK & GOLD")
            .setItems(options) { _, which ->

                when (which) {
                    0 -> generateLicenseKey()
                    1 -> changeLicenseStatus(true)
                    2 -> changeBanStatus(true)
                    3 -> changeBanStatus(false)
                    4 -> showAdminRecords()
                    5 -> showSupportDetails()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun generateLicenseKey() {

        val daysInput = EditText(this).apply {
            hint = "Validity in days (e.g. 30)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("30")
            setTextColor(Color.WHITE)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 0)

            addView(
                TextView(this@MainActivity).apply {
                    text = "License duration"
                    setTextColor(gold)
                }
            )

            addView(daysInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Create license")
            .setView(layout)
            .setPositiveButton("Generate") { _, _ ->

                val days = daysInput.text.toString().toLongOrNull()

                if (days == null || days !in 1..3650) {
                    Toast.makeText(
                        this,
                        "Enter 1–3650 days",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val code =
                    (10000000 + SecureRandom().nextInt(90000000)).toString()

                val records = readArray("licenses")

                records.put(
                    JSONObject()
                        .put("code", code)
                        .put("createdAt", System.currentTimeMillis())
                        .put(
                            "expiresAt",
                            System.currentTimeMillis() + days * 86_400_000L
                        )
                        .put("revoked", false)
                )

                adminPrefs.edit()
                    .putString("licenses", records.toString())
                    .apply()

                AlertDialog.Builder(this)
                    .setTitle("License generated")
                    .setMessage(
                        "8-digit key: $code\n" +
                        "Valid for $days days.\n\n" +
                        "Prototype only: this key is saved on this device."
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
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
        }

        AlertDialog.Builder(this)
            .setTitle(if (revoke) "Revoke license" else "Restore license")
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->

                val key = input.text.toString()
                val records = readArray("licenses")
                var found = false

                for (i in 0 until records.length()) {

                    val item = records.optJSONObject(i) ?: continue

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
                    if (found) {
                        "License ${if (revoke) "revoked" else "restored"}"
                    } else {
                        "Key not found on this device"
                    },
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeBanStatus(ban: Boolean) {

        val input = EditText(this).apply {
            hint = "Phone number with country code"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(Color.WHITE)
        }

        AlertDialog.Builder(this)
            .setTitle(if (ban) "Ban number" else "Unban number")
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->

                val number = input.text.toString().filter { it.isDigit() }

                if (number.length < 8) {
                    Toast.makeText(
                        this,
                        "Enter a valid phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val bans = readArray("bans")
                val updated = JSONArray()
                var existed = false

                for (i in 0 until bans.length()) {

                    val value = bans.optString(i)

                    if (value == number) {
                        existed = true
                    } else {
                        updated.put(value)
                    }
                }

                if (ban && !existed) {
                    updated.put(number)
                }

                adminPrefs.edit()
                    .putString("bans", updated.toString())
                    .apply()

                Toast.makeText(
                    this,
                    if (ban) {
                        "Number added to local ban list"
                    } else {
                        "Number removed from local ban list"
                    },
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun readArray(key: String): JSONArray =
        runCatching {
            JSONArray(adminPrefs.getString(key, "[]"))
        }.getOrElse {
            JSONArray()
        }

    private fun showAdminRecords() {

        val licenses = readArray("licenses")
        val bans = readArray("bans")

        val lines = StringBuilder("LICENSES\n")

        for (i in 0 until licenses.length()) {

            val item = licenses.optJSONObject(i) ?: continue

            lines.append(item.optString("code"))
                .append(" • ")
                .append(
                    if (item.optBoolean("revoked")) {
                        "REVOKED"
                    } else if (
                        item.optLong("expiresAt") < System.currentTimeMillis()
                    ) {
                        "EXPIRED"
                    } else {
                        "ACTIVE"
                    }
                )
                .append('\n')
        }

        lines.append("\nBANNED NUMBERS\n")

        for (i in 0 until bans.length()) {
            lines.append(bans.optString(i)).append('\n')
        }

        AlertDialog.Builder(this)
            .setTitle("Local records")
            .setMessage(lines.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSupportDetails() {

        AlertDialog.Builder(this)
            .setTitle("Support & owner")
            .setMessage(
                "Owner phone: $ownerPhone\n" +
                "Owner Discord ID: $ownerDiscordId\n" +
                "Server ID: $supportServerId\n" +
                "Support: $supportInvite\n\n" +
                "These identifiers are public app configuration."
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

    // OGxISAI

    private fun showExtensions() {

        val items = arrayOf(
            "Open OGxISAI microphone panel (on WhatsApp Web)",
            "Import CSS theme ZIP",
            "About compatibility"
        )

        AlertDialog.Builder(this)
            .setTitle("Alise×Kutty • Black & Gold")
            .setItems(items) { _, which ->

                when (which) {

                    0 -> {
                        injectOgxIsai()

                        Toast.makeText(
                            this,
                            "OGxISAI injection requested. Open a WhatsApp Web call to test.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    1 -> openZipPicker()

                    2 -> {
                        AlertDialog.Builder(this)
                            .setTitle("Compatibility")
                            .setMessage(
                                "OGxISAI is a browser extension. " +
                                "This build embeds its injector into WhatsApp Web. " +
                                "Processed audio reaching the other caller is not guaranteed. " +
                                "Instagram and ShareChat extension targets are not included."
                            )
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openZipPicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"

            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "application/zip",
                    "application/x-zip-compressed",
                    "application/octet-stream"
                )
            )
        }

        startActivityForResult(intent, pickZip)
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
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

                Toast.makeText(
                    this,
                    "CSS theme imported",
                    Toast.LENGTH_LONG
                ).show()

                applyEnabledStyles()

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
                            "Extension file too large"
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
            "manifest entry must be style.css"
        }

        if (manifest.optString("name").isBlank()) {
            manifest.put("name", id)
        }

        File(target, "manifest.json")
            .writeText(manifest.toString(2))

        File(target, "enabled.flag").writeText("enabled")
    }

    private fun applyEnabledStyles() {

        val css = extensionsDir.listFiles()
            ?.filter {
                it.isDirectory && File(it, "enabled.flag").exists()
            }
            ?.mapNotNull { dir ->
                runCatching {
                    File(dir, "style.css").readText()
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
        if (::web.isInitialized) {
            web.saveState(outState)
        }
    }

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
