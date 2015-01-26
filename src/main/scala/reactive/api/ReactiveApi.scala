package reactive.api

import reactive.find.FindService
import reactive.hide.HideService
import reactive.socket.{ SocketService }
import reactive.websocket.WebSocketServer
import akka.actor.{ ActorSystem, Props }
import akka.event.Logging.InfoLevel
import scala.reflect.ClassTag
import spray.http.{ HttpRequest, StatusCodes }
import spray.routing.{ Directives, RouteConcatenation }
import spray.routing.directives.LogEntry

trait AbstractSystem {
  implicit def system : ActorSystem
}

trait ReactiveApi extends RouteConcatenation with StaticRoute with AbstractSystem {
  this : MainActors =>
  private def showReq(req : HttpRequest) = LogEntry(req.uri, InfoLevel)

  val rootService = system.actorOf(Props(new RootService[BasicRouteActor](routes)), "routes")
  lazy val routes = logRequest(showReq _) {
    new FindService(find).route ~
    new HideService(hide).route ~
    staticRoute
  }
  val wsService = system.actorOf(Props(new RootService[WebSocketServer](wsroutes)), "wss")
  lazy val wsroutes = logRequest(showReq _) {
    new FindService(find).wsroute ~
    new HideService(hide).wsroute ~
    complete(StatusCodes.NotFound)
  }
  val socketService = system.actorOf(Props[SocketService], "tcp")
}

trait StaticRoute extends Directives {
  this : AbstractSystem =>

  lazy val staticRoute =
    path("favicon.ico") {
      getFromResource("favicon.ico")
    } ~
    pathPrefix("markers") {
      getFromResourceDirectory("markers/")
    } ~
    pathPrefix("css") {
      getFromResourceDirectory("css/")
    } ~
    pathEndOrSingleSlash {
      getFromResource("index.html")
    } ~ complete(StatusCodes.NotFound)
}
