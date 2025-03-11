package com.example

import com.example.GameRegistry.{ActionPerformed, Game, Games}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats {
  // Import default encoders for primitive types (Int, String, Lists, etc.)
  import DefaultJsonProtocol._

  // ตรวจสอบว่า Game ถูกประกาศเป็น case class แยกจาก object
  implicit val gameJsonFormat: RootJsonFormat[Game] = jsonFormat5(Game)
  implicit val gamesJsonFormat: RootJsonFormat[Games] = jsonFormat1(Games)

  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
}