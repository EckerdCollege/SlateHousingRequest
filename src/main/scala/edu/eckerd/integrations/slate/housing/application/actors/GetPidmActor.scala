package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, Props}
import edu.eckerd.integrations.slate.housing.application.actors.BannerHousingRequestActor.Pidm

/**
  * Created by davenpcm on 6/18/16.
  */
class GetPidmActor extends Actor with ActorLogging {
  import GetPidmActor._
  def receive() = {
    case ID(id) =>
      log.info(s"Request for id - $id")

      //TODO:  DUMMY IMPLEMENTATION - CHANGE
      sender() ! Pidm(id.drop(3).toInt)
  }
}

object GetPidmActor{
  val props = Props[GetPidmActor]
  case class ID(id: String)
}
