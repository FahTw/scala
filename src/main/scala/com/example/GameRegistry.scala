package com.example

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.Behaviors

object GameRegistry {

  // ✅ 1. สร้าง case class สำหรับข้อมูลเกม
  final case class Game(id: Int, title: String, price: Double, rating: Double, platform: String)
  final case class Games(games: Seq[Game])

  // ✅ 2. คำสั่ง (Commands) สำหรับ Actor
  sealed trait Command
  final case class GetGame(id: Int, replyTo: ActorRef[GetGameResponse]) extends Command
  final case class GetGames(replyTo: ActorRef[Games]) extends Command
  final case class GetGameByTitle(title: String, replyTo: ActorRef[GetGameResponse]) extends Command
  final case class GetGameByPrice(price: Double, replyTo: ActorRef[Games]) extends Command
  final case class GetGameByRating(rating: Double, replyTo: ActorRef[Games]) extends Command
  final case class CreateGame(game: Game, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class DeleteGame(id: Int, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class DeleteGameByTitle(title: String, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class UpdateGamePlatform(id: Int, platform: String, replyTo: ActorRef[ActionPerformed]) extends Command

  // ✅ 3. Response Models
  final case class GetGameResponse(maybeGame: Option[Game])
  final case class ActionPerformed(description: String)

  // ✅ 4. สร้าง Actor สำหรับจัดเก็บเกม
  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(games: Set[Game]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetGames(replyTo) =>
        replyTo ! Games(games.toSeq)
        Behaviors.same

      case GetGame(id, replyTo) =>
        replyTo ! GetGameResponse(games.find(_.id == id))
        Behaviors.same

      case GetGameByTitle(title, replyTo) =>
        replyTo ! GetGameResponse(games.find(_.title.equalsIgnoreCase(title)))
        Behaviors.same

      case GetGameByPrice(price, replyTo) =>
        replyTo ! Games(games.filter(_.price == price).toSeq)
        Behaviors.same

      case GetGameByRating(rating, replyTo) =>
        replyTo ! Games(games.filter(_.rating == rating).toSeq)
        Behaviors.same

      case CreateGame(game, replyTo) =>
        val updatedGames = games + game
        replyTo ! ActionPerformed(s"Game '${game.title}' added.")
        registry(updatedGames)

      case DeleteGame(id, replyTo) =>
        val updatedGames = games.filterNot(_.id == id)
        replyTo ! ActionPerformed(s"Game ID $id deleted.")
        registry(updatedGames)

      case DeleteGameByTitle(title, replyTo) =>
        val updatedGames = games.filterNot(_.title.equalsIgnoreCase(title))
        replyTo ! ActionPerformed(s"Game with title '$title' deleted.")
        registry(updatedGames)

      case UpdateGamePlatform(id, platform, replyTo) =>
        val updatedGames = games.map {
          case game if game.id == id => game.copy(platform = platform)
          case other                 => other
        }
        replyTo ! ActionPerformed(s"Game ID $id updated with new platform: $platform.")
        registry(updatedGames)
    }
}