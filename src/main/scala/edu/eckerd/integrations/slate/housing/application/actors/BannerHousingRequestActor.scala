package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, Props}
import edu.eckerd.integrations.slate.housing.application.models.HousingRequest

import scala.util.Try

/**
  * Created by davenpcm on 6/18/16.
  */
class BannerHousingRequestActor(id: String, term: String) extends Actor with ActorLogging {
  import BannerHousingRequestActor._

  val termCode = generateTermCode(term)



  override def preStart() = {
    self ! EchoStart
    context.parent ! SupervisorActor.PidmRequest(id)
  }

  def receive() = {
    case EchoStart => log.info(s"BHR Start: $id - $termCode")
    case GetPidmActor.Pidm(pidm) =>
      log.info(s"BHR: pidm - $pidm")
    case a =>
      log.error(a.toString)
      log.error("Unknown Message Received")
  }

}

object BannerHousingRequestActor {

  def generateTermCode(term: String): String = {
    val year = term.takeRight(4)
    assert(Try(year.toInt).isSuccess, "Last Four Characters of Term Should Be Year, i.e. 2016")

    val key = term.dropRight(5)
    assert(termCodeMap.contains(key), "Key String is not contained in termCodeMap")

    val codeEnd = termCodeMap(key)

    val termCode = year + codeEnd
    termCode
  }

  val termCodeMap: Map[String, String] = Map(
    "Autumn Term" -> "10",
    "Fall" -> "10",
    "Winter Term" -> "20",
    "Spring" -> "20"
  )

  case object EchoStart
  case class BannerHousingRequest(pidm: Int, termCode: String)
}
