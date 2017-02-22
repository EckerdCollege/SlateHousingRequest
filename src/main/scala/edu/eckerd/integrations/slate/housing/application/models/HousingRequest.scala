package edu.eckerd.integrations.slate.housing.application.models

/**
  * Created by davenpcm on 6/17/16.
  * This is an algebraic datatype that because identical allows us to push two seperate data types through the same
  * Function by having the functions utilize Housing Request rather than HousingAgreement or HousingApplication.
  */
sealed trait HousingRequest{
  val id: String
  val term: String
}

case class HousingAgreement(id: String, term: String) extends HousingRequest
case class HousingApplication(id: String, term: String) extends HousingRequest


