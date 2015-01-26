package reactive.websocket

import akka.actor.{ ActorRef, ActorSystem }
import spray.http.HttpRequest

object WebSocket {
  sealed trait WebSocketMessage
  case class Open(ws : WebSocket) extends WebSocketMessage
  case class Message(ws : WebSocket, msg : String) extends WebSocketMessage
  case class Close(ws : WebSocket, code : Int, reason : String) extends WebSocketMessage
  case class Error(ws : WebSocket, reason : String) extends WebSocketMessage
  case class Connect(host : String, port : Int, resource : String, withSsl : Boolean = false) extends WebSocketMessage
  case class Send(msg : String) extends WebSocketMessage
  case object Release extends WebSocketMessage
  case class Register(request : HttpRequest, handler : ActorRef, autoping : Boolean = false)
  private[websocket] object Ping extends WebSocketMessage
}
trait WebSocket {
  def send(message : String) : Unit
  def close() : Unit
  def path() : String
}
