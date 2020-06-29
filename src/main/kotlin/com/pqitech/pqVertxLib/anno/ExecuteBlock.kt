package com.pqitech.pqVertxLib.anno

import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExecuteBlock(val value: Boolean = true)
