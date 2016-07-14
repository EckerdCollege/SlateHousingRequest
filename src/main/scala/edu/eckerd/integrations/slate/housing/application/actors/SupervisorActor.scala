package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.pattern.ask
import akka.util.Timeout
import language.postfixOps
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse
import concurrent.ExecutionContext.Implicits.global
import concurrent.{Await, Future}
import scala.concurrent.duration._
/**
  * Created by davenpcm on 6/17/16.
  */
class SupervisorActor extends Actor with ActorLogging {
  import SupervisorActor._

  var OpenRequests = scala.collection.mutable.Set[ActorRef]()

  def receive() = {

    case Request(link, userName, password) =>
//      OpenRequests.foreach(_ ! SlateHousingRequestActor.TerminateRequest)
      log.debug(s"Supervisor Request Received for $link")

      val child = context.actorOf(
        Props(
          classOf[SlateHousingRequestActor],
          link,
          userName,
          password
        )
      )
      OpenRequests += child
      context.watch(child)
      val OpenRequestsLength = OpenRequests.toList.length
      log.debug(s"$OpenRequestsLength Open Requests")
      if (OpenRequestsLength > 5) log.error(s"More than 5 Requests Open - Current : $OpenRequestsLength")
    case Terminated(actorRef) =>
      OpenRequests -= actorRef
      log.debug(s"Primary Request $actorRef Terminated")
      if (OpenRequests.isEmpty) {
        context.system.terminate()
        log.debug("ActorSystem Terminated")
      }

  }
}

object SupervisorActor {
  val props = Props[SupervisorActor]

  case class Request(link: String, userName: String, password: String)

}
