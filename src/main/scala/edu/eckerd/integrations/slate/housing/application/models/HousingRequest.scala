package edu.eckerd.integrations.slate.housing.application.models

/**
  * Created by davenpcm on 6/17/16.
  */
sealed trait HousingUpdate{
  val id: String
  val term: String
}
case class HousingRequest(
                         id: String,
                         term: String
                         ) extends HousingUpdate
