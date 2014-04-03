package reactive.hide

import reactive.socket.ReactiveServer
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import org.java_websocket.WebSocket
import scala.collection._

object HideActor {
  sealed trait HideMessage
  case object Clear extends HideMessage
  case class Unregister(ws : WebSocket) extends HideMessage
}
class HideActor extends Actor with ActorLogging {
  import HideActor._
  import MarkerActor._
  import ReactiveServer._

  val bunny = context.actorOf(Props[MarkerActor])
  bunny ! Start(null, "B")
  val markers = mutable.Map[WebSocket, ActorRef]()
  override def receive = {
    case Open(ws, hs) => {
      val idx = (markers.size % 10).toString
      val marker = context.actorOf(Props(classOf[MarkerActor]))
      markers += ((ws, marker))
      log.debug("registered marker {}",idx)
      marker ! Start(ws, idx)
    }
    case Close(ws, code, reason, ext) => self ! Unregister(ws)
    case Error(ws, ex) => self ! Unregister(ws)
    case Message(ws, msg) => {
      val coords = msg.split(" ")
      val lng = coords(0)
      val lat = coords(1)
      log.debug("move marker to ({},{})",lng,lat)
      markers(ws) ! Move(lng, lat)
    }
    case Clear => {
      for (marker <- markers) {
        marker._2 ! Stop
      }
      markers.clear
    }
    case Unregister(ws) => {
      if (null != ws) {
        log.debug("unregister marker")
        val marker = markers(ws)
        markers remove ws
        marker ! Stop
      }
    }
    case move @ Move(lng, lat) => {
      log.debug("move bunny to ({},{})",lng,lat)
      bunny ! move
    }
  }
}
