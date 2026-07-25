package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.project.YawnRawSqlProjection
import com.faire.yawn.project.YawnSqlScope
import com.faire.yawn.project.resolveOnce
import com.faire.yawn.query.YawnQuery
import kotlin.reflect.typeOf

/**
 * A context providing the DSL for Yawn projected queries.
 *
 * It supports WHERE (via [addEq], etc.), JOIN (via [join], etc.), and ORDER clauses (via [order], etc.).
 * Since this refines a projected query, it requires calling the [project] method to return the projection of the query,
 * and the [PROJECTION] type parameter is different from [T].
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param PROJECTION the type being projected to (i.e. the result of the query).
 */
class ProjectedYawnQueryScope<SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, PROJECTION : Any?>
private constructor(
    query: YawnQuery<SOURCE, T>,
) : BaseYawnQueryScope<SOURCE, T, DEF>(query),
    YawnQueryScopeWithWhere<SOURCE, T> by YawnQueryScopeWithWhereDelegate(query),
    YawnQueryScopeWithJoin<SOURCE, T> by YawnQueryScopeWithJoinDelegate(query),
    YawnQueryScopeWithOrder<SOURCE, T> by YawnQueryScopeWithOrderDelegate(query) {

    private var projectionCalled: Boolean = false

    private fun ensureUniqueProjection() {
        if (projectionCalled) {
            error("Projection already called")
        } else {
            projectionCalled = true
        }
    }

    fun project(
        projection: YawnQueryProjection<SOURCE, PROJECTION>,
    ): YawnQueryProjection<SOURCE, PROJECTION> {
        ensureUniqueProjection()
        return projection.resolveOnce()
    }

    /**
     * Projects a single value computed by a raw SQL [expression], e.g. `SUM(quantity * price)`.
     *
     * Use this instead of implementing [YawnQueryProjection] by hand when all you need is one custom SQL value.
     * The result composes anywhere an ordinary column does; see [YawnSingleValueProjection].
     *
     * Reference columns through [YawnSqlScope.sql], which substitutes the physical column backing a property,
     * already qualified by its table's alias:
     *
     * ```
     * project(sqlValue<Long> { "SUM(${books.numberOfPages.sql} * 2)" })
     * ```
     *
     * The type argument decides how the result is mapped, and should be nullable if the expression can evaluate
     * to `NULL`. As with any raw SQL projection, this is a claim Yawn takes at face value rather than something
     * it can verify: it is up to you to ensure the expression really does produce that type.
     *
     * Do not name the result yourself (no `AS total`): Yawn selects it under an alias it generates, unique within
     * the query. Note also that raw SQL cannot bind parameters, so any value in [expression] is inlined verbatim;
     * never interpolate untrusted input into it.
     *
     * To share one across queries, write a helper on this scope taking the table definition as a parameter, so that
     * nothing is captured from the query it was written in:
     *
     * ```
     * private fun BookProjectedQueryScope<Long>.doubledPages(books: BookTableDefType) =
     *     sqlValue<Long> { "SUM(${books.numberOfPages.sql} * 2)" }
     * ```
     */
    inline fun <reified TO> sqlValue(
        noinline expression: YawnSqlScope<SOURCE>.() -> String,
    ): YawnRawSqlProjection<SOURCE, TO> = YawnRawSqlProjection(expression, typeOf<TO>())

    companion object {
        internal fun <SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, PROJECTION : Any?> applyLambda(
            query: YawnQuery<SOURCE, T>,
            lambda: ProjectedYawnQueryScope<SOURCE, T, DEF, PROJECTION>.() -> Unit,
        ): ProjectedYawnQueryScope<SOURCE, T, DEF, PROJECTION> {
            return ProjectedYawnQueryScope<SOURCE, T, DEF, PROJECTION>(query).apply(lambda)
        }
    }
}
