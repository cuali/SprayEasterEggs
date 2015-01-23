package reactive.find

import reactive.Configuration
import reactive.websocket.WebSocket
import akka.actor.{ ActorRef, ActorSystem }
import spray.http.StatusCodes
import spray.routing.Directives

class FindService(find : ActorRef)(implicit system : ActorSystem) extends Directives {
  lazy val route =
    pathPrefix("find") {
      val dir = "find/"
      pathEndOrSingleSlash {
        getFromResource(dir + "index.html")
      } ~
      path("ws") {
        requestUri { uri =>
          val wsUri = uri.withPort(Configuration.portWs)
          system.log.debug("redirect {} to {}", uri, wsUri)
          redirect(wsUri, StatusCodes.PermanentRedirect)
        }
      } ~
      getFromResourceDirectory(dir)
    }
  lazy val wsroute = 
    pathPrefix("find") {
      path("ws") {
        implicit ctx =>
          ctx.responder ! WebSocket.Register(ctx.request, find, true)
      }
    }
}
