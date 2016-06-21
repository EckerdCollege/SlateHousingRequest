package edu.eckerd.integrations.slate.housing.application

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import edu.eckerd.integrations.slate.housing.application.actors.SupervisorActor
import edu.eckerd.integrations.slate.housing.application.actors.SupervisorActor.Request
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import concurrent.duration._
import language.postfixOps

/**
  * Created by davenpcm on 6/17/16.
  */
object ApplicationMain extends App {
  val system = ActorSystem("HousingRequestSystem")


  val config = ConfigFactory.load()

  val oracleConfig = config.getConfig("oracle.db")
  val dbUser = oracleConfig.getString("user")
  if (dbUser == "") throw new Error("DB Username is Blank")
  val dbPass = oracleConfig.getString("password")

  if (dbPass == "") throw new Error("DB Password is Blank")

  val slateConfig = config.getConfig("slate")
  val user = slateConfig.getString("user")
  if (user == "") throw new Error("Slate Username is Blank")
  val password = slateConfig.getString("password")
  if (password == "") throw new Error("Slate Password is Blank")


  val link = "https://eck.test.technolutions.net/manage/query/run?id=a8274b1a-9860-4f79-91c2-268c27d6338b&h=7215c6fa-ea25-e33d-0d02-5bd30b28f30e&cmd=service&output=json"

  val supervisor = system.actorOf(SupervisorActor.props, "supervisor")

//  val recurringRequest = system.scheduler.schedule(
//    0 milliseconds,
//    500 milliseconds, //1 minutes,
//    supervisor,
//    Request(link, user, password)
//  )
  supervisor ! Request(link, user, password)

  Await.result(system.whenTerminated, Duration.Inf)
}
