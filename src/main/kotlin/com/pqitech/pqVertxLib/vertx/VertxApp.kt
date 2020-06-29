package com.pqitech.pqVertxLib.vertx

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.pqitech.pqVertxLib.utils.launch
import com.pqitech.pqVertxLib.utils.parseArgs
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.core.closeAwait
import io.vertx.kotlin.core.deployVerticleAwait
import org.apache.logging.log4j.LogManager
import java.util.function.Supplier

abstract class VertxApp {
  private val log_ = LogManager.getLogger("com.pqitech.vertx.VertxApp")
  private lateinit var option_: JsonObject
  private var poolSize_ : Int = 2
  private var workPoolSize_ : Int = 2
  private lateinit var vertx_: Vertx;

  val vertx get() = vertx_
  val startOptions get() = option_
  val eventLoopSize get() = poolSize_
  val workPoolSize get() = workPoolSize_

  fun start(args : Array<String>, name : String = "Pqitech Vertx App"){
    val option = parseArgs(args, name)
    start(option)
  }

  fun start(option: JsonObject)
  {
    this.option_ = option
    val optionVertx = VertxOptions(option.getJsonObject("vertx", JsonObject()))
    this.poolSize_ = optionVertx.eventLoopPoolSize
    this.workPoolSize_ = optionVertx.workerPoolSize
    this.vertx_ = Vertx.vertx(optionVertx)
    DatabindCodec.mapper().registerModule(AfterburnerModule())
    DatabindCodec.prettyMapper().registerModule(AfterburnerModule())
    DatabindCodec.mapper().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true)
    DatabindCodec.prettyMapper().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,true)
    vertx.launch {
      try {
        this.doStart()
      } catch (e : Throwable){
        e.printStackTrace()
        log_.error(e)
        vertx.closeAwait()
      }
    }
  }

  protected abstract suspend fun doStart() ;

  suspend fun deployVerticleConfigAwait(size : Int ,hander : ()->Verticle){
    val deploymentOptions = DeploymentOptions()
    deploymentOptions.instances = size
    deployVerticleConfigAwait(deploymentOptions,hander)
  }

  suspend fun deployVerticleConfigAwait(deploymentOptions : DeploymentOptions ,hander : ()->Verticle){
    deploymentOptions.config = startOptions
    vertx.deployVerticleAwait(Supplier { hander() },deploymentOptions)
  }
}

