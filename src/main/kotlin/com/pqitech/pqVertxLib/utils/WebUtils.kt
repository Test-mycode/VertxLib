package com.pqitech.pqVertxLib.utils

import com.pqitech.pqVertxLib.exception.ErrorCodeException
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager

internal val log = LogManager.getLogger("com.pqitecch.web.error")


fun HttpServerResponse.endJson(json: JsonObject) {
  this.putHeader("Content-type", "application/json").end(json.toBuffer())
}

fun HttpServerResponse.endJson(json: String) {
  this.putHeader("Content-type", "application/json").end(json)
}

fun RoutingContext.launch(handle: suspend () -> Unit) {
  val context = this;
  CoroutineScope(this.vertx().dispatcher()).launch {
    try {
      handle()
    } catch (e: Throwable) {
      context.fail(e)
    }
  }
}


fun RoutingContext.executeBlocking(handle: () -> Unit) {
  val context = this
  this.vertx().executeBlocking<Void>({
    try {
      handle()
      it.complete()
    } catch (e: Throwable) {
      it.fail(e)
    }
  },{
    if (!it.succeeded()) {
      context.fail(it.cause())
    }
  })
}

fun RoutingContext.handleException(e: Throwable?, status: Int = 500, msg: String = "") {
  val ret = JsonObject()
  ret.put("code", status)
  if (msg.isEmpty())
    ret.put("message", e?.message ?: "未知错误")
  else
    ret.put("message", msg)

  if (e != null) {
    if (e is ErrorCodeException) {
      ret.put("code", e.errorCode)
      log.debug(e)
    } else {
      log.warn("has erro: ", e)
    }
  }
  this.response().endJson(ret)
}

fun Router.enableCors() {
  this.route().handler(
    CorsHandler.create("*")
      .allowedMethod(HttpMethod.POST).allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.DELETE).allowedMethod(HttpMethod.PUT)
      .allowedHeader("Content-Type").allowedHeader("Origin")
      .allowedHeader("DNT").allowedHeader("User-Agent")
      .allowedHeader("Authorization").allowedHeader("Cache-Control")
      .allowedHeader("X-Requested-With").allowedHeader("X-Mx-ReqToken")
      .allowedHeader("X-Appid").allowedHeader("X-Token")
      .allowedHeader("*")//.allowCredentials(true)
  )
}

fun Router.bodyHandle(
  maxBody: Long,
  handleFileUploads: Boolean = true,
  deleteOnEnd: Boolean = false,
  upDirPath: String? = null
) {
  val bodyHandler = BodyHandler.create(handleFileUploads).setBodyLimit(maxBody)
    .setDeleteUploadedFilesOnEnd(deleteOnEnd)
  if (!upDirPath.isNullOrEmpty())
    bodyHandler.setUploadsDirectory(upDirPath)
  this.route().handler(bodyHandler)
}
