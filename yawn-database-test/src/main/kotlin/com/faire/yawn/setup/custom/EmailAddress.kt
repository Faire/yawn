package com.faire.yawn.setup.custom

import com.faire.yawn.YawnStringifiable
import java.io.Serializable

internal data class EmailAddress(
    val emailAddress: String,
) : Serializable, YawnStringifiable
