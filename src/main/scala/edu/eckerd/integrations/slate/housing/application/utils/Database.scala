package edu.eckerd.integrations.slate.housing.application.utils

import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

/**
  * Created by davenpcm on 6/20/16.
  */
object Database {
  val dbConfig : DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("oracle")
}
