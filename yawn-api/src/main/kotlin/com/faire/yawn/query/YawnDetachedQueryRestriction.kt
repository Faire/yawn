package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.criteria.builder.DetachedProjectedTypeSafeCriteriaBuilder
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Subqueries

/**
 * Represents a restriction that uses a detached criteria.
 *
 * @param SOURCE The type of the source entity.
 * @param F the type being projected to by the detached criteria.
 */
interface YawnDetachedQueryRestriction<SOURCE : Any, F : Any?> : YawnQueryRestriction<SOURCE> {
    val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>

    class EqualsDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion {
            return Subqueries.propertyEq(
                property.generatePath(context),
                detachedCriteriaBuilder.compile(context),
            )
        }
    }

    class EqualsAllDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyEqAll(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class NotEqualsDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyNe(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class GreaterThanDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyGt(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class GreaterThanAllDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGtAll(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class GreaterThanSomeDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGtSome(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class GreaterThanOrEqualToDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyGe(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class GreaterThanOrEqualToAllDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGeAll(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class GreaterThanOrEqualToSomeDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyGeSome(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class LessThanDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyLt(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class LessThanAllDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLtAll(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class LessThanSomeDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLtSome(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class LessThanOrEqualToDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyLe(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class LessThanOrEqualToAllDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLeAll(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class LessThanOrEqualToSomeDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyLeSome(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class InDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.propertyIn(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class NotInDetached<SOURCE : Any, F : Any?>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(context: YawnCompilationContext): Criterion = Subqueries.propertyNotIn(
            property.generatePath(context),
            detachedCriteriaBuilder.compile(context),
        )
    }

    class ExistsDetached<SOURCE : Any, F : Any?>(
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.exists(detachedCriteriaBuilder.compile(context))
    }

    class NotExistsDetached<SOURCE : Any, F : Any?>(
        override val detachedCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) : YawnDetachedQueryRestriction<SOURCE, F> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Subqueries.notExists(detachedCriteriaBuilder.compile(context))
    }
}
