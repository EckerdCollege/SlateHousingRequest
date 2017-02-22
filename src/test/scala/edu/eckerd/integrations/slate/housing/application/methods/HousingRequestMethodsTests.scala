package edu.eckerd.integrations.slate.housing.application.methods

import java.sql.Timestamp

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
/**
  * Created by davenpcm on 8/8/16.
  */
class HousingRequestMethodsTests extends FlatSpec with Matchers with HousingRequestMethods {
  override def pidmResponder: PidmResponder = {
    case "1" => Future.successful(Some("1"))
    case _ => Future.successful(None)
  }
  override def updateResponder: UpdateResponder = (_, _) => Future.successful(1)
  override def timeResponder: Timestamp = Timestamp.valueOf("2016-07-28 10:10:10.0")



}
