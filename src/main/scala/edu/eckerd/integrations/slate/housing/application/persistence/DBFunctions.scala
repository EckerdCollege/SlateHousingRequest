package edu.eckerd.integrations.slate.housing.application.persistence

import edu.eckerd.integrations.slate.housing.application.models.HousingRequest
import slick.dbio.DBIO
import slick.driver.JdbcProfile
import slick.jdbc.GetResult

import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by davenpcm on 8/2/16.
  */
trait DBFunctions {
  val profile: slick.driver.JdbcProfile

  /**
    * This function is about as simple as they come. It takes a bannerID as a String, queries banner looking for a
    * Pidm using the gwf_get_pidm function, and then translates that to a BigDecimal Type which is the appropriate
    * column type for Pidms
    * @param bannerID The banner ID
    * @param ec The execution context to fork futures from
    * @param db The database to fetch information from
    * @return An option of a PIDM if the functioin returns one.
    */
  def getPidmFromBannerID(bannerID: String)(implicit ec: ExecutionContext, db: JdbcProfile#Backend#Database): Future[Option[Int]] = {
    import profile.api._

    val id = bannerID.toUpperCase
    val action = sql"""SELECT gwf_get_pidm($id, 'E') from sys.dual""".as[Option[String]]
    val newAction = action.head
    db.run(newAction).map(_.map(_.toInt))
  }

//  def UpdateStudentHousingRequest(housingRequest: HousingRequest)
//                                 (implicit db: JdbcProfile#Backend#Database, ec: ExecutionContext): Future[Int] = {
//
//    for {
//      pidmOpt <- getPidmFromBannerID(housingRequest.id)
//
//    }
//  }

  def UpdateStudentHousingRequestAction(pidm: String, termCode: String): DBIO[Int] = {
    import profile.api._
    sqlu"""UPDATE SARADAP
      SET SARADAP_SITE_CODE = 'DA'
      WHERE
      SARADAP_PIDM = (${pidm})
      AND SARADAP_TERM_CODE_ENTRY = (${termCode})
      AND SARADAP_SITE_CODE = 'D'"""
  }



}
