package reactive.api

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import scala.reflect.ClassTag
import spray.can.Http
import spray.routing.{ HttpServiceActor, Route }

class RootService[RA <: RouteActor](val route : Route)(implicit tag : ClassTag[RA]) extends HttpServiceActor with ActorLogging {
  override def receive = {
    case connected : Http.Connected =>
      // implement the "per-request actor" pattern
      sender ! Http.Register(context.actorOf(Props(tag.runtimeClass, sender, route)))
    case whatever => log.debug("RootService got some {}", whatever)
  }
}

trait RouteActor extends HttpServiceActor {
  def connection : ActorRef
  def route : Route
}

private[api] class BasicRouteActor(val connection : ActorRef, val route : Route) extends RouteActor {
  override def receive = runRoute(route)
}
