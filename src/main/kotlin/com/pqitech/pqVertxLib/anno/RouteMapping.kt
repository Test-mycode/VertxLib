package com.pqitech.pqVertxLib.anno

import java.lang.annotation.Inherited

@Inherited
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RouteMapping(
  val value: String,
  val method: Array<String> = ["GET"]
)
