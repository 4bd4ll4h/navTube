package com.abd4ll4h.navtube.DataFetch.scraper

import android.net.Uri

interface UrlCallBack {
    fun  getTokenUrl(url: Uri)
}