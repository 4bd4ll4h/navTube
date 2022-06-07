package com.abd4ll4h.navtube.DataFetch

import android.content.Context
import com.abd4ll4h.navtube.DataFetch.scraper.Element
import com.abd4ll4h.navtube.DataFetch.scraper.WebScraper

class Scraper(context: Context) {
    private val webScraper: WebScraper by lazy {  WebScraper(context)}
    init {

        webScraper.loadURL(" https://cse.google.com/cse?cx=52071d28291f34345")
    }

}