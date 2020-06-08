package com.pqitech.pqVertxLib.anno

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Produces(val value: String = "text/html; charset=utf-8")
