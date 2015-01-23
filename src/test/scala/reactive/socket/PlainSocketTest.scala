package reactive.socket

import reactive.Configuration
import reactive.api.{ MainActors, ReactiveApi, RootService }
import reactive.find.{ FindActor, FindService }
import reactive.hide.HideActor
import reactive.websocket.{ TestingWebSocketClient, WebSocket, WebSocketServer }
import akka.actor.{ Actor, ActorSystem, Props }
import akka.io.{ IO, Tcp }
import java.net.{ InetSocketAddress, Socket, URI }
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.WebSocketClientWorker
import spray.can.websocket.frame.{ CloseFrame, StatusCode, TextFrame }
import spray.http.{ HttpHeaders, HttpMethods, HttpRequest }

@RunWith(classOf[JUnitRunner])
class PlainSocketTest extends FunSuite with MainActors with ReactiveApi {
  implicit lazy val system = ActorSystem("reactive-socket-PlainSocketTest")
  sys.addShutdownHook({ system.shutdown })
  test("pure socket connection") {
    val wss = system.actorOf(Props(new RootService[WebSocketServer](new FindService(find).wsroute)), "pswss")
    IO(UHttp) ! Http.Bind(wss, Configuration.host, Configuration.portWs)
    IO(Tcp) ! Tcp.Bind(socketService, new InetSocketAddress(Configuration.host, Configuration.portTcp))
    Thread.sleep(2000L) // wait for all servers to be cleanly started
    find ! FindActor.Clear
    hide ! HideActor.Clear
    Thread.sleep(1000L)
    var wsmsg = ""
    val wsf = system.actorOf(Props(new TestingWebSocketClient {
      override def businessLogic = {
        case WebSocket.Release => close
        case TextFrame(msg) => wsmsg = msg.utf8String
        case whatever => // ignore
      }
    }))
    wsf ! WebSocket.Connect(Configuration.host, Configuration.portWs, "/find/ws")
    Thread.sleep(2000L)
    val conn = new Socket
    conn.connect(new InetSocketAddress("localhost", Configuration.portTcp))
    conn.getOutputStream.write("2.1523721 41.4140567\n".getBytes)
    conn.getOutputStream.flush
    Thread.sleep(2000L)
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
    Thread.sleep(2000L)
    val clear = """\{"clear":\{"id":"[-0-9a-f]+","idx":"C"\}\}""".r
    assert(None != clear.findFirstIn(wsmsg))
    wsf ! WebSocket.Release
    system.shutdown
    Thread.sleep(1000L)
  }
}
