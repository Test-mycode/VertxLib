package com.pqitech.utils

//import io.vertx.core.logging.LoggerFactory
import com.pqitech.exception.ErrorCodeException
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.ClusteredSessionStore
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.CookieHandler
import java.util.logging.Level
import java.util.logging.Logger

internal val log = Logger.getLogger("com.pqitecch.web.error")


fun HttpServerResponse.endJson(json : JsonObject)
{
  this.putHeader("Content-type","application/json").end(json.toBuffer())
}

fun HttpServerResponse.endJson(json : String)
{
  this.putHeader("Content-type","application/json").end(json)
}

fun RoutingContext.launch(handle : suspend ()-> Unit)
{
  val context = this;
  CoroutineScope(this.vertx().dispatcher()).launch{
    try {
      handle()
    } catch (e : Throwable) {
      context.fail(e)
    }
  }
}


fun RoutingContext.executeBlocking(handle : ()-> Unit)
{
    val context = this;
    this.vertx().executeBlocking<Unit> {
      try {
        handle()
        it.complete()
      }
      catch (e : Throwable) {
        it.fail(e)
      }
    }.onComplete {
      if(!it.succeeded()) {
        context.fail(it.cause())
      }
    }
}

fun RoutingContext.handleException(e : Throwable?, status : Int = 500, msg : String = "")
{
  val ret = JsonObject()
  ret.put("status",status);
  if(msg.isEmpty())
    ret.put("msg", e?.message ?: "未知错误")
  else
    ret.put("msg", msg)

  if(e != null){
    if(e is ErrorCodeException){
      ret.put("status", e.errorCode)
    }
//    e.printStackTrace(log)
    log.log(Level.WARNING,"catch error exception : ",e);
//    LoggerFactory.getLogger("web").warn("handle error:", e);
  }
  this.response().endJson(ret)
}

fun Router.enableCors() {
  this.route().handler( CorsHandler.create("*")
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

//enableClusteredSession(Router router) {
//  router.route().handler(CookieHandler.create());
//  router.route().handler(SessionHandler.create(
//    ClusteredSessionStore.create(vertx, "shopping.user.session")));
//}
//fun Router.enableLocalSession() {
//  this.route().handler(CookieHandler.getDefault())
//  this..route().handler(
//    SessionHandler.create(
//      LocalSessionStore.create(vertx, "shopping.user.session")
//    )
//  )
//}

fun Router.bodyHandle(maxBody: Long,handleFileUploads : Boolean = true,deleteOnEnd : Boolean = false,upDirPath : String? = null) {
  val bodyHandler =  BodyHandler.create(handleFileUploads).setBodyLimit(maxBody)
                                .setDeleteUploadedFilesOnEnd(deleteOnEnd)
  if(!upDirPath.isNullOrEmpty())
    bodyHandler.setUploadsDirectory(upDirPath)
  this.route().handler(bodyHandler)
}
