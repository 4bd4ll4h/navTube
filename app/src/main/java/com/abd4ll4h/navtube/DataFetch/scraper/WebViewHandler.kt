package com.abd4ll4h.navtube.DataFetch.scraper

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.*
import com.abd4ll4h.navtube.DataFetch.ResponseWrapper
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("SetJavaScriptEnabled,unused")
class WebViewHandler(val context: Context) : WebViewClient() {
    private val web: WebView
    private var urlToken = CompletableDeferred<ResponseWrapper<String>>()

    private var baseUrl: String = KeyText.getBaseUrl(null)

    @Volatile
    private var htmlBool = false
    private var Html: String? = null
    var loadTime: Long = 0
    val userAgent: String

    @Volatile
    private var gotElementText = true
    private var elementText: String? = null
    var isFirstLoad = true
    private fun updateBaseUrl() {
        baseUrl = KeyText.getBaseUrl(baseUrl)
    }

    init {
        WebView.enableSlowWholeDocumentDraw()
        web = WebView(context)
        web.settings.javaScriptEnabled = true
        web.settings.blockNetworkImage = true
        userAgent = web.settings.userAgentString
        web.settings.loadsImagesAutomatically = false
        web.addJavascriptInterface(JSInterface(context), "HtmlViewer")
        web.webViewClient = this
        loadUrl()


    }

    private fun loadUrl() = web.loadUrl(baseUrl)

    suspend fun getTokenUrl(): ResponseWrapper<String> {

        return urlToken.await()
    }

     suspend fun resetWebView() {


            withContext(Dispatchers.Main){
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                web.clearCache(true)
                web.clearHistory()
                updateBaseUrl()
                urlToken.cancel()
                loadTime = 0
                isFirstLoad = true
                urlToken = CompletableDeferred<ResponseWrapper<String>>()
                loadUrl()
            }

    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (request != null) {
            if (request.url.toString().contains("https://cse.google.com/cse/element/v1")) {
                complete(ResponseWrapper.success(request.url.toString()))

            }
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.i("check_tag", "message: " + (Date().time - loadTime).toString())
        if (Date().time - loadTime >= 100 && isFirstLoad) {
            val el1: Element = findElementByTagName("input")
            val el2: Element = findElementByTagName("button")
            el1.text = KeyText.keyText
            el2.click()
            isFirstLoad = false
            loadTime = Date().time

        } else {

            if (Date().time - loadTime >= 300) {
                Log.i("check_tag", "IN: " + url.toString())
                complete(ResponseWrapper.error(KeyText.tokenErrorMassage, "no token"))
            }
        }


    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        complete(ResponseWrapper.error(KeyText.tokenErrorMassage, "no token"))
        super.onReceivedError(view, request, error)
    }

    @JvmOverloads
    fun findElementByTagName(tagName: String, id: Int = 0): Element {
        return Element(
            this,
            "document.getElementsByTagName('$tagName')[$id]"
        )
    }

    fun run2(task: String?): String? {
        while (!gotElementText) {
        }
        elementText = null
        gotElementText = false
        web.evaluateJavascript(
            task!!
        ) { gotElementText = true }
        while (!gotElementText) {
        }
        return elementText
    }

    fun run(task: String?) {
        web.loadUrl(task!!)
    }

    private inner class JSInterface internal constructor(private val ctx: Context) {
        @JavascriptInterface
        fun showHTML(html: String?) {
            Html = html
            htmlBool = false
        }

        @JavascriptInterface
        fun processContent(elText: String?) {
            elementText = elText
            gotElementText = true
        }
    }

    private fun complete(value: ResponseWrapper<String>) {
        urlToken.complete(value)
    }

}
