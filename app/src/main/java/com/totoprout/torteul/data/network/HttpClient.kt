package com.totoprout.torteul.data.network

import okhttp3.OkHttpClient

// Same desktop Firefox UA as NewPipe's DownloaderImpl; OkHttp's default "okhttp/x" stands out.
private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:140.0) Gecko/20100101 Firefox/140.0"

fun createHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
        chain.proceed(
            if (request.header("User-Agent") != null) request
            else request.newBuilder().header("User-Agent", USER_AGENT).build()
        )
    }
    .build()
