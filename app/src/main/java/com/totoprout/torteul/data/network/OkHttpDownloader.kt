package com.totoprout.torteul.data.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.downloader.Request as NewPipeRequest
import org.schabi.newpipe.extractor.downloader.Response as NewPipeResponse
import java.io.IOException

class OkHttpDownloader(private val client: OkHttpClient) : Downloader() {
    override fun execute(request: NewPipeRequest): NewPipeResponse {
        val method = request.httpMethod()
        val url = request.url()
        val data = request.dataToSend()
        val headers = request.headers()

        val contentType = headers["Content-Type"]?.firstOrNull() ?: "application/octet-stream"
        val mediaType = contentType.toMediaTypeOrNull()

        val body: RequestBody? = when (method) {
            "POST", "PUT", "PATCH" -> data?.toRequestBody(mediaType) ?: "".toRequestBody(mediaType)
            else -> null
        }

        val builder = Request.Builder()
            .url(url)
            .method(method, body)

        headers.forEach { (name, values) ->
            values.forEach { value ->
                builder.addHeader(name, value)
            }
        }

        val okHttpRequest = builder.build()

        client.newCall(okHttpRequest).execute().use { response ->
            if (response.code == 429) {
                throw ReCaptchaException("YouTube is rate-limiting requests", url)
            }
            return NewPipeResponse(
                response.code,
                response.message,
                response.headers.toMultimap(),
                response.body?.string(),
                request.url()
            )
        }
    }
}
