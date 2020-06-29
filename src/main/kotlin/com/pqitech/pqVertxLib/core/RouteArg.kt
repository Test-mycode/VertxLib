package com.pqitech.pqVertxLib.core

import com.fasterxml.jackson.core.type.TypeReference
import com.pqitech.pqVertxLib.anno.*
import java.lang.reflect.Type

class RouteArg {
  var isString = false
  var type: TypeReference<*>? = null
    private set
  var isBody = false
  var routeBody: RouteBody? = null
  var isParam = false
  var routeParam: RouteParam? = null
  var isHeader = false
  var routeHeader: RouteHeader? = null
  var isPathValue = false
  var routePathValue: RoutePathValue? = null
  var isRoutingContext = false
  var routeFiles: RouteFiles? = null
  var isUploadFile = false
  private fun setType(type: Type) {
    if (type.typeName == String::class.java.typeName) isString = true
    this.type = object : TypeReference<Any?>() {
      override fun getType(): Type {
        return type
      }
    }
  }

  companion object {
    fun createBody(type: Type, routeBody: RouteBody?): RouteArg {
      val routeArg = RouteArg()
      routeArg.setType(type)
      routeArg.isBody = true
      routeArg.routeBody = routeBody
      return routeArg
    }

    fun createParam(type: Type, routeParam: RouteParam?): RouteArg {
      val routeArg = RouteArg()
      routeArg.setType(type)
      routeArg.isParam = true
      routeArg.routeParam = routeParam
      return routeArg
    }

    fun createHeader(type: Type, routeHeader: RouteHeader?): RouteArg {
      val routeArg = RouteArg()
      routeArg.setType(type)
      routeArg.isHeader = true
      routeArg.routeHeader = routeHeader
      return routeArg
    }

    fun createPathValue(type: Type, routePathValue: RoutePathValue?): RouteArg {
      val routeArg = RouteArg()
      routeArg.setType(type)
      routeArg.isPathValue = true
      routeArg.routePathValue = routePathValue
      return routeArg
    }

    fun createRoutingContext(): RouteArg {
      val routeArg = RouteArg()
      routeArg.isRoutingContext = true
      return routeArg
    }

    fun createUploadFiles(routerFiles: RouteFiles): RouteArg {
      val routerArg = RouteArg()
      routerArg.isUploadFile = true
      routerArg.routeFiles = routerFiles
      return routerArg
    }
  }
}
