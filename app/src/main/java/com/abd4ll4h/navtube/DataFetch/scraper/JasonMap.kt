package com.abd4ll4h.navtube.DataFetch.scraper

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class JasonMap(
    @SerializedName("cursor") @Expose var cursor: Cursor? = null,
    @SerializedName("results") @Expose var results: List<Result>? = null
) {

    //data class CseImage(@SerializedName("src") @Expose var src: String? = null)


    data class Cursor(
        @SerializedName("currentPageIndex") @Expose var currentPageIndex: Int? = null,
        @SerializedName("resultCount") @Expose var resultCount: String? = null,
        @SerializedName("pages") @Expose var pages: List<Page>? = null
    )

    data class Page(
        @SerializedName("label") @Expose var label: Int? = null,
        @SerializedName("start") @Expose var start: String? = null
    )

    data class Person(
        @SerializedName("name") @Expose var name: String? = null,
        @SerializedName("url") @Expose var url: String? = null

    )

    data class Result(
       // @SerializedName("titleNoFormatting") @Expose var titleNoFormatting: String? = null,
        // @SerializedName("unescapedUrl") @Expose var unescapedUrl: String? = null,
        @SerializedName("richSnippet") @Expose var richSnippet: RichSnippet,
        @SerializedName("contentNoFormatting") @Expose var contentNoFormatting: String? = null
    )

    data class RichSnippet(
        //@SerializedName("cseImage") @Expose var cseImage: CseImage? = null,
        @SerializedName("person") @Expose var person: Person?,
        @SerializedName("videoobject") @Expose var videoobject: Videoobject?,
        @SerializedName("cseThumbnail") @Expose var cseThumbnail: CseThumbnail

    )
data class CseThumbnail(@SerializedName("src") @Expose var channelThumbnail: String,
                        @SerializedName("width") @Expose var width: Int,
                        @SerializedName("height") @Expose var height: Int,)
    data class Videoobject(
       // @SerializedName("embedurl") @Expose var embedurl: String? = null,
        @SerializedName("isfamilyfriendly") @Expose var isfamilyfriendly: Boolean,
        @SerializedName("uploaddate") @Expose var uploaddate: String,
        @SerializedName("videoid") @Expose var videoid: String,
        @SerializedName("url") @Expose var url: String,
        @SerializedName("duration") @Expose var duration: String,
        @SerializedName("unlisted") @Expose var unlisted: Boolean,
        @SerializedName("name") @Expose var name: String,
        @SerializedName("paid") @Expose var paid: Boolean,
      //  @SerializedName("width") @Expose var width: String? = null,
       // @SerializedName("regionsallowed") @Expose var regionsallowed: String? = null,
        @SerializedName("genre") @Expose var genre: String,
        @SerializedName("interactioncount") @Expose var interactioncount: Int,
        @SerializedName("channelid") @Expose var channelid: String,
        @SerializedName("datepublished") @Expose var datepublished: String,
        @SerializedName("thumbnailurl") @Expose var thumbnailurl: String,
        //@SerializedName("height") @Expose var height: String? = null,
        @SerializedName("description") @Expose var description: String
        )
}