package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Version

/**
 * A dedicated entity for exercising pagination against an eager `@OneToMany` collection
 * ([WidgetPart]), without affecting the behavior of pagination tests against [Book] et al.
 *
 * See [YawnPaginationQueriesTest] for why this matters: an eager collection causes Hibernate to
 * LEFT OUTER JOIN it in whenever a [Widget] entity is hydrated, fanning a single [Widget] row out
 * into one row per [WidgetPart]. If a real page size is applied directly on top of that join, the
 * LIMIT/OFFSET truncates at the fanned SQL row level rather than the distinct-entity level.
 */
@Entity
@Table(name = "widgets")
@YawnEntity
internal class Widget : TimestampedEntity<Widget>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<Widget>
        protected set

    @Column
    @Version
    var version: Long = 0

    @Column
    lateinit var name: String

    @OneToMany(mappedBy = "widget", fetch = FetchType.EAGER)
    var parts: List<WidgetPart> = listOf()
}
