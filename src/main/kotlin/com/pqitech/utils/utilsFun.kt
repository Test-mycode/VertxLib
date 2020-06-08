package com.pqitech.utils

import java.nio.charset.Charset
import java.security.MessageDigest

fun ByteArray.toHex(): String {
  return joinToString("") { "%02x".format(it) }
}

fun sha512(input: String) = hashString("SHA-512", input)

fun sha256(input: String) = hashString("SHA-256", input)

fun sha1(input: String) = hashString("SHA-1", input)

fun md5(input: String) = hashString("MD5", input)


private fun hashString(type: String, input: String): String {

  val bytes = MessageDigest
    .getInstance(type)
    .digest(input.toByteArray(Charset.forName("UTF-8")))

  return bytes.toHex()
}



