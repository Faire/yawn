package com.faire.yawn

/**
 * A type whose database representation is text, making it eligible for pattern matching.
 *
 * Yawn cannot work this out on its own: a value class wrapping a `String` is visible to the processor, but a type
 * Hibernate maps through an `AttributeConverter` is not, and the converter may not even be declared on the property.
 * Implementing this is how a type states that its column holds text, and says what that text is.
 *
 * ```
 * value class PhoneNumber(val value: String) : YawnStringifiable {
 *     override fun asYawnString(): String = value
 * }
 * ```
 *
 * Neither part of that is verified, so only implement this where the column really is text and [asYawnString] really
 * is what the database stores. Getting either half wrong does not raise anything: the query is still valid SQL, and
 * databases will happily compare a non-text column against a pattern, so it simply matches on the wrong text and can
 * return the wrong rows.
 */
interface YawnStringifiable {
    /**
     * The text this value is stored as, which is what patterns are matched against.
     *
     * Yawn builds the pattern from this and binds it as a `String` against the column, so it must be the database
     * representation rather than anything decorated for display.
     */
    fun asYawnString(): String
}
