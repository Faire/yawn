package com.faire.yawn.util

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeAliasSpec

internal fun FileSpec.Builder.addTypeAliases(typeAliases: Collection<TypeAliasSpec>): FileSpec.Builder {
    for (typeAlias in typeAliases) {
        addTypeAlias(typeAlias)
    }
    return this
}
