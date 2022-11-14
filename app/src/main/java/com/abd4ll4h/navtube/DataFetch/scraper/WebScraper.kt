//package com.abd4ll4h.navtube.DataFetch.scraper
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.webkit.*
//
//import com.abd4ll4h.navtube.DataFetch.scraper.KeyText.keyText
//
//
//@SuppressLint("SetJavaScriptEnabled,unused")
//class WebScraper(private val context: Context, callBack:ApiCalls) {
//
//    private val web: WebView
//
//    @Volatile
//    private var htmlBool = false
//    private var Html: String? = null
//    var isFirstLoad = true
//    @Volatile
//    private var gotElementText = true
//    private var elementText: String? = null
//    private var URL: String? = null
//    val userAgent: String
//    fun setUserAgentToDesktop(desktop: Boolean) {
//        if (desktop) {
//            val osString = userAgent.substring(userAgent.indexOf("("), userAgent.indexOf(")") + 1)
//            web.settings.userAgentString = userAgent.replace(osString, "(X11; Linux x86_64)")
//        } else {
//            web.settings.userAgentString = userAgent
//        }
//    }
//
//
//
//    fun clearHistory() {
//        web.clearHistory()
//    }
//
//    fun clearCache() {
//        web.clearCache(true)
//    }
//
//    fun clearCookies() {
//        CookieManager.getInstance().removeAllCookies(null)
//        CookieManager.getInstance().flush()
//    }
//
//    fun clearAll() {
//        clearHistory()
//        clearCache()
//        clearCookies()
//    }
//
//    fun setLoadImages(enabled: Boolean) {
//        web.settings.blockNetworkImage = !enabled
//        web.settings.loadsImagesAutomatically = enabled
//    }
//
//    fun loadURL(URL: String?) {
//        this.URL = URL
//        web.loadUrl(URL!!)
//    }
//
//    fun getURL(): String? {
//        return web.url
//    }
//
//    fun reload() {
//        web.reload()
//    }
//
//    private inner class JSInterface internal constructor(private val ctx: Context) {
//        @JavascriptInterface
//        fun showHTML(html: String?) {
//            Html = html
//            htmlBool = false
//        }
//
//        @JavascriptInterface
//        fun processContent(elText: String?) {
//            elementText = elText
//            gotElementText = true
//        }
//    }
//
//     fun run(task: String?) {
//        web.loadUrl(task!!)
//    }
//
//    fun run2(task: String?): String? {
//        while (!gotElementText) {
//        }
//        elementText = null
//        gotElementText = false
//        web.evaluateJavascript(
//            task!!
//        ) { gotElementText = true }
//        while (!gotElementText) {
//        }
//        return elementText
//    }
//
//    //FindWebViewElement
//    fun findElementByClassName(classname: String, id: Int): Element {
//        return Element(
//            this,
//            "document.getElementsByClassName('$classname')[$id]"
//        )
//    }
//
//    fun findElementByClassName(classname: String): Element {
//        return findElementByName(classname, 0)
//    }
//
//    fun findElementById(id: String): Element {
//        return Element(this, "document.getElementById('$id')")
//    }
//
//    @JvmOverloads
//    fun findElementByName(name: String, id: Int = 0): Element {
//        return Element(this, "document.getElementsByName('$name')[$id]")
//    }
//
//    fun findElementByXpath(xpath: String): Element {
//        return Element(
//            this,
//            "document.evaluate($xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue"
//        )
//    }
//
//    fun findElementByJavaScript(javascript: String?): Element {
//        return Element(this, javascript!!)
//    }
//
//    @JvmOverloads
//    fun findElementByValue(value: String, id: Int = 0): Element {
//        return Element(
//            this,
//            "document.querySelectorAll('[value=\"$value\"]')[$id]"
//        )
//    }
//
//    @JvmOverloads
//    fun findElementByTitle(title: String, id: Int = 0): Element {
//        return Element(
//            this,
//            "document.querySelectorAll('[title=\"$title\"]')[$id]"
//        )
//    }
//
//    @JvmOverloads
//    fun findElementByTagName(tagName: String, id: Int = 0): Element {
//        return Element(
//            this,
//            "document.getElementsByTagName('$tagName')[$id]"
//        )
//    }
//
//    @JvmOverloads
//    fun findElementByType(type: String, id: Int = 0): Element {
//        return Element(
//            this,
//            "document.querySelectorAll('[type=\"$type\"]')[$id]"
//        )
//    }
//
//
//    init {
//        web = WebView(context)
//        enableSlowWholeDocumentDraw()
//        web.settings.javaScriptEnabled = true
//        web.settings.blockNetworkImage = true
//        web.settings.loadsImagesAutomatically = false
//        val jInterface: JSInterface = JSInterface(context)
//        web.addJavascriptInterface(jInterface, "HtmlViewer")
//        userAgent = web.settings.userAgentString
//        web.webViewClient = object : WebViewClient() {
//            override fun shouldInterceptRequest(
//                view: WebViewHandler?,
//                request: WebResourceRequest?
//            ): WebResourceResponse? {
//                if (request!!.url.toString().contains("https://cse.google.com/cse/element/v1")){
//                    callBack.getEngineToken(request.url)
//                }
//
//                return super.shouldInterceptRequest(view, request)
//            }
//
//            override fun onPageFinished(view: WebViewHandler?, url: String?) {
//
//                if (isFirstLoad) {
//                    val el1: Element = this@WebScraper.findElementByTagName("input")
//                    val el2: Element = this@WebScraper.findElementByTagName("button")
//                    el1.text = keyText
//                    el2.click()
//
//                    isFirstLoad=false
//                }
//                super.onPageFinished(view, url)
//
//            }
//
//        }
//
//    }
//}