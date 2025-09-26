package com.faire.yawn

import com.faire.yawn.project.YawnProjectionDef
import com.faire.yawn.project.YawnProjectionRef
import org.assertj.core.api.Assertions.assertThat
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

internal object YawnTestUtils {
    inline fun <reified T : Any> assertGeneratedEntity(
        expectedVisibility: KVisibility? = null,
        noinline builder: YawnTestAssertContext.() -> Unit = {},
    ) {
        internalAssertGeneratedEntity(
            expectedSuperClassDef = YawnTableDef::class,
            expectedSuperClassRef = YawnTableRef::class,
            sourceClass = T::class,
            expectedVisibility = expectedVisibility,
            builder = builder,
        )
    }

    inline fun <reified T : Any> assertGeneratedProjection(
        expectedVisibility: KVisibility? = null,
        noinline builder: YawnTestAssertContext.() -> Unit = {},
    ) {
        internalAssertGeneratedEntity(
            expectedSuperClassDef = YawnProjectionDef::class,
            expectedSuperClassRef = YawnProjectionRef::class,
            sourceClass = T::class,
            expectedVisibility = expectedVisibility,
            builder = builder,
        )
    }

    fun <T : Any> internalAssertGeneratedEntity(
        expectedSuperClassDef: KClass<out YawnDef<*, *>>,
        expectedSuperClassRef: KClass<out YawnRef<*, *>>,
        sourceClass: KClass<T>,
        expectedVisibility: KVisibility? = null,
        builder: YawnTestAssertContext.() -> Unit = {},
    ) {
        val generatedClass = getGeneratedDefKClass(sourceClass, expectedSuperClassDef)
        val generatedObject = getGeneratedDefKClass(sourceClass, expectedSuperClassRef)

        // assert the correct superclass type
        val superClass = generatedClass.java.genericSuperclass as ParameterizedType
        assertThat(superClass.rawType).isEqualTo(expectedSuperClassDef.java)

        // assert the generic parameter
        val genericParameter = superClass.actualTypeArguments.last()
        assertThat(genericParameter).isEqualTo(sourceClass.java)

        // maybe assert visibility
        if (expectedVisibility != null) {
            assertThat(generatedClass.visibility).isEqualTo(expectedVisibility)
        }

        // asserts from the lambda
        builder(YawnTestAssertContext(generatedClass, generatedObject))
    }

    private fun getGeneratedDefKClass(
        clazz: KClass<*>,
        expectedSuperClass: KClass<*>,
    ): KClass<*> {
        val classPath = clazz.java.packageName
        val defName = expectedSuperClass.simpleName!!.removePrefix("Yawn").removeSuffix("Ref")
        val classNamePrefix = clazz.java
            .getNestingChain()
            .joinToString(separator = "_") { it.simpleName }
        val className = "$classNamePrefix$defName"
        return Class.forName("$classPath.$className").kotlin
    }

    class YawnTestAssertContext(
        val generatedClass: KClass<*>,
        private val generatedObject: KClass<*>,
    ) {
        /**
         * Use as placeholder for the SOURCE generic parameter on the generated class in order to call [hasField].
         */
        class SOURCE

        private val sourcePlaceholderName = SOURCE::class.qualifiedName!!

        inline fun <reified T : Any, reified D> hasTableColumn(columnName: String) {
            hasField<YawnTableDef<SOURCE, T>.ColumnDef<D>>(columnName)
        }

        inline fun <reified T : Any, reified D> hasProjectionColumn(columnName: String) {
            hasField<YawnProjectionDef<SOURCE, T>.ProjectionColumnDef<D>>(columnName)
        }

        inline fun <reified T : Any, reified D : Any> hasEmbeddedProperty(
            columnName: String,
            embeddedTypeAssertions: (YawnTestEmbeddableAssertContext) -> Unit,
        ) {
            val property = generatedClass.memberProperties.single { it.name == columnName }
            assertThat(property.returnType.isSubtypeOf(typeOf<YawnTableDef<*, T>.EmbeddedDef<D>>())).isTrue()

            val embeddedClass = property.returnType.classifier as KClass<*>
            embeddedTypeAssertions(YawnTestEmbeddableAssertContext(embeddedClass, sourcePlaceholderName))
        }

        inline fun <reified C> hasField(columnName: String) {
            val properties = generatedClass.memberProperties
            val expectedTypeAsString = getTypeStringWithSourceReplaced<C>(sourcePlaceholderName)
            assertThat(properties).anyMatch { it.name == columnName && it.returnType.toString() == expectedTypeAsString }
        }

        fun hasNoField(columnName: String) {
            val properties = generatedClass.memberProperties
            assertThat(properties).noneMatch { it.name == columnName }
        }

        fun hasCompanionObjectWithCreateFunction() {
            assertThat(generatedObject.functions).anyMatch { it.name == "create" }
        }
    }

    class YawnTestEmbeddableAssertContext(
        private val embeddedClass: KClass<*>,
        private val sourcePlaceholderName: String,
    ) {
        inline fun <reified T : Any, reified D> hasEmbeddedTableColumn(columnName: String) {
            val properties = embeddedClass.memberProperties
            val columnType =
                getTypeStringWithSourceReplaced<YawnTableDef<YawnTestAssertContext.SOURCE, T>.ColumnDef<D>>(
                    sourcePlaceholderName,
                )
            assertThat(properties).anyMatch { it.name == columnName && it.returnType.toString() == columnType }
        }
    }
}

private fun Class<*>.getNestingChain(): List<Class<*>> {
    val chain = mutableListOf<Class<*>>()
    var currentClass: Class<*>? = this
    while (currentClass != null) {
        chain.add(currentClass)
        currentClass = currentClass.enclosingClass
    }
    return chain.reversed()
}

private inline fun <reified C> getTypeStringWithSourceReplaced(replacement: String): String {
    return typeOf<C>().toString().replace(replacement, "SOURCE")
}
