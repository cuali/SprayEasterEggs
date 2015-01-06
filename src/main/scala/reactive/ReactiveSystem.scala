package reactive

import reactive.api.{ MainActors, ReactiveApi }
import reactive.socket.ReactiveServer
import akka.actor.ActorSystem
import akka.io.{ IO, Tcp }
import java.net.InetSocketAddress
import spray.can.Http

object ReactiveSystem extends App with MainActors with ReactiveApi {
  implicit lazy val system = ActorSystem("reactive-system")
  private val rs = new ReactiveServer(Configuration.portWs)
  rs.forResource("/find/ws", Some(find))
  rs.forResource("/hide/ws", Some(hide))
  rs.start
  sys.addShutdownHook({ system.shutdown; rs.stop })
  IO(Http) ! Http.Bind(rootService, Configuration.host, port = Configuration.portHttp)
  IO(Tcp) ! Tcp.Bind(socketService, new InetSocketAddress(Configuration.host, Configuration.portTcp))
}

object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("easter-eggs.host")
  val portHttp = config.getInt("easter-eggs.ports.http")
  val portTcp = config.getInt("easter-eggs.ports.tcp")
  val portWs = config.getInt("easter-eggs.ports.ws")
}
