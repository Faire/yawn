package com.faire.yawn.project

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * A projection of a single value computed by a raw SQL expression.
 *
 * Normally produced by [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.sqlValue], which is the ergonomic way
 * in; constructing it directly is the same thing with the result type handed over explicitly.
 *
 * **You are responsible for the type-safety of the expression.** [resultType] tells Yawn how to read the value back
 * out of the result set and what to hand you, and nothing checks that the SQL actually produces it: a `VARCHAR`
 * column declared as `Long`, or a nullable expression declared non-null, will fail at the ORM boundary or hand you a
 * value that lies about its type. This is the tradeoff for being able to write SQL that Yawn has no typed API for,
 * and it is why the mapping below is an unchecked cast rather than something Yawn can verify. The rest of the
 * projection API exists precisely so that you rarely need this one.
 *
 * Note also that raw SQL cannot bind parameters, so anything interpolated into the expression is inlined into the
 * statement verbatim; never build one out of untrusted input.
 */
class YawnRawSqlProjection<SOURCE : Any, TO>(
    expression: YawnSqlScope<SOURCE>.() -> String,
    resultType: KType,
) : YawnSingleValueProjection<SOURCE, TO>(
    leaf = ProjectionLeaf.SqlValue(expression, resultType.toResultClass()),
    mapper = {
        @Suppress("UNCHECKED_CAST")
        it as TO
    },
)

/**
 * A projected value is a single column, so its type has to be a class; a `List<String>` or a type variable has no
 * column representation and there is nothing sensible to map.
 */
private fun KType.toResultClass(): KClass<*> {
    return classifier as? KClass<*> ?: error("Cannot project a SQL value to $this, as it is not a class.")
}
