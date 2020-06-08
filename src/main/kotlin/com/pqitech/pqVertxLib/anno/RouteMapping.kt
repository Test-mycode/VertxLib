package com.pqitech.pqVertxLib.anno

@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.ANNOTATION_CLASS,
  AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
annotation class RouteMapping(
  val value: String,
  val method: Array<String> = ["GET"]
)
