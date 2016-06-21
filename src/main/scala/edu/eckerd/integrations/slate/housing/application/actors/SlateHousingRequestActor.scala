package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, headers}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol
import edu.eckerd.integrations.slate.housing.application.models.HousingRequest
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse
import scala.concurrent.duration._
import language.postfixOps
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.{Await, Future}

/**
  * Created by davenpcm on 6/17/16.
  */
trait HousingRequestJsonProtocols extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val requestFormat = jsonFormat2(HousingRequest.apply)
  implicit val responseFormat = jsonFormat1(HousingRequestResponse.apply)
}

class SlateHousingRequestActor(link: String, userName: String, password: String) extends Actor with ActorLogging with HousingRequestJsonProtocols {
  import SlateHousingRequestActor._
  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val http = Http(context.system)

  var Requests = scala.collection.mutable.Set[ActorRef]()


  override def preStart() = {
    val authorization = headers.Authorization(
      BasicHttpCredentials(userName, password)
    )
    http.singleRequest(
      HttpRequest(
        uri = link,
        headers = List(authorization)
      )
    ).pipeTo(self)


  }


  def receive() = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
//      log.info("Got Good Status Code Reply")
      Unmarshal(entity).to[HousingRequestResponse].pipeTo(context.self)
      http.shutdownAllConnectionPools()
    case HttpResponse(code, _, _, _) =>
      log.error("Invalid Status Code: " + code)
      http.shutdownAllConnectionPools()
    case Terminated(actorRef) =>
      Requests -= actorRef
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
    case TerminateRequest =>
      implicit val timeout = Timeout(2 seconds)
      val finalRequests = Requests.toList
      val currentRequests = finalRequests.length
      log.debug(s"$currentRequests requests open at termination")
      val statusOfOpen = finalRequests
        .map(
          a => ask(a, BannerHousingRequestActor.StatusRequest).mapTo[String]
        )
      val f = Future.sequence(statusOfOpen)
      val statuses = Await.result(f, timeout.duration)
      finalRequests.zip(statuses).foreach(z => log.error( s"${z._1} - ${z._2}"))
      Requests.clear()
      context.self ! PoisonPill
    case a =>
      log.error(a.toString)
      log.error("Unknown Message Received")
      http.shutdownAllConnectionPools()
  }
}

object SlateHousingRequestActor {

  case object CheckFinished
  case object TerminateRequest
//  val props = Props(SlateHousingRequestActor]
//  case class HttpResponse(status: StatusCode, headers: Seq[HttpHeader], entity: ResponseEntity, protocol: HttpProtocol)
}
