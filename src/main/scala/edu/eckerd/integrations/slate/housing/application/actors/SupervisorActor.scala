package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.pattern.ask
import akka.util.Timeout
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse
import concurrent.ExecutionContext.Implicits.global
import concurrent.{Await, Future}
import scala.concurrent.duration._
/**
  * Created by davenpcm on 6/17/16.
  */
class SupervisorActor extends Actor with ActorLogging {
  import SupervisorActor._

  var Requests = scala.collection.mutable.Set[ActorRef]()


  def receive() = {
    case Terminated(actorRef) =>
      Requests -= actorRef
    case Request(link, userName, password) =>
      context.actorOf(
        Props(
          classOf[SlateHousingRequestActor],
          link,
          userName,
          password
        )
      )
    case HousingRequestResponse(list) =>
      list.foreach{ HousingRequest =>
        val child = context.actorOf(
          Props(
            classOf[BannerHousingRequestActor],
            HousingRequest.id,
            HousingRequest.term
          )
        )
        context.watch(child)
        Requests += child
      }
      context.sender() ! PoisonPill
    case TerminateRequest =>
      implicit val timeout = Timeout(2 seconds)
      val finalRequests = Requests.toList
      val currentRequests = finalRequests.length
//      log.info(s"$currentRequests requests open at termination")
      val statusOfOpen = finalRequests
        .map(
          a => ask(a, BannerHousingRequestActor.StatusRequest).mapTo[String]
        )


      val f = Future.sequence(statusOfOpen)
      val statuses = Await.result(f, timeout.duration)
      finalRequests.zip(statuses).foreach(z => log.error( s"${z._1} - ${z._2}"))
      Requests.clear()
  }
}

object SupervisorActor {
  val props = Props[SupervisorActor]

  case object TerminateRequest
  case class Request(link: String, userName: String, password: String)

}
