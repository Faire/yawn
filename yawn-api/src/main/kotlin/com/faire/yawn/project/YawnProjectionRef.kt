package com.faire.yawn.project

import com.faire.yawn.YawnRef

/**
 * The user-facing object representing a projection will extend [YawnProjectionDef].
 */
interface YawnProjectionRef<D : Any, DEF : YawnProjectionDef<D, D>> : YawnRef<D, DEF>
