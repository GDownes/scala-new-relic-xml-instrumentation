package com.example

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.actor.typed.ActorSystem

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
