package com.pqitech.vertx

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

typealias MiddlewareFun = ((RoutingContext)->Unit)
typealias BeforeAddRouteFun = ((Route)->Route)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RouteInfo(val path : String,
                           val method : String = "GET")


interface  RouteBaseController {

  fun handlerFailed(context: RoutingContext,e : Throwable,status : Int = 500, msg : String = "")
  {
    AbstractWebVerticle.handleException(context,e,status,msg)
  }
}


fun <T : RouteBaseController> Router.addRoute(classInfo : Class<T>,middlewareFun: MiddlewareFun ? = null)
{
  this.addClassRoute(classInfo){ route ->
    if(middlewareFun != null)
      route.handler(middlewareFun)
    route
  }
}

fun <T : RouteBaseController> Router.addRoute(obj : T,middlewareFun: MiddlewareFun ? = null)
{
  this.addClassRoute(obj){ route ->
    if(middlewareFun != null)
      route.handler(middlewareFun)
    route
  }
}

fun <T : RouteBaseController> Router.addClassRoute(classInfo : Class<T>,addRouteFun: BeforeAddRouteFun)
{
  for(method in classInfo.methods){
    val value = method.getAnnotation(RouteInfo::class.java)
    if(value != null){
      val httpMethod = HttpMethod.valueOf(value.method.toUpperCase())
      val route = this.route(httpMethod,value.path)
      addRouteFun(route).handler {
        try {
          val obj = classInfo.getDeclaredConstructor().newInstance()
          method.invoke(obj, it);
        } catch (e: Throwable){
          it.fail(e)
        }
      }
    }
  }
}

fun <T : RouteBaseController> Router.addClassRoute(obj : T,addRouteFun: BeforeAddRouteFun)
{
  for(method in obj.javaClass.methods){
    val value = method.getAnnotation(RouteInfo::class.java)
    if(value != null){
      val httpMethod = HttpMethod.valueOf(value.method.toUpperCase())
      val route = this.route(httpMethod,value.path);
      addRouteFun(route).handler {
        try {
          method.invoke(obj, it);
        } catch (e: Throwable){
          obj.handlerFailed(it,e)
        }
      }
    }
  }
}
