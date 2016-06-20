package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import edu.eckerd.integrations.slate.housing.application.models.HousingRequest
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import com.typesafe.slick.driver.oracle.OracleDriver.api._

import scala.util.Try
import akka.pattern.pipe

import concurrent.ExecutionContext.Implicits.global

/**
  * Created by davenpcm on 6/18/16.
  */
class BannerHousingRequestActor(id: String, term: String) extends Actor with ActorLogging {
  import BannerHousingRequestActor._

  val termCode = generateTermCode(term)
  private var internalPidm : Option[String] = None

  val dbConfig : DatabaseConfig[JdbcProfile] = edu.eckerd.integrations.slate.housing.application.utils.Database.dbConfig


  override def preStart() = {

    val Request = dbConfig.db.run(getPidm(id)).map{
      case Some(pidm) => BannerHousingRequest(pidm)
      case None => throw new Throwable("Null Pidm Returned")
    }

    pipe(Request).pipeTo(self)

  }

  def receive() = {
    case BannerHousingRequest(pidm) =>
      internalPidm = Some(pidm)
//      log.info(s"newBHR - pidm: $pidm, termCode: $termCode")
      val updatedRows = dbConfig.db.run(UpdateStudentHousingRequest(pidm, termCode)).map(Rows)
      pipe(updatedRows).pipeTo(self)
    case Rows(n) =>
//      log.info(s"$id - $n Rows Effected")
      context.stop(self)

  }

  def UpdateStudentHousingRequest(pidm: String, termCode: String): DBIO[Int] = {
    sqlu"""UPDATE SARADAP
      SET SARADAP_SITE_CODE = 'DA'
      WHERE
      SARADAP_PIDM = (${pidm})
      AND SARADAP_TERM_CODE_ENTRY = (${termCode})
      AND (SARADAP_SITE_CODE = 'D' OR SARADAP_SITE_CODE = 'DA')"""
  }

  def getPidm(id: String): DBIO[Option[String]] ={
    val idForQuery = id.toUpperCase
    val query = sql"""SELECT gwf_get_pidm('#$idForQuery', 'E') from dual""".as[Option[String]]
    query.head
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
  case object StatusRequest
  case class Status()
  case class BannerHousingRequest(pidm: String)
  case class Rows(n : Int)


}
