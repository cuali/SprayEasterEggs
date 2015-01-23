package reactive

import reactive.api.{ MainActors, ReactiveApi }
import akka.actor.{ ActorSystem, PoisonPill }
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
import spray.can.Http
import spray.can.server.UHttp

object ReactiveSystem extends App with MainActors with ReactiveApi {
  implicit lazy val system = ActorSystem("reactive-system")
  sys.addShutdownHook({ system.shutdown })
  IO(UHttp) ! Http.Bind(wsService, Configuration.host, Configuration.portWs)
  // Since the UTttp extension extends from Http extension, it starts an actor whose name will later collide with the Http extension.
  system.actorSelection("/user/IO-HTTP") ! PoisonPill
  IO(Tcp) ! Tcp.Bind(socketService, new InetSocketAddress(Configuration.host, Configuration.portTcp))
  // We could use IO(UHttp) here instead of killing the "/user/IO-HTTP" actor
  IO(Http) ! Http.Bind(rootService, Configuration.host, Configuration.portHttp)
}

object Configuration {
  import com.typesafe.config.ConfigFactory
 
  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  lazy val host = config.getString("easter-eggs.host")
  lazy val portHttp = config.getInt("easter-eggs.ports.http")
  lazy val portTcp = config.getInt("easter-eggs.ports.tcp")
  lazy val portWs = config.getInt("easter-eggs.ports.ws")
}
