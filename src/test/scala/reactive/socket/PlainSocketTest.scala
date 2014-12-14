package reactive.socket

import java.net.{InetSocketAddress, Socket, URI}

import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import reactive.Configuration
import reactive.api.{MainActors, ReactiveApi}
import reactive.find.FindActor
import reactive.hide.HideActor

@RunWith(classOf[JUnitRunner])
class PlainSocketTest extends FunSuite with MainActors with ReactiveApi {
  implicit lazy val system = ActorSystem("reactive-system")

  sys.addShutdownHook({
    system.shutdown
  })

  test("pure socket connection") {
    val rs = new ReactiveServer(Configuration.portWs)
    rs.forResource("/find/ws", Some(find))
    rs.start
    IO(Tcp) ! Tcp.Bind(socketService, new InetSocketAddress(Configuration.host, Configuration.portTcp))
    find ! FindActor.Clear
    hide ! HideActor.Clear
    Thread.sleep(1000L) // wait for all servers to be cleanly started

    var wsmsg = ""
    val wsf = new WebSocketClient(URI.create(s"ws://localhost:${Configuration.portWs}/find/ws")) {
      override def onMessage(msg: String) {
        wsmsg = msg
      }

      override def onOpen(hs: ServerHandshake) {}

      override def onClose(code: Int, reason: String, intentional: Boolean) {}

      override def onError(ex: Exception) {}
    }
    wsf.connect

    val conn = new Socket
    conn.connect(new InetSocketAddress("localhost", Configuration.portTcp))
    conn.getOutputStream.write("2.1523721 41.4140567\n".getBytes)
    conn.getOutputStream.flush
    Thread.sleep(1000L)

    val first = """\{"move":\{"id":"[-0-9a-f]+","idx":"C","longitude":2\.1523721,"latitude":41\.4140567\}\}""".r
    assert(None != first.findFirstIn(wsmsg))
    Thread.sleep(1000L)

    conn.getOutputStream.write("-38.4798 -3.8093\n".getBytes)
    conn.getOutputStream.flush
    Thread.sleep(1000L)

    val second = """\{"move":\{"id":"[-0-9a-f]+","idx":"C","longitude":-38\.4798,"latitude":-3\.8093\}\}""".r
    assert(None != second.findFirstIn(wsmsg))
    Thread.sleep(1000L)

    conn.close
    Thread.sleep(1000L)

    val clear = """\{"clear":\{"id":"[-0-9a-f]+","idx":"C"\}\}""".r
    assert(None != clear.findFirstIn(wsmsg))
    wsf.close
    Thread.sleep(1000L)

    rs.stop
  }
}
