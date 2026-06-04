package com.sentinela.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.sentinela.BuildConfig

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FireMapView(
    days: Int,
    modifier: Modifier = Modifier,
) {
    val mapUrl = remember(days) {
        val base = BuildConfig.SENTINELA_API_URL.trimEnd('/')
        "$base/mobile-map?days=$days&v=3"
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.parseColor("#aad3df"))
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    blockNetworkImage = false
                    useWideViewPort = true
                    loadWithOverviewMode = false
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    userAgentString = "Sentinela/1.0 (${context.packageName}; Android)"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            "if (typeof map !== 'undefined' && map) map.invalidateSize(true);",
                            null,
                        )
                    }
                }
                loadUrl(mapUrl)
            }
        },
        update = { webView ->
            if (webView.url != mapUrl) {
                webView.loadUrl(mapUrl)
            }
        },
    )
}
