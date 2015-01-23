package reactive.hide

import reactive.websocket.WebSocket
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import scala.collection.mutable

object HideActor {
  sealed trait HideMessage
  case object Clear extends HideMessage
  case class Unregister(ws : WebSocket) extends HideMessage
}
class HideActor extends Actor with ActorLogging {
  val bunny = context.actorOf(Props[MarkerActor])
  bunny ! MarkerActor.Start(null, "B")
  val markers = mutable.Map[WebSocket, ActorRef]()
  override def receive = {
    case WebSocket.Open(ws) =>
      val idx = (markers.size % 10).toString
      val marker = context.actorOf(Props(classOf[MarkerActor]))
      markers += ((ws, marker))
      log.debug("registered marker {}", idx)
      marker ! MarkerActor.Start(ws, idx)
    case WebSocket.Close(ws, code, reason) =>
      self ! HideActor.Unregister(ws)
    case WebSocket.Error(ws, ex) =>
      self ! HideActor.Unregister(ws)
    case WebSocket.Message(ws, msg) =>
      val coords = msg.split(" ")
      val lng = coords(0)
      val lat = coords(1)
      log.debug("move marker to ({},{})", lng, lat)
      markers(ws) ! MarkerActor.Move(lng, lat)
    case HideActor.Clear =>
      for (marker <- markers) {
        marker._2 ! MarkerActor.Stop
      }
      markers.clear
    case HideActor.Unregister(ws) =>
      if (null != ws) {
        log.debug("unregister marker")
        val marker = markers(ws)
        markers remove ws
        marker ! MarkerActor.Stop
      }
    case move @ MarkerActor.Move(lng, lat) =>
      log.debug("move bunny to ({},{})", lng, lat)
      bunny ! move
    case MarkerActor.Stop =>
      log.debug("marker {} stopped", sender)
    case whatever =>
      log.warning("Hiding '{}'", whatever)
  }
}
