package com.faire.yawn.criteria.builder

/**
 * An abstract super-class for all Yawn criteria builders; not be used directly.
 *
 * It only encodes the one true behavior shared across all builders: they must be able to return themselves for builder
 * chaining. Since we have many different inheritances, interfaces, and delegators ("mixins") for different
 * functionalities for each builder, this fundamental shared piece allows them all to converse.
 *
 * The full inheritance chain from [BaseYawnBuilder] is:
 * * [YawnQueryBuilder], for builders that produce a [com.faire.yawn.query.YawnQuery]; which can be:
 * * * [EntityYawnQueryBuilder], for non-projected queries
 * * * [ProjectedYawnQueryBuilder], for projected queries
 * * [DetachedProjectedYawnBuilder], for detached criteria (these do not produce a query)
 *
 * @param CRITERIA is the concrete type of the criteria being used;
 *        this is required to specify the return type of the builder-style methods.
 *        It will be one of the above-mentioned types.
 */
abstract class BaseYawnBuilder<CRITERIA : BaseYawnBuilder<CRITERIA>> {
    /**
     * Following the "builder" pattern, each method on the various Yawn Query Builders returns the builder itself.
     * However, since we have an inheritance chain, in order for the type to match what you had before, we need this
     * type-aware override.
     * This should always just return `this`, but will ensure the correct [CRITERIA] typing.
     */
    protected abstract fun builderReturn(): CRITERIA
}
