package edu.eckerd.integrations.slate.housing.application.request

import edu.eckerd.integrations.slate.housing.application.models.HousingAgreement
import edu.eckerd.integrations.slate.housing.application.models.HousingApplication
import org.scalatest.{FlatSpec, Matchers}
import spray.json.{JsObject, JsString}
import spray.json._

/**
  * Created by davenpcm on 8/8/16.
  */
class HousingJsonProtocolTests extends FlatSpec with Matchers {
  import HousingJsonProtocol._

  "HousingAgreementResponseFormat" should "write a HousingAgreement to json" in {
    val ha = HousingAgreement("1", "Spring 2016")
    val js = JsObject("id" -> JsString("1"), "term" -> JsString("Spring 2016"))
    HousingAgreementResponseFormat.write(ha) should be (js)
  }

  it should "take parsed Json to a Housing Agreement" in {
    val ha = HousingAgreement("1", "Spring 2016")
    val js = JsObject("id" -> JsString("1"), "term" -> JsString("Spring 2016"))
    HousingAgreementResponseFormat.read(js) should be (ha)
  }

  it should "parse a String to Housing Agreement" in {
    HousingAgreementResponseFormat.read(JsonParser("""{"id":"1","term":"Spring 2016"}""")) should be
    (HousingAgreement("1", "Spring 2016"))
  }

  "HousingApplicationResponseFormat" should "write a HousingApplication to Json" in {
    val ha = HousingApplication("1", "Spring 2016")
    val js = JsObject("id" -> JsString("1"), "term" -> JsString("Spring 2016"))
    HousingApplicationResponseFormat.write(ha) should be (js)
  }

  it should "take parsed Json to a Housing Agreement" in {
    val ha = HousingApplication("1", "Spring 2016")
    val js = JsObject("id" -> JsString("1"), "term" -> JsString("Spring 2016"))
    HousingApplicationResponseFormat.read(js) should be (ha)
  }

  it should "parse a String to Housing Agreement" in {
    HousingApplicationResponseFormat.read(JsonParser("""{"id":"1","term":"Spring 2016"}""")) should be
    (HousingApplication("1", "Spring 2016"))
  }

}
