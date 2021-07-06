package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.typed.ActorSystem

class TraceRoutes()(implicit val system: ActorSystem[_]) {

  implicit val ec: ExecutionContext = system.executionContext

  val traceRoutes: Route =
    concat(
      pathPrefix("xml") {
        import xmlService._
        concat(
          path("xml-sync-trace") {
            get {
              complete(xmlSyncTrace)
            }
          },
          path("xml-sync-trace-nested") {
            get {
              complete(xmlSyncTraceNested)
            }
          },
          path("xml-async-trace") {
            get {
              onSuccess(xmlAsyncTrace)(complete(_))
            }
          },
          path("xml-async-trace-nested") {
            get {
              onSuccess(xmlAsyncTraceNested)(complete(_))
            }
          },
          path("xml-async-trace-nested-for") {
            get {
              onSuccess(xmlAsyncTraceNestedFor)(complete(_))
            }
          }
        )
      }
    )

  object xmlService {
    def xmlSyncTrace = "sync-trace"

    def xmlSyncTraceNested: String = "sync-traced-nested and then " + xmlSyncTrace

    def xmlAsyncTrace: Future[String] = Future("async-trace")

    def xmlAsyncTraceNestedFor: Future[String] = for {
      topMsg    <- Future("async-trace-nested and then ")
      nestedMsg <- xmlAsyncTrace
    } yield topMsg + nestedMsg

    def xmlAsyncTraceNested: Future[String] =
      Future("async-trace-nested and then ")
        .flatMap(topMsg =>
          xmlAsyncTrace
            .map(nestedMsg => topMsg + nestedMsg)
        )
  }
}
