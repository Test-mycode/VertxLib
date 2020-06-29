package com.pqitech.pqVertxLib.anno

import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RoutePathValue(
  val value: String,
  val require: Boolean = true,
  val defaultVal: String = ""
)
