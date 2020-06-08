package com.pqitech.utils

import  io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.vertx.core.cli.CLI
import io.vertx.core.cli.Option
import io.vertx.core.json.JsonObject
import java.io.FileReader

fun Vertx.launch(handle : suspend ()-> Unit) = CoroutineScope(this.dispatcher()).launch{handle()}

fun parseArgs(args : Array<String>,name : String = args[0]) : JsonObject
{
  val config = Option()
  config.setShortName("c").setLongName("config").setDefaultValue("config.json").setDescription("设置程序的参数")
  val log = Option().setShortName("l").setLongName("log").setDefaultValue("log4j.xml").setDescription("设置日志配置文件")
  val cli = CLI.create(name).addOption(config).addOption(log).parse(args.toList())
  val configFile = cli.getRawValueForOption(config)
  println("the config file is: $configFile")
  val logFilePath = cli.getRawValueForOption(log)
  if(logFilePath.isNotEmpty()){
    System.setProperty("log4j.configurationFile",logFilePath)
    System.setProperty("log4j2.configurationFile",logFilePath)
  }
  val file = FileReader(configFile)
  return JsonObject(file.readText())
}
