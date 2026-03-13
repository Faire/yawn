package com.faire.yawn.project

import com.faire.yawn.YawnDef
import com.faire.yawn.project.AggregateKind.AVG
import com.faire.yawn.project.AggregateKind.COUNT
import com.faire.yawn.project.AggregateKind.COUNT_DISTINCT
import com.faire.yawn.project.AggregateKind.GROUP_BY
import com.faire.yawn.project.AggregateKind.MAX
import com.faire.yawn.project.AggregateKind.MIN
import com.faire.yawn.project.AggregateKind.SUM
import com.faire.yawn.project.ModifierKind.DISTINCT
import com.faire.yawn.query.YawnCompilationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ProjectorResolverTest {
    private val def = TestDef()
    private val nameCol = def.column<String>("name")
    private val nullableNameCol = def.column<String?>("name")
    private val pagesCol = def.column<Long>("pages")
    private val authorCol = def.column<String>("author")
    private val ratingCol = def.column<Double>("rating")
    private val revenueCol = def.column<Long>("revenue")

    @Test
    fun `single value projection`() {
        val projector = YawnValueProjector {
            ProjectionNode.property(nameCol)
        }

        val resolved = resolve(projector)

        val leaf = resolved.nodes.single().leaf as ProjectionLeaf.Property
        assertThat(leaf.column).isEqualTo(nameCol)

        assertThat(resolveAndMap(projector, "The Hobbit")).isEqualTo("The Hobbit")
    }

    @Test
    fun `aggregate value projection`() {
        val projector = YawnValueProjector {
            ProjectionNode.aggregate(MIN, ratingCol)
        }

        val resolved = resolve(projector)

        val leaf = resolved.nodes.single().leaf as ProjectionLeaf.Aggregate
        assertThat(leaf.kind).isEqualTo(MIN)

        assertThat(resolveAndMap(projector, 3.5)).isEqualTo(3.5)
    }

    @Test
    fun `constant projection produces no nodes`() {
        val projector = YawnProjector<Any, String?> {
            ProjectionNode.constant(null)
        }

        val resolved = resolve(projector)
        assertThat(resolved.nodes).isEmpty()
        assertThat(resolved.mapRow(listOf())).isNull()
    }

    @Test
    fun `constant with non-null value`() {
        val projector = YawnProjector {
            ProjectionNode.constant("hello")
        }

        val resolved = resolve(projector)
        assertThat(resolved.nodes).isEmpty()
        assertThat(resolved.mapRow(listOf())).isEqualTo("hello")
    }

    @Test
    fun `mapped projection folds transform into mapper`() {
        val inner = YawnValueProjector {
            ProjectionNode.property(nullableNameCol)
        }
        val projector = YawnProjector {
            ProjectionNode.mapped(inner) { it ?: "default" }
        }

        val resolved = resolve(projector)
        assertThat(resolved.nodes).hasSize(1) // mapped is eliminated, only the inner value remains

        assertThat(resolved.mapRow(listOf("present"))).isEqualTo("present")
        assertThat(resolved.mapRow(listOf(null))).isEqualTo("default")
    }

    @Test
    fun `pair composite flattens into two nodes`() {
        val projector = YawnProjector {
            ProjectionNode.composite(
                YawnValueProjector { ProjectionNode.property(nameCol) },
                YawnValueProjector { ProjectionNode.aggregate(SUM, pagesCol) },
            ) { a, b -> Pair(a, b) }
        }

        val resolved = resolve(projector)
        assertThat(resolved.nodes).hasSize(2)

        val result = resolved.mapRow(listOf("The Hobbit", 1_300L))
        assertThat(result).isEqualTo("The Hobbit" to 1_300L)
    }

    @Test
    fun `triple composite flattens into three nodes`() {
        val projector = YawnProjector {
            ProjectionNode.composite(
                YawnValueProjector { ProjectionNode.property(nameCol) },
                YawnValueProjector { ProjectionNode.property(authorCol) },
                YawnValueProjector { ProjectionNode.aggregate(SUM, pagesCol) },
            ) { a, b, c -> Triple(a, b, c) }
        }

        val resolved = resolve(projector)
        assertThat(resolved.nodes).hasSize(3)

        val result = resolved.mapRow(listOf("The Hobbit", "Tolkien", 300L))
        assertThat(result).isEqualTo(Triple("The Hobbit", "Tolkien", 300L))
    }

    @Test
    fun `deduplication of identical leaves`() {
        val projector = YawnProjector {
            ProjectionNode.composite(
                YawnValueProjector { ProjectionNode.aggregate(SUM, revenueCol) },
                YawnValueProjector { ProjectionNode.aggregate(SUM, revenueCol) },
            ) { a, b -> Pair(a, b) }
        }

        val resolved = resolve(projector)
        assertThat(resolved.nodes).hasSize(1) // deduplicated!

        val result = resolved.mapRow(listOf(50_000L))
        assertThat(result).isEqualTo(50_000L to 50_000L) // both fields get the same value
    }

    @Test
    fun `different kinds on same column are NOT deduplicated`() {
        val projector = YawnProjector {
            ProjectionNode.composite(
                YawnValueProjector { ProjectionNode.aggregate(COUNT_DISTINCT, pagesCol) },
                YawnValueProjector { ProjectionNode.aggregate(COUNT, pagesCol) },
            ) { a, b -> Pair(a, b) }
        }

        val resolved = resolve(projector)

        // different kinds => different leaves
        assertThat(resolved.nodes).hasSize(2)
    }

    @Test
    fun `modifier wraps leaf`() {
        val projector = YawnValueProjector {
            val from = ProjectionNode.property(authorCol)
            ProjectionNode.Value(ProjectionLeaf.Modifier(DISTINCT, from.leaf), from.mapper)
        }

        val resolved = resolve(projector)

        val leaf = resolved.nodes.single().leaf as ProjectionLeaf.Modifier
        assertThat(leaf.kind).isEqualTo(DISTINCT)
        assertThat(leaf.inner).isEqualTo(ProjectionLeaf.Property(authorCol))

        assertThat(resolved.mapRow(listOf("Tolkien"))).isEqualTo("Tolkien")
    }

    @Test
    fun `row count projection`() {
        val projector = YawnValueProjector {
            ProjectionNode.rowCount()
        }

        val resolved = resolve(projector)

        val leaf = resolved.nodes.single().leaf
        assertThat(leaf).isInstanceOf(ProjectionLeaf.RowCount::class.java)

        assertThat(resolved.mapRow(listOf(42L))).isEqualTo(42L)
    }

    @Test
    fun `3-level nested composites with deduplication`() {
        val projector = YawnProjector {
            ProjectionNode.composite(
                listOf(
                    // publisherName: groupBy
                    YawnValueProjector {
                        ProjectionNode.aggregate(GROUP_BY, nameCol)
                    },
                    // topAuthorStats: nested composite
                    YawnProjector {
                        ProjectionNode.composite(
                            // authorInfo: pair(distinct(author), count(pages))
                            {
                                ProjectionNode.composite(
                                    YawnValueProjector<Any, String> {
                                        ProjectionNode.Value(
                                            ProjectionLeaf.Modifier(DISTINCT, ProjectionLeaf.Property(authorCol)),
                                        )
                                    },
                                    YawnValueProjector<Any, Long> {
                                        ProjectionNode.aggregateAs(COUNT, pagesCol)
                                    },
                                ) { a, b -> Pair(a, b) }
                            },
                            // pageStats: pair(avg(pages), max(pages))
                            {
                                ProjectionNode.composite(
                                    YawnValueProjector<Any, Double> {
                                        ProjectionNode.aggregateAs(AVG, pagesCol)
                                    },
                                    YawnValueProjector {
                                        ProjectionNode.aggregate(MAX, pagesCol)
                                    },
                                ) { a, b -> Pair(a, b) }
                            },
                        ) { authorInfo, pageStats ->
                            AuthorSummary(
                                authorInfo = authorInfo,
                                pageStats = pageStats,
                            )
                        }
                    },
                    // totalRevenue: sum(revenue)
                    YawnValueProjector {
                        ProjectionNode.aggregate(SUM, revenueCol)
                    },
                    // totalRevenueCheck: sum(revenue) (duplicate)
                    YawnValueProjector {
                        ProjectionNode.aggregate(SUM, revenueCol)
                    },
                ),
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                PublisherReport(
                    publisherName = values[0] as String,
                    topAuthorStats = values[1] as AuthorSummary,
                    totalRevenue = values[2] as Long,
                    totalRevenueCheck = values[3] as Long,
                )
            }
        }

        val resolved = resolve(projector)

        // Verify flat node list: 6 unique leaves (SUM(revenue) deduped)
        assertThat(resolved.nodes).hasSize(6)

        // Verify node types in order
        assertThat(resolved.nodes[0].leaf).isEqualTo(ProjectionLeaf.Aggregate(GROUP_BY, nameCol))
        assertThat(resolved.nodes[1].leaf)
            .isEqualTo(ProjectionLeaf.Modifier(DISTINCT, ProjectionLeaf.Property(authorCol)))
        assertThat(resolved.nodes[2].leaf).isEqualTo(ProjectionLeaf.Aggregate(COUNT, pagesCol))
        assertThat(resolved.nodes[3].leaf).isEqualTo(ProjectionLeaf.Aggregate(AVG, pagesCol))
        assertThat(resolved.nodes[4].leaf).isEqualTo(ProjectionLeaf.Aggregate(MAX, pagesCol))
        assertThat(resolved.nodes[5].leaf).isEqualTo(ProjectionLeaf.Aggregate(SUM, revenueCol))

        // Verify result mapping with mock data
        val result = resolved.mapRow(
            listOf("Penguin", "Tolkien", 5L, 320.0, 1_000L, 50_000L),
        )
        assertThat(result).isEqualTo(
            PublisherReport(
                publisherName = "Penguin",
                topAuthorStats = AuthorSummary(
                    authorInfo = "Tolkien" to 5L,
                    pageStats = 320.0 to 1_000L,
                ),
                totalRevenue = 50_000L,
                totalRevenueCheck = 50_000L, // same value from deduplicated index
            ),
        )
    }

    @Test
    fun `composite with constants and mapped produces minimal nodes`() {
        val nullablePagesCol = def.column<Long?>("pages")
        val projector = YawnProjector {
            ProjectionNode.composite(
                // real column
                YawnValueProjector { ProjectionNode.property(nameCol) },
                // null constant; no SQL
                { ProjectionNode.constant(null) },
                // coalesce (mapped) wrapping a column; one SQL column
                {
                    ProjectionNode.mapped(
                        YawnValueProjector {
                            ProjectionNode.property(nullablePagesCol)
                        },
                    ) { it ?: 0L }
                },
            ) { a, b, c -> Triple(a, b, c) }
        }

        val resolved = resolve(projector)

        assertThat(resolved.nodes).hasSize(2) // only the two real columns

        val result = resolved.mapRow(listOf("The Hobbit", null))
        assertThat(result).isEqualTo(Triple("The Hobbit", null, 0L)) // coalesce applied, constant is null
    }

    @Test
    fun `deeply nested mapped chains are all eliminated`() {
        val projector = YawnProjector {
            ProjectionNode.mapped(
                {
                    ProjectionNode.mapped(
                        {
                            ProjectionNode.mapped(
                                YawnValueProjector { ProjectionNode.property(nameCol) },
                            ) { it.lowercase() }
                        },
                    ) { it.trim() }
                },
            ) { it.uppercase() }
        }

        val resolved = resolve(projector)

        assertThat(resolved.nodes).hasSize(1) // all mapped layers eliminated

        val result = resolved.mapRow(listOf("  Hello World  "))
        // Transforms applied inside-out: lowercase -> trim -> uppercase
        assertThat(result).isEqualTo("HELLO WORLD")
    }

    @Test
    fun `sql leaf projection`() {
        val projector = YawnValueProjector<Any, Long> {
            ProjectionNode.sql(
                sqlExpression = "LENGTH({alias}.name) AS name_length",
                aliases = listOf("name_length"),
                resultTypes = listOf(Long::class),
            )
        }

        val resolved = resolve(projector)

        val leaf = resolved.nodes.single().leaf as ProjectionLeaf.Sql
        assertThat(leaf.sqlExpression).isEqualTo("LENGTH({alias}.name) AS name_length")

        assertThat(resolved.mapRow(listOf(10L))).isEqualTo(10L)
    }

    data class AuthorSummary(
        val authorInfo: Pair<String, Long>,
        val pageStats: Pair<Double, Long>,
    )

    data class PublisherReport(
        val publisherName: String,
        val topAuthorStats: AuthorSummary,
        val totalRevenue: Long,
        val totalRevenueCheck: Long,
    )

    private class TestDef : YawnDef<Any, Any>() {
        fun <T> column(name: String): YawnColumnDef<T> = TestColumnDef(name)

        private inner class TestColumnDef<T>(private val name: String) : YawnColumnDef<T>() {
            override fun generatePath(context: YawnCompilationContext): String = name
            override fun toString(): String = "col($name)"
        }
    }

    private fun <TO> resolve(projector: YawnProjector<Any, TO>): ResolvedProjection<Any, TO> =
        ProjectorResolver<Any>().resolve(projector)

    private fun <TO> resolveAndMap(projector: YawnProjector<Any, TO>, vararg values: Any?): TO {
        val resolved = resolve(projector)
        return resolved.mapRow(listOf(*values))
    }
}
