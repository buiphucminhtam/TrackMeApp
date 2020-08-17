package com.fossil.trackme.utils

import com.squareup.picasso.LruCache
import com.squareup.picasso.Picasso
import com.fossil.trackme.AppApplication
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PicassoUtil {
    private val picassoMap = hashMapOf<String, Picasso>()

    private const val DEFAULT = "Default"
    private const val CACHE_SIZE = 20 * 1024 * 1024

    const val HOME = "Home"

    fun get(key: String = DEFAULT): Picasso {
        return if (picassoMap.containsKey(key))
            picassoMap[key]!!
        else
            createPicassoInstance(key)
    }

    private fun createPicassoInstance(key: String): Picasso {
        val instance = Picasso.Builder(AppApplication.INSTANCE)
            .executor(createExecutor())
            .memoryCache(LruCache(CACHE_SIZE))
            .build()
        picassoMap[key] = instance
        return instance
    }

    private fun createExecutor(thread: Int = 2): ExecutorService {
        return Executors.newFixedThreadPool(thread)
    }
}