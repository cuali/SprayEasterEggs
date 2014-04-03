package reactive.find

import reactive.Configuration
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
          redirect(wsUri, StatusCodes.PermanentRedirect)
        }
      } ~
      getFromResourceDirectory(dir)
    }
}
