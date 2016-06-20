package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse

/**
  * Created by davenpcm on 6/17/16.
  */
class SupervisorActor extends Actor with ActorLogging {
  import SupervisorActor._

  var Requests = scala.collection.mutable.Set[ActorRef]()

  def receive() = {
    case Terminated(actorRef) =>
      Requests -= actorRef
    case Request(link, userName, password) =>
      context.actorOf(
        Props(
          classOf[SlateHousingRequestActor],
          link,
          userName,
          password
        ),
        "slateRequest"
      )
    case HousingRequestResponse(list) =>
      list.foreach{ HousingRequest =>
        val child = context.actorOf(
          Props(
            classOf[BannerHousingRequestActor],
            HousingRequest.id,
            HousingRequest.term
          ),
          name = s"BHRA-${HousingRequest.id}"
        )
        context.watch(child)
        Requests += child
      }
      context.sender() ! PoisonPill
    case TerminateSys =>
      val currentRequests = Requests.toList.length
      log.info(s"$currentRequests requests open at termination")
      context.system.terminate()
  }
}

object SupervisorActor {
  val props = Props[SupervisorActor]
  case object TerminateSys
  case class PidmRequest(id: String)
  case class Request(link: String, userName: String, password: String)
}
