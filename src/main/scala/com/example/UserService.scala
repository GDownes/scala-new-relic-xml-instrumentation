package com.example

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import com.newrelic.api.agent.{Trace, Token, NewRelic}
import cats.effect.syntax.async

trait UserService {
  def getUsers(): Future[Users]
  def getUser(name: String): Future[GetUserResponse]
  def createUser(user: User): Future[ActionPerformed]
  def deleteUser(name: String): Future[ActionPerformed]
}

class UserRegistryService(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_])
    extends UserService {
  import UserRegistry._
  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))
}

class SlowUserService(delay: FiniteDuration) extends UserService {
  import monix.eval.Task
  import monix.execution.Scheduler.Implicits.global
  import concurrent.duration._

  var users: Set[User] = Set.empty
  @Trace(async = true)
  override def getUsers(): Future[Users] =
    scheduleFuture {
      logSomething()
      Users(users.toSeq)
    }.flatMap(sf => {
      val token = NewRelic.getAgent().getTransaction().getToken()
      asyncLinkedLogSomething(token).map(_ => sf)
    })

  @Trace(async = true)
  override def getUser(name: String): Future[GetUserResponse] =
    scheduleFuture {
      logSomething()
      GetUserResponse(users.find(_.name == name))
    }.flatMap(sf => asyncLogSomething().map(_ => sf))

  @Trace(async = true)
  override def createUser(user: User): Future[ActionPerformed] =
    scheduleFuture {
      logSomething()
      users += user
      ActionPerformed(s"User ${user.name} created.")
    }.flatMap(sf => asyncLogSomething().map(_ => sf))

  @Trace(async = true)
  override def deleteUser(name: String): Future[ActionPerformed] =
    scheduleFuture {
      logSomething()
      users = users.filterNot(_.name == name)
      ActionPerformed(s"User $name deleted.")
    }.flatMap(sf => asyncLogSomething().map(_ => sf))

  def scheduleFuture[T](body: => T): Future[T] =
    Task.now(body).delayExecution(delay).runToFuture

  @Trace
  def logSomething(): Unit =
    println("logging")

  // not logged in transaction
  @Trace(async = true)
  def asyncLogSomething(): Future[Unit] =
    Future(println("async log something"))

  @Trace(async = true)
  def asyncLinkedLogSomething(token: Token): Future[Unit] = {
    Future {
      token.link()
      println("async log something")
    }
  }
}
