package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, Props}
import slick.dbio.DBIO
import com.typesafe.slick.driver.oracle.OracleDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import akka.pattern.pipe

/**
  * Created by davenpcm on 6/18/16.
  */
class GetPidmActor(dbConfig: DatabaseConfig[JdbcProfile]) extends Actor with ActorLogging {
  import GetPidmActor._

  import dbConfig.db


  def receive() = {
    case ID(id) =>
      log.info(s"Request for id - $id")

      val dbio = getPidm(id)
      val dbReceive = db.run(dbio)
      val pidm = dbReceive.map{
        case Some(value) => Pidm(value)
        case None => log.error(s"ID - $id lacks a PIDM")
      }

      pipe(pidm).pipeTo(context.sender())
  }

  def getPidm(id: String): DBIO[Option[String]] ={
    val idForQuery = id.toUpperCase
    val query = sql"""SELECT gwf_get_pidm('#$idForQuery', 'E') from dual""".as[Option[String]]
    query.head
  }
}

object GetPidmActor{
  def  props(dbConfig : DatabaseConfig[JdbcProfile]) = Props(classOf[GetPidmActor], dbConfig)

  case class ID(id: String)
  case class Pidm(pidm: String)




}
