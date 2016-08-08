package edu.eckerd.integrations.slate.housing.application.methods

import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._
import cats.Applicative._
import cats.data.Xor
import edu.eckerd.integrations.slate.housing.application.models.{HousingAgreement, HousingApplication, HousingRequest}
/**
  * Created by davenpcm on 8/2/16.
  */
trait HousingRequestMethods {

  type PidmResponder = String => Future[Option[String]]
  def pidmResponder: PidmResponder
  type UpdateResponder = (String, String) => Future[Int]
  def updateResponder : UpdateResponder
  def timeResponder : java.sql.Timestamp


  def generateTermCode(housingRequest: HousingRequest):Xor[String,String] = housingRequest match {
    case _: HousingApplication =>
      val term = housingRequest.term
      val year = term.takeRight(4)
      val key = term.dropRight(5)
      val codeEnd = TERM_CODE_MAP.get(key)
      val termCode = codeEnd.map(year + _)
      Xor.fromOption(termCode, s"Invalid Term - $housingRequest")
    case _ : HousingAgreement =>
      val term = housingRequest.term
      val year = term.take(4)
      val key = term.drop(5)
      val codeEnd = TERM_CODE_MAP.get(key)
      val termCode = codeEnd.map(year + _)
      Xor.fromOption(termCode, s"Invalid Term - $housingRequest")
  }

  def generatePidm(optPidm: Option[String], housingRequest: HousingRequest): Xor[String, String] = {
    Xor.fromOption(optPidm, s"NonExistent Pidm - $housingRequest")
  }

  def UpdateDatabase(housingRequest: HousingRequest)(implicit ec: ExecutionContext): Future[Xor[String, Int]] = {
    val f = for {
      optPidm <- pidmResponder(housingRequest.id)
    } yield for {
      term <- generateTermCode(housingRequest)
      pidm <- generatePidm(optPidm, housingRequest)
    } yield updateResponder(pidm, term)
    futureXorFutureConverter(f)
  }

  lazy val TERM_CODE_MAP: Map[String, String] = Map(
    "AUTUMN" -> "10",
    "Autumn" -> "10",
    "autumn" -> "10",
    "AUTUMN TERM" -> "10",
    "Autumn Term" -> "10",
    "Autumn term" -> "10",
    "autumn term" -> "10",
    "FALL" -> "10",
    "Fall" -> "10",
    "fall" -> "10",
    "FALL TERM" -> "10",
    "Fall Term" -> "10",
    "Fall term" -> "10",
    "fall term" -> "10",
    "WINTER" -> "20",
    "Winter" -> "20",
    "winter" -> "20",
    "WINTER TERM" -> "20",
    "Winter Term" -> "20",
    "Winter term" -> "20",
    "winter term" -> "20",
    "SPRING" -> "20",
    "Spring" -> "20",
    "spring" -> "20",
    "SPRING TERM" -> "20",
    "Spring Term" -> "20",
    "Spring term" -> "20",
    "spring term" -> "20",
    "SUMMER" -> "30",
    "Summer" -> "30",
    "summer" -> "30",
    "SUMMER TERM" -> "30",
    "Summer Term" -> "30",
    "Summer term" -> "30",
    "summer term" -> "30"
  )

  def xorFutureConverter[A, B](xor: Xor[B, Future[A]])
                              (implicit ec: ExecutionContext): Future[Xor[B,A]] = {
    xor.bimap((a) => Future.successful(a), (b) => b).bisequence
  }

  def futureXorFutureConverter[A,B](future: Future[Xor[B, Future[A]]])
                                   (implicit ec: ExecutionContext): Future[Xor[B, A]] = {
    for {
      xor <- future
      result <- xorFutureConverter(xor)
    } yield result
  }
}
