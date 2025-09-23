package com.faire.yawn.setup.entities

import java.io.Serializable

internal data class YawnId<T : Any>(
    val id: Long,
) : Serializable
