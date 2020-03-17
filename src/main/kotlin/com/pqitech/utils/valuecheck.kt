package com.pqitech.utils

import io.vertx.core.json.JsonObject
import io.vertx.core.MultiMap
import com.pqitech.exception.*


fun <T> T?.checkParamNull(key : String = "") : T
{
    if(this == null)
    {
        throw ErrorCodeException(DefErrorCode.missingParameter, "$key 必须设置值！")
    }
    return this
}

fun JsonObject.mustString(key : String) : String
{
    val ret = this.getString(key,"")
    if(ret.isEmpty())
        throw ErrorCodeException(DefErrorCode.missingParameter, "$key 必须设置值！")
    return ret
}

fun JsonObject.maybeString(key : String)  = this.getString(key,"")


fun MultiMap.must(key : String) : String
{
    val ret = this.get(key)
    if(ret.isNullOrEmpty())
        throw ErrorCodeException(DefErrorCode.missingParameter,"$key 必须设置值！")
    return ret
}

fun MultiMap.maybe(key : String) : String
{
    val ret = this.get(key)
    if(ret.isNullOrEmpty())
        return "";
    return ret
}
