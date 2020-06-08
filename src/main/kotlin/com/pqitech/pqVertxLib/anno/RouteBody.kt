package com.pqitech.pqVertxLib.anno

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RouteBody(val valid: Boolean = true)
