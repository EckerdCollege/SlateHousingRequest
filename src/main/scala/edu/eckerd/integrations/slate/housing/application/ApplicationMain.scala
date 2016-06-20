package edu.eckerd.integrations.slate.housing.application

import akka.actor.ActorSystem
import edu.eckerd.integrations.slate.housing.application.actors.SupervisorActor

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

/**
  * Created by davenpcm on 6/17/16.
  */
object ApplicationMain extends App {
  val system = ActorSystem("HRSys")
  val supervisor = system.actorOf(SupervisorActor.props, "super")
  supervisor ! SupervisorActor.Request(
    "",
    "",
    ""
  )

  Thread.sleep(5000)
  supervisor ! SupervisorActor.TerminateSys

  Await.result(system.whenTerminated, Duration(20, "seconds"))
}
