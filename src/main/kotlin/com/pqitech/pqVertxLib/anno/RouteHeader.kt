package com.pqitech.pqVertxLib.anno

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RouteHeader(
  val value: String,
  val require: Boolean = true,
  val defaultVal: String = ""
)
