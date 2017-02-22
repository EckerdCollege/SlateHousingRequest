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
  val db : JdbcProfile#Backend#Database

  /**
    * This function is about as simple as they come. It takes a bannerID as a String, queries banner looking for a
    * Pidm using the gwf_get_pidm function, and then translates that to a BigDecimal Type which is the appropriate
    * column type for Pidms
    * @param bannerID The banner ID
    * @param ec The execution context to fork futures from
    * @param db The database to fetch information from
    * @return An option of a PIDM if the functioin returns one.
    */
  def getPidmFromBannerID(bannerID: String)
                         (implicit ec: ExecutionContext, db: JdbcProfile#Backend#Database): Future[Option[String]] = {
    import profile.api._

    val id = bannerID.toUpperCase
    val action = sql"""SELECT gwf_get_pidm($id, 'E') from sys.dual""".as[Option[String]]
    val newAction = action.head
    db.run(newAction)
  }

  /**
    * This executes the updateStudentHousingApplication Action
    * @param pidm The pidm to change
    * @param termCode The term code to update
    * @param ec The execution context to fork from
    * @param db The database to modify
    * @return An int corresponding to the number of rows affecting after it has returned from the database
    */
  def UpdateStudentHousingApplication(pidm: String, termCode: String)
                                     (implicit ec: ExecutionContext,
                                      db: JdbcProfile#Backend#Database): Future[Int] = {
    val action = updateStudentHousingApplicationAction(pidm, termCode)
    db.run(action)
  }

  /**
    * This executes the updateStudentHousingAgreement Action
    * @param pidm The pidm to change
    * @param termCode The term code to update
    * @param ec The execution context to fork from
    * @param db The database to modify
    * @return An int corresponding to the number of rows affecting after it has returned from the database
    */
  def UpdateStudentHousingAgreement(pidm: String, termCode: String)
                                   (implicit ec:ExecutionContext,
                                    db: JdbcProfile#Backend#Database): Future[Int] = {
    val action = updateStudentHousingAgreementAction(pidm, termCode)
    db.run(action)
  }

  /**
    * This is the DBIO operation for updating the student housing application. It transitions for a given student
    * and a given term code entry their site code from D to DA.
    * @param pidm The pidm to update
    * @param termCode The term to update for
    * @return An int corresponding to the number of rows affected. Should be 1 or 0.
    */
  def updateStudentHousingApplicationAction(pidm: String, termCode: String)
                                           : DBIO[Int] = {
    import profile.api._
    sqlu"""UPDATE SARADAP
      SET SARADAP_SITE_CODE = 'DA'
      WHERE
      SARADAP_PIDM = (${pidm})
      AND SARADAP_TERM_CODE_ENTRY = (${termCode})
      AND SARADAP_SITE_CODE = 'D'"""
  }

  /**
    * This is the DBIO operation for updating the Student Housing Agreement. It transitions toe ARTP_CODE to "HMAP"
    * and we guarantee this only happens if it is not already in HMAP. We specify the individual using PIDM and
    * FROM_TERM
    * @param pidm The pidm of the user the alter
    * @param termCode The term code to alter for
    * @return An Int corresponding to the number of rows affected. 0 or 1.
    */
  def updateStudentHousingAgreementAction(pidm: String, termCode: String): DBIO[Int] = {
    import profile.api._
    sqlu"""UPDATE SLBRMAP
           SET SLBRMAP_ARTP_CODE='HMAP'
           WHERE SLBRMAP_PIDM = $pidm
           AND SLBRMAP_FROM_TERM = $termCode
           AND SLBRMAP_ARTP_CODE <> 'HMAP'"""
  }



}
