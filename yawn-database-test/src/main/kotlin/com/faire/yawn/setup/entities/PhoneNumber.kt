package com.faire.yawn.setup.entities

import com.faire.yawn.YawnStringifiable

@JvmInline
internal value class PhoneNumber(
    val value: String,
) : YawnStringifiable {
    init {
        require(regex.matches(value)) { "Phone number must match pattern (XXX) XXX-XXXX" }
    }

    constructor(
        areaCode: String,
        centralOfficeCode: String,
        lineNumber: String,
    ) : this("($areaCode) $centralOfficeCode-$lineNumber")

    val areaCode: String
        get() = value.substring(1, 4)

    val centralOfficeCode: String
        get() = value.substring(6, 9)

    val lineNumber: String
        get() = value.substring(10, 14)

    override fun toString(): String = value

    override fun asYawnString(): String = value
}

private val regex = Regex("""^\(\d{3}\) \d{3}-\d{4}$""")
