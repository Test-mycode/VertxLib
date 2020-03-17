package com.pqitech.verticle

import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import  io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await


abstract class WebVerticle : CoroutineVerticle() {
  private var server : HttpServer? = null
  private var router : Router ? = null

  abstract fun initRouter(router: Router);
  abstract fun httpConfig(option : HttpServerOptions)

  override suspend fun start() {
    val options = HttpServerOptions(config)
    httpConfig(options)
    server = vertx.createHttpServer(options)
    router = Router.router(vertx)
    initRouter(router!!)
    server?.requestHandler(router)?.listen()?.await()
  }

  override suspend fun stop() {
    server?.close()?.await()
  }

}


