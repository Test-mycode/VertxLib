package com.pqitech.vertx

import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RouteInfo(
  val path : String,
  val method : String = "GET"
)


interface  RouteBaseController {

  fun handlerFailed(context: RoutingContext,e : Throwable,status : Int = 500, msg : String = "")
  {
    AbstractWebVerticle.handleException(context,e,status,msg)
  }
}


fun <T : RouteBaseController> Router.addRoute(classInfo : Class<T>)
{
  for(method in classInfo.methods){
    val value = method.getAnnotation(RouteInfo::class.java)
    if(value != null){
      val httpmethod = HttpMethod.valueOf(value.method.toUpperCase())
      val route = this.route(httpmethod,value.path);
        route.handler {
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

fun <T : RouteBaseController> Router.addRoute(obj : T)
{
  for(method in obj.javaClass.methods){
    val value = method.getAnnotation(RouteInfo::class.java)
    if(value != null){
      val httpMethod = HttpMethod.valueOf(value.method.toUpperCase())
      val route = this.route(httpMethod,value.path);
      route.handler {
        try {
          method.invoke(obj, it);
        } catch (e: Throwable){
          obj.handlerFailed(it,e)
        }
      }
    }
  }
}
