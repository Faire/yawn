package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "widget_parts")
@YawnEntity
internal class WidgetPart : TimestampedEntity<WidgetPart>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<WidgetPart>
        protected set

    @Column
    lateinit var name: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_id")
    lateinit var widget: Widget
}
