package reactive.find

import reactive.socket.ReactiveServer
import akka.actor.{Actor, ActorLogging}
import scala.collection._
import org.java_websocket.WebSocket

object FindActor {
  sealed trait FindMessage
  case object Clear extends FindMessage
  case class Unregister(ws : WebSocket) extends FindMessage
  case class Marker(id : String, idx : String) extends FindMessage
  case class Clear(marker : Marker) extends FindMessage
  case class Move(marker : Marker, longitude : String, latitude : String) extends FindMessage
}
class FindActor extends Actor with ActorLogging {
  import FindActor._
  import ReactiveServer._

  val clients = mutable.ListBuffer[WebSocket]()
  val markers = mutable.Map[Marker,Option[Move]]()
  override def receive = {
    case Open(ws, hs) => {
      clients += ws
      for (marker <- markers if None != marker._2) {
        ws.send(message(marker._2.get))
      }
      log.debug("registered monitor for url {}", ws.getResourceDescriptor)
    }
    case Close(ws, code, reason, ext) => self ! Unregister(ws)
    case Error(ws, ex) => self ! Unregister(ws)
    case Message(ws, msg) =>
      log.debug("url {} received msg '{}'", ws.getResourceDescriptor, msg)
    case Clear => {
      for (marker <- markers if None != marker._2) {
        val msg = message(marker._1)
        for (client <- clients) {
          client.send(msg)
        }
      }
      markers.clear
    }
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("unregister monitor")
        clients -= ws
      }
    }
    case Clear(marker) => {
      log.debug("clear marker {} '{}'", marker.idx, marker.id)
      val msg = message(marker)
      markers -= marker
      for (client <- clients) {
        client.send(msg)
      }
      log.debug("sent to {} clients to clear marker '{}'", clients.size, msg)
    }
    case marker @ Marker(id, idx) => {
      markers += ((marker, None))
      log.debug("create new marker {} '{}'", idx, id)
    }
    case move @ Move(marker, lng, lat) => {
      markers += ((marker, Some(move)))
      val msg = message(move)
      for (client <- clients) {
        client.send(msg)
      }
      log.debug("sent to {} clients the new move '{}'", clients.size, msg)
    }
  }
  private def message(move :Move) = s"""{"move":{"id":"${move.marker.id}","idx":"${move.marker.idx}","longitude":${move.longitude},"latitude":${move.latitude}}}"""
  private def message(marker :Marker) = s"""{"clear":{"id":"${marker.id}","idx":"${marker.idx}"}}"""
}
