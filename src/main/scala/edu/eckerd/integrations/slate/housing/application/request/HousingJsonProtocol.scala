package edu.eckerd.integrations.slate.housing.application.request

import edu.eckerd.integrations.slate.core.DefaultJsonProtocol
import edu.eckerd.integrations.slate.housing.application.models.HousingRequest
/**
  * Created by davenpcm on 8/2/16.
  */
object HousingJsonProtocol extends DefaultJsonProtocol {
  implicit val HousingRequestResponseFormat = jsonFormat2(HousingRequest)
}
