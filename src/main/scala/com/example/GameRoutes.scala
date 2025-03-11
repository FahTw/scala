package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.GameRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

class GameRoutes(gameRegistry: ActorRef[GameRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  // ✅ ตั้งค่า Timeout ให้กับ ask pattern
  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  // ✅ ฟังก์ชันติดต่อกับ GameRegistry
  def getGames(): Future[Games] =
    gameRegistry.ask(GetGames.apply)
  def getGame(id: Int): Future[GetGameResponse] =
    gameRegistry.ask(GetGame(id, _))
  def createGame(game: Game): Future[ActionPerformed] =
    gameRegistry.ask(CreateGame(game, _))
  def deleteGame(id: Int): Future[ActionPerformed] =
    gameRegistry.ask(DeleteGame(id, _))
  def deleteGameByTitle(title: String): Future[ActionPerformed] =
    gameRegistry.ask(DeleteGameByTitle(title, _))
  def updateGamePlatform(id: Int, platform: String): Future[ActionPerformed] =
    gameRegistry.ask(UpdateGamePlatform(id, platform, _))
  def getGameByTitle(title: String): Future[GetGameResponse] =
    gameRegistry.ask(GetGameByTitle(title, _))
  def getGameByPrice(price: Double): Future[Games] =
    gameRegistry.ask(GetGameByPrice(price, _))
  def getGameByRating(rating: Double): Future[Games] =
    gameRegistry.ask(GetGameByRating(rating, _))

  // ✅ สร้าง API Routes
  val gameRoutes: Route =
  pathPrefix("games") {
    concat(
      pathEnd {
        concat(
          get {
            complete(getGames())
          },
          post {
            entity(as[Game]) { game =>
              onSuccess(createGame(game)) { performed =>
                complete((StatusCodes.Created, performed))
              }
            }
          })
      },
      path(IntNumber) { id =>
        concat(
          get {
            rejectEmptyResponse {
              onSuccess(getGame(id)) { response =>
                complete(response.maybeGame)
              }
            }
          },
          delete {
            onSuccess(deleteGame(id)) { performed =>
              complete((StatusCodes.OK, performed))
            }
          },
          put {
            parameter("platform") { platform =>
              onSuccess(updateGamePlatform(id, platform)) { performed =>
                complete((StatusCodes.OK, performed))
              }
            }
          })
      },
      path("title" / Segment) { title =>
        concat(
          get {
            rejectEmptyResponse {
              onSuccess(getGameByTitle(title)) { response =>
                complete(response.maybeGame)
              }
            }
          },
          delete {
            onSuccess(deleteGameByTitle(title)) { performed =>
              complete((StatusCodes.OK, performed))
            }
          })
      },
      path("price" / DoubleNumber) { price =>
        get {
          onSuccess(getGameByPrice(price)) { games =>
            complete(games)
          }
        }
      },
      path("rating" / DoubleNumber) { rating =>
        get {
          onSuccess(getGameByRating(rating)) { games =>
            complete(games)
          }
        }
      }
    )
  }
}