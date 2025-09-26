package com.faire.yawn.utils

import java.util.*

/**
 * Fake token class to test column definitions.
 */
class FakeToken<T>(val value: String) {
    companion object {
        fun <T> generate(): FakeToken<T> {
            return FakeToken(UUID.randomUUID().toString())
        }
    }
}
