package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import edu.eckerd.integrations.slate.housing.application.actors.GetPidmActor.ID

/**
  * Created by davenpcm on 6/20/16.
  */
class DatabaseSupervisorActor extends Actor with ActorLogging {

  import DatabaseSupervisorActor._

  val dbConfig : DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("oracle")


  val getPidmActor = context.actorOf(GetPidmActor.props(dbConfig), "getPidmActor")

  def receive() = {
    case Function(originalSender, function, passIn : String) =>
      function match {
        case GetPidm => getPidmActor.tell(ID(passIn), originalSender)
      }
    case _ =>
  }

}

object DatabaseSupervisorActor {
  val props = Props[DatabaseSupervisorActor]

  sealed trait dbAction
  case class Function[A <: FunctionType, B](originalSender: ActorRef, function : A, passIn : B )


  sealed trait FunctionType
  case object GetPidm extends FunctionType



}
