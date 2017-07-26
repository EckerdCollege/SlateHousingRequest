package edu.eckerd.integrations.slate.housing.application

import java.sql.Timestamp

import cats.implicits._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import edu.eckerd.integrations.slate.housing.application.methods.HousingRequestMethods
import edu.eckerd.integrations.slate.housing.application.persistence.{DBFunctions, HasDB}
import edu.eckerd.integrations.slate.core.Request
import edu.eckerd.integrations.slate.housing.application.models.{HousingAgreement, HousingApplication, HousingRequest}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import edu.eckerd.integrations.slate.housing.application.request.HousingJsonProtocol._

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by davenpcm on 6/17/16.
  */
object ApplicationMain extends App with LazyLogging {

  /**
    * This is where we mix and match our DB functions and mutable state seperately for the two types because
    * our application handles them the same except for the endpoint they update.
    */
  object HousingAgreementHandler extends HousingRequestMethods with DBFunctions with HasDB {
    override implicit val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("oracle")
    override def pidmResponder: this.PidmResponder = getPidmFromBannerID
    override def timeResponder: Timestamp = new java.sql.Timestamp(new java.util.Date().getTime)
    override def updateResponder: UpdateResponder = UpdateStudentHousingAgreement
  }

  object HousingApplicationHandler extends HousingRequestMethods with DBFunctions with HasDB {
    override implicit val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("oracle")
    override def pidmResponder: this.PidmResponder = getPidmFromBannerID
    override def timeResponder: Timestamp = new java.sql.Timestamp(new java.util.Date().getTime)
    override def updateResponder: UpdateResponder = UpdateStudentHousingApplication
  }


  /**
    * This is where we see what is essentially a linear script as The three processess occur in parralel and wait
    * till they are all complete to terminate the actor system and log any errors that occur.
    */
  implicit val system = ActorSystem("HousingSystem")
  implicit val materializer = ActorMaterializer()

  val newStudentHousingAgreementRequest = Request.forConfig[HousingAgreement]("newStudentHousingAgreement")
    .retrieve()
    .flatMap(Future.traverse(_)(HousingAgreementHandler.UpdateDatabase))

  val housingApplicationRequest = Request.forConfig[HousingApplication]("housingApplication")
    .retrieve()
    .flatMap(Future.traverse(_)(HousingApplicationHandler.UpdateDatabase))

  val f = for {
    seq <- housingApplicationRequest
    seq2 <- newStudentHousingAgreementRequest
    _ <- system.terminate()
  } yield{
    val l = seq.toList ::: seq2.toList
    for{
      either <- l
    } yield either match {
      case Left(value) =>
        logger.info(s"$value")
      case _ => ()
    }
  }

  Await.result(f, 30.seconds)

}
