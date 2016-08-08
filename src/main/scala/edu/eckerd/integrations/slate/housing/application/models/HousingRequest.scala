package edu.eckerd.integrations.slate.housing.application.models

/**
  * Created by davenpcm on 6/17/16.
  */
sealed trait HousingRequest{
  val id: String
  val term: String
}

case class HousingAgreement(id: String, term: String) extends HousingRequest
case class HousingApplication(id: String, term: String) extends HousingRequest


