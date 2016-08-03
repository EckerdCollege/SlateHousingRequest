package edu.eckerd.integrations.slate.housing.application

import java.sql.Timestamp

import cats.implicits._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import edu.eckerd.integrations.slate.housing.application.methods.HousingRequestMethods
import edu.eckerd.integrations.slate.housing.application.persistence.{DBFunctions, HasDB}
import edu.eckerd.integrations.slate.core.Request
import edu.eckerd.integrations.slate.housing.application.models.{HousingAgreement, HousingApplication, HousingRequest}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by davenpcm on 6/17/16.
  */
object ApplicationMain extends App {
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

  implicit val system = ActorSystem("HousingSystem")
  implicit val materializer = ActorMaterializer()

  val newStudentHousingAgreementRequest = Request.forConfig[HousingAgreement]("newStudentHousingAgreement")
    .retrieve()
    .flatMap(Future.traverse(_)(HousingAgreementHandler.UpdateDatabase))

  val currentStudentHousingAgreementRequest = Request.forConfig[HousingAgreement]("currentStudentHousingAgreement")
    .retrieve()
    .flatMap(Future.traverse(_)(HousingAgreementHandler.UpdateDatabase))

  val housingApplicationRequest = Request.forConfig[HousingApplication]("housingApplication")
    .retrieve()
    .flatMap(Future.traverse(_)(HousingApplicationHandler.UpdateDatabase))





}
