package com.faire.yawn.setup.custom

import com.faire.yawn.setup.entities.BaseEntity
import com.faire.yawn.setup.entities.YawnId
import com.faire.yawn.setup.hibernate.YawnIdType
import com.google.gson.GsonBuilder
import com.google.inject.TypeLiteral
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.dialect.H2Dialect
import org.hibernate.internal.SessionFactoryImpl
import java.util.Properties
import kotlin.jvm.java
import kotlin.reflect.KClass

internal object CustomHibernateConfigurer {
  private val gson by lazy { GsonBuilder().create() }

  fun createSessionFactory(
      entities: Set<KClass<out BaseEntity<*>>>,
  ): SessionFactory {
    val properties = buildProperties()
    val configuration = Configuration().addProperties(properties)

    bindEntities(configuration, entities)
    registerCustomAdapters(configuration)
    registerCustomJsonAdapter(configuration, entities)

    val sessionFactory = configuration.buildSessionFactory()
    fixJsonBindingsHack(sessionFactory)

    return sessionFactory
  }

  private fun buildProperties(): Properties {
    val properties = Properties()

    // Switch to H2 Dialect for in-memory DB
    properties["hibernate.dialect"] = H2Dialect::class.qualifiedName

    // In-memory H2 database URL
    properties["hibernate.connection.driver_class"] = "org.h2.Driver"
    properties["hibernate.connection.url"] = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
    properties["hibernate.connection.username"] = "sa"
    properties["hibernate.connection.password"] = ""

    // Automatically create and drop schema
    properties["hibernate.hbm2ddl.auto"] = "create-drop"

    // Optional optimizations / settings
    properties["hibernate.show_sql"] = false
    properties["hibernate.format_sql"] = false
    properties["hibernate.use_sql_comments"] = false
    properties["hibernate.generate_statistics"] = false
    properties["hibernate.jdbc.time_zone"] = "UTC"
    properties["hibernate.default_batch_fetch_size"] = 50
    properties["hibernate.max_fetch_depth"] = 2
    properties["hibernate.jdbc.batch_size"] = 50

    return properties
  }

  private fun fixJsonBindingsHack(sessionFactory: SessionFactory) {
    // Hibernate populates this map to provide types for SQL queries, but this breaks our code all
    // over the place. JsonMessageType is not possible to initialize from the SQL context since
    // it can't provide the constructor arguments.
    // Clearing this out stops Hibernate from trying to load custom types in SQL queries.
    (sessionFactory as SessionFactoryImpl).metamodel
        .typeConfiguration
        .jdbcToHibernateTypeContributionMap
        .clear()
  }

  private fun bindEntities(
      configuration: Configuration,
      entities: Set<KClass<out BaseEntity<*>>>,
  ) {
    for (entity in entities) {
      configuration.addAnnotatedClass(entity.java)
    }
  }

  private fun registerCustomJsonAdapter(
      configuration: Configuration,
      entities: Set<KClass<out BaseEntity<*>>>,
  ) {
    val existingJsonKeys = mutableMapOf<String, TypeLiteral<*>>()
    for (entity in entities) {
      addCustomFieldTypeConverters(configuration, entity, existingJsonKeys)
    }
  }

  private fun addCustomFieldTypeConverters(
      configuration: Configuration,
      entityClass: KClass<out BaseEntity<*>>,
      existingJsonKeys: MutableMap<String, TypeLiteral<*>>,
  ) {
    val entityType = TypeLiteral.get(entityClass.java)

    for (field in entityClass.java.declaredFields) {
      if (field.isAnnotationPresent(SerializeAsJson::class.java)) {
        val fieldTypeLiteral = entityType.getFieldType(field)
        val jsonMessageType = JsonMessageType(gson, fieldTypeLiteral)
        val existingField = existingJsonKeys.put(field.type.name, fieldTypeLiteral)
        require(existingField == null || existingField == fieldTypeLiteral) {
          // If one entity has a field List<Country> and another has List<State>, hibernate won't be able to
          // deserialize one of them, since the key is just "java.util.List" for both.
          "@SerializeAsJson collision: ${field.type.name} for $field"
        }
        val keys = arrayOf(field.type.name)
        configuration.registerTypeOverride(jsonMessageType, keys)
      }
    }
  }

  private fun registerCustomAdapters(
      configuration: Configuration,
  ) {
    val idKeys = arrayOf(YawnId::class.java.name, YawnIdType::class.java.name)
    configuration.registerTypeOverride(YawnIdType(), idKeys)

    configuration.addAttributeConverter(EmailAddressConverter::class.java, true)
  }
}
