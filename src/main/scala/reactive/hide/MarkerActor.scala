package reactive.hide

import reactive.find.FindActor
import reactive.websocket.WebSocket
import akka.actor.{ Actor, ActorLogging, PoisonPill }
import java.util.UUID

object MarkerActor {
  sealed trait MarkerMessage
  case object Stop extends MarkerMessage
  case class Start(ws : WebSocket, marker : String) extends MarkerMessage
  case class Move(longitude : String, latitude : String) extends MarkerMessage
}
class MarkerActor extends Actor with ActorLogging {
  import MarkerActor._

  var marker : FindActor.Marker = _
  var client : WebSocket = _
  override def receive = {
    case Stop => {
      context.actorSelection("/user/find") ! FindActor.Clear(marker)
      sender ! Stop
      context stop self
    }
    case Start(ws, idx) => {
      client = ws
      marker = FindActor.Marker(UUID.randomUUID.toString, idx)
      log.debug("registered marker {} {} websocket under id {}",idx,(if(null==client)"without"else"with"),marker.id)
      context.actorSelection("/user/find") ! marker
      if (null!=client) client.send("OK")
    }
    case Move(lng, lat) => {
      log.debug("move marker {} {} websocket to ({},{})",marker.id,(if(null==client)"without"else"with"),lng,lat)
      context.actorSelection("/user/find") ! FindActor.Move(marker, lng, lat)
      if (null!=client) client.send("OK")
    }
  }
}
