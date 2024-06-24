package com.example.andoridtestv2

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URISyntaxException
import com.example.andoridtestv2.utils.ClientType

class WebActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var myWebView: WebView
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        myWebView = findViewById(R.id.webView1)
        myWebView.webViewClient = WebViewClient() // 새 창 띄우기 않기
        configurationWebView(myWebView)
        /*myWebView.loadUrl("https://stgpg-contact.kcp.co.kr/api/mobile/pay/mobile_sample/trade_reg")*/
        myWebView.loadUrl("https://testpay.kcp.co.kr/support/hdw/mgkim/cjoshop_qpay_20240530/sample/index.html")
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun configurationWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.builtInZoomControls = true
        webView.settings.setSupportZoom(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // http -> https 호출 허용.
            webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // 서드파티 쿠키 허용.
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        myWebView.webChromeClient = WebChromeClient()
        myWebView.webViewClient = PaycoPaymentWebViewClient()
    }
    private inner class PaycoPaymentWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String) =
            when (ClientType.getClientType(url)) {
                // about:blank, javascript: 로 시작하는 url을 처리
                ClientType.BLANK,
                ClientType.JAVASCRIPT -> {
                    true
                }

                // http, https 프로토콜로 시작하는 일반적인 웹 주소를 처리
                ClientType.WEB -> {
                    false
                }

                // url에 대한 처리
                else -> {
                    handleAppUrl(url)
                }
            }
    }

    private fun handleAppUrl(url: String): Boolean {
        // 전화 걸기 처리
        if (handleTelShouldOverrideUrlLoading(url)) {
            return true
        }

        // intent 처리
        if (handleIntentShouldOverrideUrlLoading(url)) {
            return true
        }

        // Play Store url 처리
        if (storeShouldOverrideUrlLoading(url)) {
            return true
        }

        // 기타 url 처리
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            return true
        } catch (e: Exception) {
            // 어플이 설치 안되어 있을경우 오류 발생. 해당 부분은 업체에 맞게 구현
            Toast.makeText(this, "해당 어플을 설치해 주세요.", Toast.LENGTH_LONG).show();
        }
        return false
    }
    /**
     * 전화걸기 uri 처리 - ARS 인증을 위한 전화 연결 등
     * @param url     처리하고자 하는 url
     * @return        uri 처리 여부. WebViewClient.shouldOverrideUrlLoading가 반환할 값을 반환
     */
    private fun handleTelShouldOverrideUrlLoading(url: String): Boolean {
        if (url.toLowerCase().startsWith("tel:")) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            return true
        }

        return false
    }

    /**
     * intent 프로토콜로 시작하는 uri를 처리하는 메소드로 앱 설치 여부를 확인하고, 설치된 앱이 없을 경우
     * 플레이 스토어로 이동합니다.
     * @param url   처리할 uri
     * @return      url 처리 결과
     */
    private fun handleIntentShouldOverrideUrlLoading(url: String): Boolean {
        if (url.startsWith("intent")) {

            val schemeIntent = try {
                Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            } catch (uriEx: URISyntaxException) {
                Log.e(TAG, "URISyntaxException=[" + uriEx.message + "]")
                return false
            }

            try {
                startActivity(schemeIntent)
                return true
            } catch (actNotEx: ActivityNotFoundException) {
                // 처리할 수 없는 패키지일 경우 플레이 스토어로 이동.
                // 단, intent의 getPackage() 메소드로 패키지 정보가 가져올 수 있을 때에만 플레이 스토어로 이동할 수 있습니다.
                val packageName = schemeIntent.getPackage()
                if (!packageName.isNullOrEmpty()) {
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    startActivity(marketIntent)
                    return true
                }
            }
        }

        return false
    }

    /**
     * market 프로토콜로 시작하는 url의 처리. 플레이스토어 이동으로 사용하는 url을 처리합니다.
     * @param url   처리할 url
     * @return      url 처리 결과
     */
    private fun storeShouldOverrideUrlLoading(url: String): Boolean {
        if (url.startsWith("market")) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            return true
        }

        return false
    }

}