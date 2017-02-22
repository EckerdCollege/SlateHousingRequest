package edu.eckerd.integrations.slate.housing.application.request

import edu.eckerd.integrations.slate.core.DefaultJsonProtocol
import edu.eckerd.integrations.slate.housing.application.models.{HousingAgreement, HousingApplication, HousingRequest}
/**
  * Created by davenpcm on 8/2/16.
  * This is where we generate the json interpretation formats for our classes so that we can extract them from
  * the Slate Response
  */
object HousingJsonProtocol extends DefaultJsonProtocol {
  implicit val HousingAgreementResponseFormat = jsonFormat2(HousingAgreement)
  implicit val HousingApplicationResponseFormat = jsonFormat2(HousingApplication)
}
