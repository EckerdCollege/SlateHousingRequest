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
  val slateConfig = config.getConfig("slate")
  val user = slateConfig.getString("user")
  val password = slateConfig.getString("password")
  val link = slateConfig.getString("link")

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
