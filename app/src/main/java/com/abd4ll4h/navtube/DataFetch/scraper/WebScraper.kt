package com.abd4ll4h.navtube.DataFetch.scraper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.*


@SuppressLint("SetJavaScriptEnabled,unused")
open class WebScraper(private val context: Context) {

    private val web: WebView

    @Volatile
    private var htmlBool = false
    private var Html: String? = null

    @Volatile
    private var gotElementText = true
    private var elementText: String? = null
    private var URL: String? = null
    private val userAgent: String
    fun setUserAgentToDesktop(desktop: Boolean) {
        if (desktop) {
            val osString = userAgent.substring(userAgent.indexOf("("), userAgent.indexOf(")") + 1)
            web.settings.userAgentString = userAgent.replace(osString, "(X11; Linux x86_64)")
        } else {
            web.settings.userAgentString = userAgent
        }
    }



    fun clearHistory() {
        web.clearHistory()
    }

    fun clearCache() {
        web.clearCache(true)
    }

    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    fun clearAll() {
        clearHistory()
        clearCache()
        clearCookies()
    }

    fun setLoadImages(enabled: Boolean) {
        web.settings.blockNetworkImage = !enabled
        web.settings.loadsImagesAutomatically = enabled
    }

    fun loadURL(URL: String?) {
        this.URL = URL
        web.loadUrl(URL!!)
    }

    fun getURL(): String? {
        return web.url
    }

    fun reload() {
        web.reload()
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

     fun run(task: String?) {
        web.loadUrl(task!!)
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

    //FindWebViewElement
    fun findElementByClassName(classname: String, id: Int): Element {
        return Element(
            this,
            "document.getElementsByClassName('$classname')[$id]"
        )
    }

    fun findElementByClassName(classname: String): Element {
        return findElementByName(classname, 0)
    }

    fun findElementById(id: String): Element {
        return Element(this, "document.getElementById('$id')")
    }

    @JvmOverloads
    fun findElementByName(name: String, id: Int = 0): Element {
        return Element(this, "document.getElementsByName('$name')[$id]")
    }

    fun findElementByXpath(xpath: String): Element {
        return Element(
            this,
            "document.evaluate($xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue"
        )
    }

    fun findElementByJavaScript(javascript: String?): Element {
        return Element(this, javascript!!)
    }

    @JvmOverloads
    fun findElementByValue(value: String, id: Int = 0): Element {
        return Element(
            this,
            "document.querySelectorAll('[value=\"$value\"]')[$id]"
        )
    }

    @JvmOverloads
    fun findElementByTitle(title: String, id: Int = 0): Element {
        return Element(
            this,
            "document.querySelectorAll('[title=\"$title\"]')[$id]"
        )
    }

    @JvmOverloads
    fun findElementByTagName(tagName: String, id: Int = 0): Element {
        return Element(
            this,
            "document.getElementsByTagName('$tagName')[$id]"
        )
    }

    @JvmOverloads
    fun findElementByType(type: String, id: Int = 0): Element {
        return Element(
            this,
            "document.querySelectorAll('[type=\"$type\"]')[$id]"
        )
    }


    init {
        web = WebView(context)
        WebView.enableSlowWholeDocumentDraw()
        web.settings.javaScriptEnabled = true
        web.settings.blockNetworkImage = true
        web.settings.loadsImagesAutomatically = false
        val jInterface: JSInterface = JSInterface(context)
        web.addJavascriptInterface(jInterface, "HtmlViewer")
        userAgent = web.settings.userAgentString
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                Log.i("urlChecking",request?.url.toString())
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val el1: Element = this@WebScraper.findElementByXpath("//*[@id=\"gs_tti50\"]")
                val el2: Element = this@WebScraper.findElementByXpath("/html/body/div/div[1]/div/form/table/tbody/tr/td[2]")
                el1.text = "que"
                el2.click()
                val el3: Element = this@WebScraper.findElementById("result")
                Log.i("urlChecking",el3.value)
            }
        }

    }
}