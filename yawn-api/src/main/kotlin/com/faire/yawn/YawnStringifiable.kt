package com.faire.yawn

/**
 * Marks a type whose database representation is text, making it eligible for pattern matching.
 *
 * Yawn cannot always work this out on its own: a value class wrapping a `String` is visible to the processor, but a
 * type Hibernate maps through an `AttributeConverter` is not, and the converter may not even be declared on the
 * property. Implementing this interface is how a type states that its column holds text.
 *
 * ```
 * value class PhoneNumber(val value: String) : YawnStringifiable
 * ```
 *
 * The claim is not verified, so only add this to a type whose column really is text. For a column that cannot
 * implement it, for example one owned by another library, match it as text instead via
 * [YawnDef.YawnColumnDef.raw].
 */
interface YawnStringifiable
