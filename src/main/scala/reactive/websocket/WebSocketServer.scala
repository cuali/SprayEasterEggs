package reactive.websocket

import reactive.api.RouteActor
import akka.actor.{ ActorRef, ActorSystem, Cancellable }
import scala.concurrent.duration.DurationInt
import spray.can.Http
import spray.can.server.UHttp.Upgraded
import spray.can.websocket.{ UpgradedToWebSocket, WebSocketServerWorker }
import spray.can.websocket.frame.{ CloseFrame, PingFrame, PongFrame, StatusCode, TextFrame }
import spray.routing.{ RequestContext, Route }
import spray.http.HttpRequest
import spray.routing.{ Rejected, RequestContext }

class WebSocketServer(val serverConnection: ActorRef, val route: Route) extends RouteActor with WebSocketServerWorker with WebSocket {
  import context.dispatcher
  override lazy val connection = serverConnection
  override def receive = matchRoute(route) orElse handshaking orElse closeLogic
  private def matchRoute(route : Route) : Receive = {
    case request : HttpRequest =>
      val ctx = RequestContext(request, self, request.uri.path)
      log.debug("HTTP request for uri {}", request.uri.path)
      route(ctx.withResponder(self))
      handshaking(request)
    case WebSocket.Register(request, actor, ping) =>
      if (ping) pinger = Some(context.system.scheduler.scheduleOnce(110.seconds, self, WebSocket.Ping))
      handler = actor
      uripath = request.uri.path.toString
      handler ! WebSocket.Open(this)
    case Rejected(rejections) =>
      log.info("Rejecting with {}", rejections)
      context stop self
  }
  // this is the actor's behavior after the WebSocket handshaking resulted in an upgraded request 
  override def businessLogic = {
    case TextFrame(message) =>
      ping
      handler ! WebSocket.Message(this, message.utf8String)
    case UpgradedToWebSocket =>
      // nothing to do
    case WebSocket.Ping =>
      send(PingFrame())
    case PongFrame(payload) =>
      ping
    case Http.Aborted =>
      handler ! WebSocket.Error(this, "aborted")
    case Http.ErrorClosed(cause) =>
      handler ! WebSocket.Error(this, cause)
    case CloseFrame(status, reason) =>
      handler ! WebSocket.Close(this, status.code, reason)
    case Http.Closed =>
      handler ! WebSocket.Close(this, StatusCode.NormalClose.code, "")
    case Http.ConfirmedClosed =>
      handler ! WebSocket.Close(this, StatusCode.GoingAway.code, "")
    case Http.PeerClosed =>
      handler ! WebSocket.Close(this, StatusCode.GoingAway.code, "")
    case WebSocket.Release =>
      handler ! WebSocket.Close(this, StatusCode.NormalClose.code, "")
    case whatever =>
      log.debug("WebSocket received '{}'", whatever)
  }
  def send(message : String) = send(TextFrame(message))
  def close() = send(CloseFrame(StatusCode.NormalClose))
  def path() = uripath
  private def ping() : Unit = pinger match {
    case None => // nothing to do
    case Some(timer) =>
      if (!timer.isCancelled) timer.cancel
      pinger = Some(context.system.scheduler.scheduleOnce(110.seconds, self, WebSocket.Ping))
  }
  private var uripath = "/"
  private var pinger : Option[Cancellable] = None
  private var handler = self
}
