package edu.eckerd.integrations.slate.housing.application.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
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
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.sslconfig.akka.AkkaSSLConfig

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


  val https = Http(context.system)
    .outgoingConnectionHttps("eck.test.technolutions.net")
    .mapAsync(1){ r =>
      val hr = Unmarshal(r).to[HousingRequestResponse]
      pipe(hr).pipeTo(context.self)
    }
    .to(Sink.ignore)

  var Requests = scala.collection.mutable.Set[ActorRef]()



  override def preStart() = {
    val authorization = headers.Authorization(
      BasicHttpCredentials(userName, password)
    )

    val slateSource = Source.single(
      HttpRequest(
        uri = link,
        headers = List(authorization)
      )
    )

    val future = slateSource.runWith(https)
    Await.result(future, Duration(2, SECONDS))

  }


  def receive() = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      Unmarshal(entity).to[HousingRequestResponse].pipeTo(context.self)
    case HttpResponse(code, _, _, _) =>
      val codeVal = code.value
      if (codeVal != "500 Internal Server Error"){
        log.error("Invalid Status Code: " + codeVal)

      } else {
        log.debug(s"Server Failure : $codeVal")
      }
      context.self ! PoisonPill


    case Terminated(actorRef) =>
      Requests -= actorRef
      context.self ! CheckFinished
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
    case HRFinishedSuccessfully =>
      context.sender() ! PoisonPill
    case HRFinishedWithErrors =>
      context.sender() ! PoisonPill
    case CheckFinished =>
      if (Requests.isEmpty) context.self ! PoisonPill
  }
}

object SlateHousingRequestActor {

  case object CheckFinished
  case object TerminateRequest

  case object HRFinishedSuccessfully
  case object HRFinishedWithErrors
}
