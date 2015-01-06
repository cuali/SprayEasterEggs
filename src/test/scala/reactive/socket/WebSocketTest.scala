package reactive.socket

import reactive.Configuration
import reactive.api.{ MainActors, ReactiveApi }
import reactive.find.FindActor
import reactive.hide.HideActor
import akka.actor.ActorSystem
import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WebSocketTest extends FunSuite with MainActors with ReactiveApi {
  implicit lazy val system = ActorSystem("reactive-system")
  sys.addShutdownHook({ system.shutdown })
  test("websocket connection") {
    val rs = new ReactiveServer(Configuration.portWs)
    rs.forResource("/find/ws", Some(find))
    rs.forResource("/hide/ws", Some(hide))
    rs.start
    find ! FindActor.Clear
    hide ! HideActor.Clear
    Thread.sleep(2000L) // wait for all servers to be cleanly started
    var wsmsg = ""
    val wsf = new WebSocketClient(URI.create(s"ws://localhost:${Configuration.portWs}/find/ws")) {
      override def onMessage(msg : String) {
        wsmsg = msg
      }
      override def onOpen(hs : ServerHandshake) {}
      override def onClose(code : Int, reason : String, intentional : Boolean) {}
      override def onError(ex : Exception) { println(ex.getMessage) }
    }
    wsf.connect
    val wsh = new WebSocketClient(URI.create(s"ws://localhost:${Configuration.portWs}/hide/ws")) {
      override def onMessage(msg : String) {}
      override def onOpen(hs : ServerHandshake) {}
      override def onClose(code : Int, reason : String, intentional : Boolean) {}
      override def onError(ex : Exception) {}
    }
    wsh.connect
    Thread.sleep(1000L)
    wsh.send("2.1523721 41.4140567")
    Thread.sleep(1000L)
    val first = """\{"move":\{"id":"[-0-9a-f]+","idx":"0","longitude":2\.1523721,"latitude":41\.4140567\}\}""".r
    assert(None != first.findFirstIn(wsmsg))
    Thread.sleep(1000L)
    wsh.send("-38.4798 -3.8093")
    Thread.sleep(1000L)
    val second = """\{"move":\{"id":"[-0-9a-f]+","idx":"0","longitude":-38\.4798,"latitude":-3\.8093\}\}""".r
    assert(None != second.findFirstIn(wsmsg))
    Thread.sleep(1000L)
    wsh.close
    Thread.sleep(1000L)
    val clear = """\{"clear":\{"id":"[-0-9a-f]+","idx":"0"\}\}""".r
    assert(None != clear.findFirstIn(wsmsg))
    wsf.close
    Thread.sleep(1000L)
    rs.stop
  }
}
