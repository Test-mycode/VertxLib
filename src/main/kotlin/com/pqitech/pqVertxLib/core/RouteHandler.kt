package com.pqitech.pqVertxLib.core

import com.fasterxml.jackson.core.JsonProcessingException
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.hibernate.validator.HibernateValidator
import java.lang.invoke.MethodHandle
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.validation.Validation
import javax.validation.ValidationException
import javax.validation.Validator

class RouteHandler(
  method: Method,
  private val instance: Any,
  private val methodHandle: MethodHandle
) : Handler<RoutingContext> {
  val routeInfo: RouteInfo =
      RouteInfo(method)

  companion object {
    private var validator: Validator = Validation
      .byProvider(HibernateValidator::class.java)
      .configure()
      .failFast(true)
      .buildValidatorFactory()
      .validator
  }

  private fun generateParams(routingContext: RoutingContext): Array<Any?> {
    val args = routeInfo.args
    val result = arrayOfNulls<Any>(args.size + 1)
    result[0] = instance
    for (i in args.indices) {
      result[i + 1] = generateParam(routingContext, args[i])
    }
    return result
  }

  private fun <T> validate(obj: T) {
    val constraintViolations = validator.validate(obj)
    // 抛出检验异常
    if (constraintViolations.size > 0) {
      throw ValidationException(String.format("参数校验失败:%s", constraintViolations.iterator().next().message))
    }
  }

  private fun generateParam(routingContext: RoutingContext, routeArg: RouteArg?): Any? {
    if (routeArg!!.isBody) {
      return try {
        val body = routingContext.bodyAsString
        require(!(body == null || body.isBlank())) { "http请求体不存在" }
        val result = deserialization(body, routeArg)

        //使用hibernate validator 校验bean
        if (routeArg.routeBody!!.valid) {
          validate(result)
        }
        result
      } catch (e: JsonProcessingException) {
        throw IllegalArgumentException("http请求体解析出错", e)
      }
    }
    if (routeArg.isHeader) {
      val routeHeader = routeArg.routeHeader
      val header = routingContext.request().getHeader(routeHeader!!.value)
      try {
        if (header != null && !header.isBlank()) return deserialization(header, routeArg)
        if (!routeHeader.defaultVal.isBlank()) return deserialization(routeHeader.defaultVal, routeArg)
        require(!routeHeader.require) { "http请求头" + routeHeader.value + "不存在" }
      } catch (e: JsonProcessingException) {
        throw IllegalArgumentException("http请求头" + routeHeader.value + "解析出错", e)
      }
    }
    if (routeArg.isParam) {
      val routeParam = routeArg.routeParam
      val params = routingContext.queryParam(routeParam!!.value)
      var param = if (params == null || params.isEmpty()) null else params[0]
      if (param == null) param = routingContext.request().getParam(routeParam.value)
      try {
        if (param != null && !param.isBlank()) return deserialization(param, routeArg)
        if (!routeParam.defaultVal.isBlank()) return deserialization(routeParam.defaultVal, routeArg)
        require(!routeParam.require) { "请求参数" + routeParam.value + "不存在" }
      } catch (e: JsonProcessingException) {
        throw IllegalArgumentException("请求参数" + routeParam.value + "解析出错", e)
      }
    }
    if (routeArg.isPathValue) {
      val routePathValue = routeArg.routePathValue
      val pathValue = routingContext.pathParam(routePathValue!!.value)
      try {
        if (pathValue != null && !pathValue.isBlank()) return deserialization(pathValue, routeArg)
        if (!routePathValue.defaultVal.isBlank()) return deserialization(routePathValue.defaultVal, routeArg)
        require(!routePathValue.require) { "请求参数" + routePathValue.value + "不存在" }
      } catch (e: JsonProcessingException) {
        throw IllegalArgumentException("请求参数" + routePathValue.value + "解析出错", e)
      }
    }
    if(routeArg.isUploadFile) {
      return routingContext.fileUploads()
    }
    return if (routeArg.isRoutingContext) {
      routingContext
    } else null
  }

  @Throws(JsonProcessingException::class)
  private fun deserialization(value: String, routeArg: RouteArg): Any? {
    return if (routeArg.isString) value else DatabindCodec.mapper().readValue(value, routeArg.type)
  }

  override fun handle(routingContext: RoutingContext) {
    val context = this
    CoroutineScope(routingContext.vertx().dispatcher()).launch {
      try {
        val result = suspendCancellableCoroutine <Any> { continuation ->
          if (context.routeInfo.isSuspend)
            methodHandle.invokeWithArguments(*generateParams(routingContext), continuation)
          else
            methodHandle.invokeWithArguments(*generateParams(routingContext))
        }
        context.output(routingContext.response(), result)
      } catch (e: InvocationTargetException) {
        routingContext.fail(e.targetException)
      } catch (throwable: Throwable) {
        routingContext.fail(throwable)
      }
    }
  }

  @Throws(JsonProcessingException::class)
  private fun output(response: HttpServerResponse, result: Any?) {
    if (routeInfo.mediaType != null)
      response.putHeader("Content-Type", routeInfo.mediaType)

    if (result == null || result.javaClass == Void::class.java) {
      response.end()
    } else if (result is String) {
      if (routeInfo.mediaType == null)
        response.putHeader("Content-Type", "text/plain; charset=utf-8")
      response.end(result)
    } else if (result is JsonArray || result is JsonObject) {
      if (routeInfo.mediaType == null)
        response.putHeader("Content-Type", "application/json; charset=utf-8")
      response.end(result.toString())
    } else {
      if (routeInfo.mediaType == null)
        response.putHeader("Content-Type", "application/json; charset=utf-8")

      response.end(DatabindCodec.mapper().writeValueAsString(result))
    }
  }

}
