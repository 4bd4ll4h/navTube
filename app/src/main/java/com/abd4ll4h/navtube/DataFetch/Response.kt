package com.abd4ll4h.navtube.DataFetch

import androidx.annotation.Nullable



    class Response<T> private constructor(
        val status: Status, @field:Nullable @param:Nullable val data: T,
        @field:Nullable @param:Nullable val message: String?
    ) {
        enum class Status {
            SUCCESS, ERROR, LOADING
        }

        companion object {
            fun <T> success(data: T): Response<T> {
                return Response(Status.SUCCESS, data, null)
            }

            fun <T> error(msg: String?, @Nullable data: T): Response<T> {
                return Response(Status.ERROR, data, msg)
            }

            fun <T> loading(@Nullable data: T): Response<T> {
                return Response(Status.LOADING, data, null)
            }
        }
    }
