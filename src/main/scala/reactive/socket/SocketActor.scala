package reactive.socket

import reactive.hide.MarkerActor
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.io.Tcp
import java.util.UUID
import scala.collection._

class SocketActor(val connection : ActorRef) extends Actor with ActorLogging {
  val marker = context.actorOf(Props[MarkerActor])
  marker ! MarkerActor.Start(null, "C")
  val coords = "(-?\\d+\\.\\d+) (-?\\d+\\.\\d+)".r
  override def receive = {
    case Tcp.Received(data) =>
      data.utf8String.trim match {
        case coords(lng, lat) =>
          marker ! MarkerActor.Move(lng, lat)
        case msg => log.info(msg)
      }
      //connection ! Write(ByteString("OK\n"))
    case Tcp.PeerClosed => stop()
    case Tcp.ErrorClosed => stop()
    case Tcp.Closed => stop()
    case Tcp.ConfirmedClosed => stop()
    case Tcp.Aborted => stop()
  }
  private def stop() {
    marker ! MarkerActor.Stop
    context stop self
  }
}
