package edu.eckerd.integrations.slate.housing.application.methods

/**
  * Created by davenpcm on 8/2/16.
  */
trait HousingRequestMethods {
  def generateTermCode(term: String): String = {
    val year = term.takeRight(4)
    val key = term.dropRight(5)
    val codeEnd = termCodeMap(key)
    val termCode = year + codeEnd
    termCode
  }

  lazy val termCodeMap: Map[String, String] = Map(
    "Autumn Term" -> "10",
    "Fall" -> "10",
    "Winter Term" -> "20",
    "Spring" -> "20"
  )
}
