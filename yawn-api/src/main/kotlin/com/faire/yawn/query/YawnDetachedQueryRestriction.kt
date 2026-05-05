package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.criteria.builder.DetachedProjectedYawnBuilder
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Subqueries

/**
 * Represents a restriction that uses a detached criteria.
 *
 * @param SOURCE The type of the source entity.
 * @param F the type being projected to by the detached criteria.
 */
interface YawnDetachedQueryRestriction<SOURCE : Any, F> : YawnQueryRestriction<SOURCE> {
    val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>

    class EqualsDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion {
            return Subqueries.propertyEq(
                property.generatePath(context),
                detachedBuilder.compile(context),
            )
        }
    }

    class EqualsAllDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyEqAll(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class NotEqualsDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyNe(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class GreaterThanDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyGt(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class GreaterThanAllDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGtAll(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class GreaterThanSomeDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGtSome(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class GreaterThanOrEqualToDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyGe(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class GreaterThanOrEqualToAllDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGeAll(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class GreaterThanOrEqualToSomeDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGeSome(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class LessThanDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyLt(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class LessThanAllDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLtAll(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class LessThanSomeDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLtSome(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class LessThanOrEqualToDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyLe(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class LessThanOrEqualToAllDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLeAll(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class LessThanOrEqualToSomeDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLeSome(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class InDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyIn(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class NotInDetached<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyNotIn(
            property.generatePath(context),
            detachedBuilder.compile(context),
        )
    }

    class ExistsDetached<SOURCE : Any, F>(
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.exists(detachedBuilder.compile(context))
    }

    class NotExistsDetached<SOURCE : Any, F>(
        override val detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.notExists(detachedBuilder.compile(context))
    }
}
