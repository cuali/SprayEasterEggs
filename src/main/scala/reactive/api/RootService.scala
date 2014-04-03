package reactive.api

import akka.actor.{ Actor, ActorLogging }
import spray.routing.{ HttpService, Route }

class RootService(route : Route) extends Actor with HttpService with ActorLogging {
  implicit def actorRefFactory = context
  def receive = runRoute(route)
}
