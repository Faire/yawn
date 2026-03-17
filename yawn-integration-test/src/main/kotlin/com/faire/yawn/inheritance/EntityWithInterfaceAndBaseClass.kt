package com.faire.yawn.inheritance

import com.faire.yawn.YawnEntity

/**
 * Reproduces a KSP bug where [com.google.devtools.ksp.symbol.KSClassDeclaration.getAllProperties]
 * drops Java annotations (e.g. `@Column`) when the same property is declared in both an interface
 * ([HasName]) and an abstract class ([BaseEntityWithName]).
 */
@YawnEntity
internal class EntityWithInterfaceAndBaseClass : BaseEntityWithName(), HasName
