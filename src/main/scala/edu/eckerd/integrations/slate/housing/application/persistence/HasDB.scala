package edu.eckerd.integrations.slate.housing.application.persistence

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by davenpcm on 8/3/16.
  * This is where we keep our Database. We force initialization by the caller but grant ourselves implicit values
  * to utilize the database in whatever implements it.
  */
trait HasDB {
  implicit val dbConfig: DatabaseConfig[JdbcProfile]
  implicit lazy val profile = dbConfig.driver
  implicit lazy val db = dbConfig.db
}
