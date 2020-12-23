package com.foreignlanguagereader.domain.client.common

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import cats.data.Nested
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.ws.WSClient

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * Common behavior for rest clients that we implement using WS
  */
case class RestClient(
    ws: WSClient,
    implicit val ec: ExecutionContext,
    breaker: Circuitbreaker,
    name: String,
    headers: List[(String, String)],
    timeout: FiniteDuration
) {
  val logger: Logger = Logger(this.getClass)

  logger.info(s"Initialized ws client $name with timeout $timeout")

  def get[T: ClassTag](
      url: String
  )(implicit reads: Reads[T]): Nested[Future, CircuitBreakerResult, T] = {
    val typeName = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    val message = s"Failed to get $typeName from $url"
    get(url, message)
  }

  def get[T: ClassTag](
      url: String,
      logIfError: String
  )(implicit reads: Reads[T]): Nested[Future, CircuitBreakerResult, T] = {
    logger.info(s"Calling url $url")
    val typeName = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    breaker.withBreaker(logIfError) {
      ws.url(url)
        // Doubled so that the circuit breaker will handle it.
        .withRequestTimeout(timeout * 2)
        .withHttpHeaders(headers: _*)
        .get()
        .map(_.json.validate[T])
        .map {
          case JsSuccess(result, _) => result
          case JsError(errors) =>
            val error = s"Failed to parse $typeName from $url: $errors"
            logger.error(error)
            throw new IllegalArgumentException(error)
        }
    }
  }
}

class RestClientBuilder @Inject() (system: ActorSystem, ws: WSClient) {
  def buildClient(
      name: String,
      headers: List[(String, String)] = List(),
      timeout: FiniteDuration = FiniteDuration(60, TimeUnit.SECONDS),
      resetTimeout: FiniteDuration = FiniteDuration(60, TimeUnit.SECONDS),
      maxFailures: Int = 5
  )(implicit ec: ExecutionContext): RestClient = {
    val breaker: Circuitbreaker =
      new Circuitbreaker(system, ec, name, timeout, resetTimeout, maxFailures)
    RestClient(
      ws,
      ec,
      breaker,
      name,
      headers,
      timeout
    )
  }
}