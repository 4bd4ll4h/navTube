package com.abd4ll4h.navtube.DataFetch.scraper

import android.util.Log


class Element(private val web: WebViewHandler, private val elementLocator: String) {
    fun click() {
        web.run("javascript:$elementLocator.click();void(0);")
    }

    var text: String
        get() = web.run2("javascript:window.HtmlViewer.processContent($elementLocator.innerText);").toString()
        set(text) {
            val t = "javascript:$elementLocator.value='$text';void(0);"

            web.run(t)
        }
    val value: String
        get() = web.run2("javascript:window.HtmlViewer.processContent($elementLocator.value);").toString()
    val name: String
        get() = web.run2("javascript:window.HtmlViewer.processContent($elementLocator.name);").toString()
    val title: String
        get() = web.run2("javascript:window.HtmlViewer.processContent($elementLocator.title);").toString()

}
