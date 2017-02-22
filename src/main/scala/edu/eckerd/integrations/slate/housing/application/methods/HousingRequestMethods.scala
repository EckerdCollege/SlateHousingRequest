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

  /**
    * These are our responders or if you will our interfaces to external data, as well as time because time keeps
    * changing so fixing time allows us to test.
    *
    * The First is the Pidm Responder which is the operation that goes to the database. This is the way we get
    * the identifier for the person
    *
    * The update responder is the process which will A Tuple2 to a Future of Int representing the number of rows
    * affected by the change
    *
    * Finally timeResponder is just a way for us to do testing so we can know what time we will receive.
    */

  type PidmResponder = String => Future[Option[String]]
  def pidmResponder: PidmResponder
  type UpdateResponder = (String, String) => Future[Int]
  def updateResponder : UpdateResponder
  def timeResponder : java.sql.Timestamp


  /**
    * This function takes the Housing Request and Generates The Term Code - Form "201610" - In the current case
    * Housing Applications requests come in the form "Fall 2016"/"Autumn Term 2016". As a result we parse these
    * and then put time value through our Map to generate the 10/20/30. In the case of Housing agreements it is in
    * the opposite order they come in "2016 Autumn" So we do the same but with reverse picking off. If it is
    * unable to process It Goes to the left of the Xor So we keep the String showing whate the problem with the record
    * is.
    *
    * @param housingRequest The Housing Request to Process
    * @return An Xor on the Left is a string containing any errors and on the right a string representing a valid
    *         term code
    */
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

  /**
    * This takes The Option returned from the database and the original housing request and transforms it
    * to an Xor. Where we are told we did not receive the pidm on the left and on the right is the pidm if it
    * succeeded
    * @param optPidm The pidm returned from the database
    * @param housingRequest The original housing request that we are looking to process
    * @return
    */
  def generatePidm(optPidm: Option[String], housingRequest: HousingRequest): Xor[String, String] = {
    Xor.fromOption(optPidm, s"NonExistent Pidm - $housingRequest")
  }

  /**
    * This function takes the initial housing request and does all the work. It is the Entryway. First it gets the
    * pidm from the Database. Then it attempts to generate the term code, then it parses the pidm Option to an Xor.
    * Then it utilizes the update responder to update the database and the record is finished processing.
    * @param housingRequest The housing request to process
    * @param ec The execution context as this will fork off as multiple housing requests are processed
    * @return The return is a Future Xor of String(containing any errors) or Int(How many rows affected)
    */
  def UpdateDatabase(housingRequest: HousingRequest)(implicit ec: ExecutionContext): Future[Xor[String, Int]] = {
    val f = for {
      optPidm <- pidmResponder(housingRequest.id)
    } yield for {
      term <- generateTermCode(housingRequest)
      pidm <- generatePidm(optPidm, housingRequest)
    } yield updateResponder(pidm, term)
    futureXorRightFutureConverter(f)
  }

  /**
    * Functional Future Transformation. We have created a Future on the Left Out Of a Future So We would like
    * the result of the composition of the two futures where the left is Not a Future. Or else this will compound
    * your left futures. This sequences out after mapping the left to a Future So we can compound futures on the
    * desugared flatmap call.
    * @param future The Future Xor of a Future Right we are transforming
    * @param ec The execution context to merge the futures through
    * @tparam A Any Type A
    * @tparam B Any Type B - Although Don't use this Where B is a Future[_] that would completely
    *           defeat the purpose.
    * @return A Future[Xor[B,A]] - Beautiful as we can work on the event as a whole.
    */
  def futureXorRightFutureConverter[A,B](future: Future[Xor[B, Future[A]]])
                                        (implicit ec: ExecutionContext): Future[Xor[B, A]] = for {
    xor <- future
    result <- xor.bimap((a) => Future.successful(a), (b) => b).bisequence
  } yield result

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
}
