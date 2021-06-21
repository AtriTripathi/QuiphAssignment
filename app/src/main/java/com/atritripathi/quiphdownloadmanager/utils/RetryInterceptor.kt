package com.atritripathi.quiphdownloadmanager.utils

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * This solution simply takes the ongoing request chain and adds a new request to it,
 * if the previous one fails while keeping track of max retry counters.
 * Simple and elegant solution. :)
 */
class RetryInterceptor(private val maxRetry: Int = 1) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var currentRetry = 0
        val request = chain.request()
        var response = chain.proceed(request)

        while (!response.isSuccessful && currentRetry < maxRetry) {
            Timber.d("Network request failed: Retry Count $currentRetry")
            currentRetry++
            response = chain.proceed(request)   // Retry the request
        }

        return response
    }
}