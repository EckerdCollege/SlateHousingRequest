package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, Props}
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse

/**
  * Created by davenpcm on 6/17/16.
  */
class SupervisorActor extends Actor with ActorLogging {
  import SupervisorActor._

  val slateRequestActor = context.actorOf(SlateHousingRequestActor.props, "slateRequest")

  def receive() = {
    case Request(link, userName, password) => slateRequestActor ! SlateHousingRequestActor.Request(link, userName, password)
    case HousingRequestResponse(list) =>
      list.foreach(t => log.info(t.toString))
      context.system.terminate()
  }
}

object SupervisorActor {
  val props = Props[SupervisorActor]

  case class Request(link: String, userName: String, password: String)
}
