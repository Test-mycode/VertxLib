package com.pqitech.pqVertxLib.core
import com.pqitech.pqVertxLib.anno.*
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.jvm.kotlinFunction

class RouteInfo(private var method: Method) {
  var isSuspend = false
  var isBlocking = false
  var mediaType: String? = null
  var produces: Produces? = null
  var routeMapping: RouteMapping? = null
  val args: MutableList<RouteArg> = ArrayList();

  init {
    isBlocking = method.getAnnotation(ExecuteBlock::class.java) != null
    routeMapping = method.getAnnotation(RouteMapping::class.java)
    for (parameter in method.parameters) {
      val type = parameter.type
      if (type == RoutingContext::class.java) {
        args.add(RouteArg.Companion.createRoutingContext())
        continue
      }
      val routeBody = parameter.getAnnotation(RouteBody::class.java)
      if (routeBody != null) {
        args.add(
            RouteArg.Companion.createBody(
                parameter.parameterizedType,
                routeBody
            )
        )
        continue
      }
      val routeHeader = parameter.getAnnotation(RouteHeader::class.java)
      if (routeHeader != null) {
        args.add(
            RouteArg.Companion.createHeader(
                parameter.parameterizedType,
                routeHeader
            )
        )
        continue
      }
      val routeParam = parameter.getAnnotation(RouteParam::class.java)
      if (routeParam != null) {
        args.add(
            RouteArg.Companion.createParam(
                parameter.parameterizedType,
                routeParam
            )
        )
        continue
      }
      val routePathValue = parameter.getAnnotation(RoutePathValue::class.java)
      if (routePathValue != null) {
        args.add(
            RouteArg.Companion.createPathValue(
                parameter.parameterizedType,
                routePathValue
            )
        )
      }
    }

    this.isSuspend = method.kotlinFunction?.isSuspend ?: false;

    this.produces = method.getAnnotation(Produces::class.java)
    if (produces != null) {
      mediaType = this.produces!!.value
    }
  }
}
