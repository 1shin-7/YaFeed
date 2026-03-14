package com.w57736e.yafeed.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast

object BrowserHelper {
    const val BROWSER_DEFAULT = "default"
    const val BROWSER_SAMSUNG = "samsung"
    const val BROWSER_CHROME = "chrome"

    private val BROWSER_PACKAGES = mapOf(
        BROWSER_SAMSUNG to "com.sec.android.app.sbrowser",
        BROWSER_CHROME to "com.android.chrome"
    )

    fun detectAvailableBrowsers(context: Context): List<String> {
        val browsers = mutableListOf<String>()
        val pm = context.packageManager

        // Check if default browser/WebView is available
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
            if (pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()) {
                browsers.add(BROWSER_DEFAULT)
            }
        } catch (e: Exception) {
            // No default browser
        }

        // Check specific browser apps
        BROWSER_PACKAGES.forEach { (type, packageName) ->
            try {
                pm.getPackageInfo(packageName, 0)
                browsers.add(type)
            } catch (e: PackageManager.NameNotFoundException) {
                // Browser not installed
            }
        }

        return browsers
    }

    fun openInBrowser(context: Context, url: String, browserType: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                if (browserType != BROWSER_DEFAULT) {
                    BROWSER_PACKAGES[browserType]?.let { setPackage(it) }
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
