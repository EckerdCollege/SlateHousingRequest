package edu.eckerd.integrations.slate.housing.application.persistence


import org.scalatest.{FlatSpec, Matchers}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by davenpcm on 8/8/16.
  */
class DBFunctionsTests extends FlatSpec with Matchers with DBFunctions with HasDB {


  override implicit val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("oracle")
  import profile.api._

  Await.result(
    db.run(DBIO.seq(
      sqlu"""CREATE TABLE SLBRMAP
          (SLBRMAP_PIDM VARCHAR,
          SLBRMAP_ARTP_CODE VARCHAR,
          SLBRMAP_FROM_TERM VARCHAR)
        """,
      sqlu"""INSERT INTO SLBRMAP VALUES ('1', 'HOME', '201610')"""
    )),
    2.seconds
  )

  "UpdateStudentHousingAgreement" should "update the ARTP code" in {
    Await.result(UpdateStudentHousingAgreement("1", "201610"), 2.seconds) should be (1)
  }

  it should "do nothing if repeated" in {
    Await.result(UpdateStudentHousingAgreement("1", "201610"), 2.seconds) should be (0)
  }

  Await.result(
    db.run(
      DBIO.seq(
        sqlu"""CREATE TABLE SARADAP
              (SARADAP_PIDM VARCHAR, SARADAP_SITE_CODE VARCHAR, SARADAP_TERM_CODE_ENTRY VARCHAR)
            """,
        sqlu"""INSERT INTO SARADAP VALUES ('1', 'D', '201610')"""
      )
    ),
    2.seconds
  )

  "UpdateStudentHousingApplication" should "Update the Site Code" in {
   Await.result(UpdateStudentHousingApplication("1", "201610"), 2.seconds) should be (1)
  }

  it should "do nothing if repeated" in {
    Await.result(UpdateStudentHousingApplication("1", "201610"), 2.seconds) should be (0)
  }
}
