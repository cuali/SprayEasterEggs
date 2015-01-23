package reactive.hide

import reactive.Configuration
import reactive.api.ApplicationJsonFormats
import reactive.websocket.WebSocket
import akka.actor.{ ActorRef, ActorSystem }
import spray.http.StatusCodes
import spray.routing.Directives

class HideService(val hide : ActorRef)(implicit system : ActorSystem) extends Directives with ApplicationJsonFormats {
  private implicit val moveFormat = jsonFormat2(MarkerActor.Move)
  lazy val route =
    pathPrefix("hide") {
      val dir = "hide/"
      pathEndOrSingleSlash {
        get {
          getFromResource(dir + "index.html")
        } ~
        post {
          handleWith {
            move : MarkerActor.Move =>
              hide ! move
              "hidden"
          }
        }
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
    pathPrefix("hide") {
      path("ws") {
        implicit ctx =>
          ctx.responder ! WebSocket.Register(ctx.request, hide, true)
      }
    }
}
