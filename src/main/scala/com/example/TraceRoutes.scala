package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.{Future, ExecutionContext}
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import com.newrelic.api.agent.Trace

class TraceRoutes()(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  implicit val ec: ExecutionContext = system.executionContext

  val traceRoutes: Route =
    concat(
      pathPrefix("annotation") {
        import annotationService._
        concat(
          path("sync-trace") {
            get {
              complete(syncTrace)
            }
          },
          path("sync-trace-nested") {
            get {
              complete(syncTraceNested)
            }
          },
          path("async-trace") {
            get {
              onSuccess(asyncTrace)(complete(_))
            }
          },
          path("async-trace-nested") {
            get {
              onSuccess(asyncTraceNested)(complete(_))
            }
          },
          path("async-trace-nested-for") {
            get {
              onSuccess(asyncTraceNestedFor)(complete(_))
            }
          }
        )
      },
      pathPrefix("dsl") {
        import dslService._
        concat(
          path("async-trace") {
            get {
              onSuccess(asyncTrace)(complete(_))
            }
          },
          path("async-trace-nested") {
            get {
              onSuccess(asyncTraceNested)(complete(_))
            }
          },
          path("async-trace-nested-for") {
            get {
              onSuccess(asyncTraceNestedFor)(complete(_))
            }
          },
          path("async-trace-nested-chain") {
            get {
              onSuccess(asyncTraceChain)(complete(_))
            }
          }
        )
      }
    )

  object annotationService {
    @Trace
    def syncTrace = "sync-trace"

    @Trace
    def syncTraceNested = "sync-traced-nested and then " + syncTrace

    @Trace(async = true)
    def asyncTrace = Future("async-trace")

    @Trace(async = true)
    def asyncTraceNestedFor: Future[String] = for {
      topMsg    <- Future("async-trace-nested and then ")
      nestedMsg <- asyncTrace
    } yield topMsg + nestedMsg

    @Trace(async = true)
    def asyncTraceNested: Future[String] =
      Future("async-trace-nested and then ")
        .flatMap(topMsg =>
          asyncTrace
            .map(nestedMsg => topMsg + nestedMsg)
        )
  }

  object dslService {
    import instrumentation.FutureOps._
    def asyncTrace = Future(traceBody("asyncTrace")("async-trace"))

    def asyncTraceNestedFor: Future[String] = for {
      topMsg    <- Future(traceBody("topMsgFor")("async-trace-nested for and then "))
      nestedMsg <- asyncTrace
    } yield topMsg + nestedMsg

    def asyncTraceNested: Future[String] =
      Future(traceBody("topMsgMFM")("async-trace-nested and then "))
        .flatMap(
          traceFunc("nestedMsgMFM")(topMsg =>
            asyncTrace
              .map(nestedMsg => topMsg + nestedMsg)
          )
        )

    def asyncTraceChain: Future[String] =
      Future(traceBody("topMsg")("async-trace-chain and then "))
        .map(traceFunc("map1")(_ + ", map1"))
        .flatMap(traceFunc("flatmap")(res => Future(res + ", flatmap")))
        .map(traceFunc("map2")(_ + ", map2"))
        .filter(traceFunc("filter")(_.startsWith("async")))
  }
}
