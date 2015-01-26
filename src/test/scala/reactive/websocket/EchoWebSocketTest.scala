package reactive.websocket

import reactive.Configuration
import reactive.api.{ MainActors, ReactiveApi, RootService }
import reactive.find.{ FindActor, FindService }
import reactive.hide.{ HideActor, HideService }
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.frame.TextFrame
import spray.http.StatusCodes

@RunWith(classOf[JUnitRunner])
class EchoWebSocketTest extends FunSuite with MainActors with ReactiveApi {
  implicit lazy val system = ActorSystem("EchoWebSocketTest")
  sys.addShutdownHook({ system.shutdown })
  test("websocket echo") {
    val wss = system.actorOf(Props(new RootService[WebSocketServer](complete(StatusCodes.NotFound))), "ewss")
    IO(UHttp) ! Http.Bind(wss, Configuration.host, Configuration.portWs)
    Thread.sleep(2000L) // wait for all servers to be cleanly started
    find ! FindActor.Clear
    hide ! HideActor.Clear
    Thread.sleep(1000L)
    var wsmsg = ""
    val wse = system.actorOf(Props(new TestingWebSocketClient {
      override def businessLogic = {
        case WebSocket.Release => close
        case TextFrame(msg) => wsmsg = msg.utf8String
        case WebSocket.Send(message) => send(message)
        case whatever => // ignore
      }
    }))
    wse ! WebSocket.Connect("echo.websocket.org", 443, "/echo", withSsl = true)
    Thread.sleep(2000L) // wait for all servers to be cleanly started
    val rock = "Rock it with HTML5 WebSocket"
    wse ! WebSocket.Send(rock)
    Thread.sleep(2000L)
    assert(rock == wsmsg)
    wse ! WebSocket.Release
    system.shutdown
    Thread.sleep(1000L)
  }
}
