package reactive.hide

import reactive.Configuration
import reactive.api.ApplicationJsonFormats
import akka.actor.{ ActorRef, ActorSystem }
import spray.http.StatusCodes
import spray.routing.Directives

class HideService(hide : ActorRef)(implicit system : ActorSystem) extends Directives with ApplicationJsonFormats {
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
        requestUri {
          uri =>
            val wsUri = uri.withPort(Configuration.portWs)
            redirect(wsUri, StatusCodes.PermanentRedirect)
        }
      } ~
      getFromResourceDirectory(dir)
    }
}
