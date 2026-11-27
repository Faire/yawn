package com.faire.yawn.project

/**
 * Holds one child of an [orderable composite][YawnProjectionPair] so it can be swapped in place for an
 * order-by-able wrapper *after* the composite has already been built, instead of before - see
 * [com.faire.yawn.criteria.query.orderAscBy]/[com.faire.yawn.criteria.query.orderDescBy].
 *
 * A slot always resolves to whatever it currently holds, so ordering by a slot (which replaces [current] with an
 * aliased wrapper around it) is automatically reflected the next time the owning composite is resolved - there is
 * no separate value to remember to thread back into `project(...)`.
 */
class ProjectionSlot<SOURCE : Any, TO> internal constructor(
    initial: YawnValueProjector<SOURCE, TO>,
) : YawnValueProjector<SOURCE, TO> {
    internal var current: YawnValueProjector<SOURCE, TO> = initial

    override fun projection(): ProjectionNode.Value<SOURCE, TO> = current.projection()
}
