package com.example.storagesamples.documenttree

/**
 * @author Army
 * @version V_1.0.0
 * @date 2019-12-01
 * @description
 */
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}