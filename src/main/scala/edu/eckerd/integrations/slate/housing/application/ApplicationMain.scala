package edu.eckerd.integrations.slate.housing.application

import akka.actor.ActorSystem
import edu.eckerd.integrations.slate.housing.application.actors.SupervisorActor
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by davenpcm on 6/17/16.
  */
object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")
  val supervisor = system.actorOf(SupervisorActor.props, "Supervisor")
  supervisor ! SupervisorActor.Request(
    "",
    "",
    ""
  )

  Await.result(system.whenTerminated, Duration.Inf)
}
