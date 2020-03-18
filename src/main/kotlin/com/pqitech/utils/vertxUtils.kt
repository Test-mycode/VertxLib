package com.pqitech.utils

import  io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory

fun Vertx.launch(handle : suspend ()-> Unit) = CoroutineScope(this.dispatcher()).launch{handle()}

fun nettyUseLog4j() {
  System.setProperty("vertx.logger-delegate-factory-class-name","io.vertx.core.logging.Log4j2LogDelegateFactory")
  InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
}
