package com.pqitech.vertx

import com.pqitech.exception.ErrorCodeException
import com.pqitech.utils.endJson
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import org.apache.logging.log4j.LogManager

abstract class AbstractWebVerticle  : CoroutineVerticle() {
  private lateinit var server : HttpServer
  private lateinit var router : Router

  val httpServer get() = server
  val httpRouter get() = router

  override suspend fun start() {
    server = vertx.createHttpServer(httpOptions())
    router = Router.router(vertx)
    router.route().failureHandler(::failureHandler)
    doInit()
    server.requestHandler(router).listen().await()
  }

  protected abstract suspend fun  doInit()

  override suspend fun stop() {
    server.close().await()
  }

  private fun httpOptions() : HttpServerOptions {
    val options = HttpServerOptions(config.getJsonObject("web", JsonObject()))
    options.setReuseAddress(true).isReusePort = true
    return  options
  }

  private fun failureHandler(context: RoutingContext)
  {
    handleException(context,context.failure(),404)
  }

  companion object {
    val log_ = LogManager.getLogger("com.pqitech.vertx.WebVerticle")

    fun handleException(context: RoutingContext, e: Throwable?, status: Int = 500, msg: String = "") {
      val ret = JsonObject()
      ret.put("status", status);
      ret.put("error", status)
      if (msg.isEmpty())
        ret.put("message", e?.message ?: "未知错误")
      else
        ret.put("message", msg)
      ret.put("data", JsonObject())
      if (e != null) {
        if (e is ErrorCodeException) {
          log_.debug("catch ErrorCodeException : ", e);
          ret.put("status", e.errorCode)
        } else {
          e.printStackTrace()
          log_.warn("catch error exception : ", e);
        }
      }
      context.response().setStatusCode(200).endJson(ret)
    }
  }
}
