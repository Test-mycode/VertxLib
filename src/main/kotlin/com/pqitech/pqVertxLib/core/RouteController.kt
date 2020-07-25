package com.pqitech.pqVertxLib.core

import com.pqitech.pqVertxLib.anno.RouteMapping
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import java.lang.invoke.MethodHandles
import java.util.*

abstract class RouteController {
  private val routeMapping: RouteMapping?
  private val lookup: MethodHandles.Lookup
  private val routes: MutableList<RouteHandler>

  init {
    val cls = this.javaClass
    routes = LinkedList()
    lookup = MethodHandles.lookup()
    routeMapping = cls.getAnnotation(RouteMapping::class.java)

    for (method in cls.declaredMethods) {
      if (method.isAnnotationPresent(RouteMapping::class.java)) {
        method.isAccessible = true
        routes.add(RouteHandler(method,this, lookup.unreflect(method)))
      }
    }
  }

  fun addRoute(vertx: Vertx, router: Router) {
    var r = router
    if (routeMapping != null && !routeMapping.value.isBlank()) {
      val subRouter = Router.router(vertx)
      router.mountSubRouter(routeMapping.value, subRouter)
      r = subRouter
    }
    for (routeHandler in routes) {
      val routeInfo = routeHandler.routeInfo
      for (httpMethod in routeInfo.routeMapping?.method!!) {
        val route = r.route(HttpMethod.valueOf(httpMethod), routeInfo.routeMapping?.value)
        if (routeInfo.isBlocking) route.blockingHandler(routeHandler) else route.handler(routeHandler)
      }
    }
    this.afterInit(vertx)
  }

  protected open fun afterInit(vertx: Vertx) {

  }

}
