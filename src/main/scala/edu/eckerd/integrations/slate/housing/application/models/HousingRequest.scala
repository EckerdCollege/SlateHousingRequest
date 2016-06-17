package edu.eckerd.integrations.slate.housing.application.models

/**
  * Created by davenpcm on 6/17/16.
  */
case class HousingRequest(
                         id: String,
                         term: String
                         ) {
}

object HousingRequest{

//  val termMap = Map(
//    "Autumn Term" -> "10",
//    "Fall" -> "10",
//    "Winter Term" -> "20",
//    "Spring" -> "20"
//  )
//
//  def apply(id: String, slateTerm: String): HousingRequest = {
//    val stringTerm = slateTerm.substring(0, -5)
//    val term = termMap(stringTerm)
//    val year = slateTerm.substring(-4)
//    val termCode = year + term
//
//    new HousingRequest(id, termCode)
//  }

}
