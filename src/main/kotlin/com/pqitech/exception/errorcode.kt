package com.pqitech.exception;

import java.lang.Exception

class ErrorCodeException(val errorCode : Int, msg : String ) : Exception(msg)
{
    constructor(code : ErrorCode) : this(code.code,code.baseMsg) {
    }

    constructor(code : ErrorCode, string: String) : this(code.code,"${code.baseMsg}: $string") {
    }
}

data class ErrorCode(val code : Int, val baseMsg : String)
{
}

object DefErrorCode
{
    val unknownError = ErrorCode(500,"未知错误")
    // check ： 30000 ~ 30009
    val checkSumError  = ErrorCode(30000,"参数校验值错误")
    val missingParameter = ErrorCode(30001,"参数必须存在且不能为空！")
    // 格式检查 30010 ~ 30019
    val paramFormalError = ErrorCode(10010,"参数格式错误！")
    val emailFormalError = ErrorCode(10011,"email 格式错误！")
    val phoneFormalError = ErrorCode(10012,"phone 格式错误！")

    // User 30100 ~ 30199
    val userNoLogin  = ErrorCode(30100,"用户未登录！")
    val userNotFound = ErrorCode(30101,"用户不存在！")
    val passwordError = ErrorCode(30102,"密码错误")
    val tokenExpire = ErrorCode(30103,"Token 过期")
    val tokenInValid = ErrorCode(30104,"Token 无效")
    val userDisable = ErrorCode(30105,"用户被禁用")
    val userNoPermission =  ErrorCode(30106,"用户无权限")
    val UserAlreadytExists = ErrorCode(30107,"用户已经存在")


}
