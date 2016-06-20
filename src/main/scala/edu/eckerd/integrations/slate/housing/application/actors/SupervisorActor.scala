package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse

/**
  * Created by davenpcm on 6/17/16.
  */
class SupervisorActor extends Actor with ActorLogging {
  import SupervisorActor._

  val DBSupervisor = context.actorOf(DatabaseSupervisorActor.props , "DBSupervisor")

  def receive() = {
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
        context.actorOf(
          Props(
            classOf[BannerHousingRequestActor],
            HousingRequest.id,
            HousingRequest.term
          ),
          name = s"BHRA-${HousingRequest.id}"
        )
      }
    case PidmRequest(id) =>
      DBSupervisor ! DatabaseSupervisorActor.Function(sender(), DatabaseSupervisorActor.GetPidm , id)
//      getPidmActor.tell( GetPidmActor.ID(id), sender() )
    case TerminateSys =>
      context.system.terminate()
  }
}

object SupervisorActor {
  val props = Props[SupervisorActor]
  case object TerminateSys
  case class PidmRequest(id: String)
  case class Request(link: String, userName: String, password: String)
}
