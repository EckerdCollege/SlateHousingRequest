package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, headers}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol
import edu.eckerd.integrations.slate.housing.application.models.HousingRequest
import edu.eckerd.integrations.slate.housing.application.models.HousingRequestResponse
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.model.HttpProtocol
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

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
      Unmarshal(entity).to[HousingRequestResponse].pipeTo(context.parent)
      http.shutdownAllConnectionPools()
    case HttpResponse(code, _, _, _) =>
      log.error("Invalid Status Code: " + code)
      http.shutdownAllConnectionPools()
    case a =>
      log.error(a.toString)
      log.error("Unknown Message Received")
      http.shutdownAllConnectionPools()
  }
}

object SlateHousingRequestActor {
//  val props = Props(SlateHousingRequestActor]
//  case class HttpResponse(status: StatusCode, headers: Seq[HttpHeader], entity: ResponseEntity, protocol: HttpProtocol)
}
