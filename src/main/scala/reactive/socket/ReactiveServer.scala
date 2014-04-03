package reactive.socket

import akka.actor.ActorRef
import java.net.InetSocketAddress
import org.java_websocket.WebSocket
import org.java_websocket.framing.CloseFrame
import org.java_websocket.server.WebSocketServer
import org.java_websocket.handshake.ClientHandshake
import scala.collection.mutable.Map

object ReactiveServer {
  sealed trait ReactiveServerMessage
  case class Message(ws : WebSocket, msg : String)
  	extends ReactiveServerMessage
  case class Open(ws : WebSocket, hs : ClientHandshake)
  	extends ReactiveServerMessage
  case class Close(ws : WebSocket, code : Int, reason : String, external : Boolean)
  	extends ReactiveServerMessage
  case class Error(ws : WebSocket, ex : Exception)
  	extends ReactiveServerMessage
}
class ReactiveServer(val port : Int)
    extends WebSocketServer(new InetSocketAddress(port)) {
  private val reactors = Map[String, ActorRef]()
  final def forResource(descriptor : String, reactor : Option[ActorRef]) {
    reactor match {
      case Some(actor) => reactors += ((descriptor, actor))
      case None => reactors -= descriptor
    }
  }
  final override def onMessage(ws : WebSocket, msg : String) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! ReactiveServer.Message(ws, msg)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }
  final override def onOpen(ws : WebSocket, hs : ClientHandshake) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! ReactiveServer.Open(ws, hs)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }
  final override def onClose(ws : WebSocket, code : Int, reason : String, external : Boolean) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! ReactiveServer.Close(ws, code, reason, external)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }
  final override def onError(ws : WebSocket, ex : Exception) {
    if (null != ws) {
      reactors.get(ws.getResourceDescriptor) match {
        case Some(actor) => actor ! ReactiveServer.Error(ws, ex)
        case None => ws.close(CloseFrame.REFUSE)
      }
    }
  }
}
