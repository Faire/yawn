package com.faire.yawn.setup.entities

@JvmInline
internal value class PhoneNumber(
    val value: String,
) {
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
}

private val regex = Regex("""^\(\d{3}\) \d{3}-\d{4}$""")
