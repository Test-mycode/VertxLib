package com.pqitech.pqVertxLib.exception

import java.lang.Exception

class ErrorCodeException(val errorCode : Int, msg : String ) : Exception(msg)
{
    constructor(code : ErrorCode) : this(code.code,code.baseMsg)

    constructor(code : ErrorCode, string: String) : this(code.code,"${code.baseMsg}: $string")
}

data class ErrorCode(val code : Int, val baseMsg : String)
{
}

object DefErrorCode
{
  val unknownError = ErrorCode(500, "未知错误")
  // check ： 1000 ~ 1999
  val checkSumError  = ErrorCode(1000, "参数校验值错误")
  val missingParameter = ErrorCode(1001, "参数必须存在且不能为空！")
  // 格式检查 1010 ~ 1019
  val paramFormalError = ErrorCode(1010, "参数格式错误！")
  val emailFormalError = ErrorCode(1011, "email 格式错误！")
  val phoneFormalError = ErrorCode(1012, "phone 格式错误！")

  // User 2000 ~ 2099
  val userNoLogin  = ErrorCode(2000, "用户未登录！")
  val userNotFound = ErrorCode(2001, "用户不存在！")
  val passwordError = ErrorCode(2002, "密码错误")
  val tokenExpire = ErrorCode(2003, "Token 过期")
  val tokenInValid = ErrorCode(2004, "Token 无效")
  val userDisable = ErrorCode(2005, "用户被禁用")
  val userNoPermission = ErrorCode(2006, "用户无权限")
  val UserAlreadytExists = ErrorCode(2007, "用户已经存在")


}
