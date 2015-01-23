package reactive.hide

import reactive.api.{ MainActors, ReactiveApi }
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import spray.http.{ HttpEntity, MediaTypes, StatusCodes }
import spray.routing.Directives
import spray.testkit.Specs2RouteTest

@RunWith(classOf[JUnitRunner])
class HideServiceSpec extends Specification with Directives with Specs2RouteTest with MainActors with ReactiveApi {
  def actorRefFactory = system

  "Reactive API" should {
    "hide bunny" in {
      Post("/hide").withEntity(HttpEntity(MediaTypes.`application/json`, """
          {"longitude" : "-38.4798", "latitude" : "-3.8093"}
          """)) ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must === ("hidden")
      }
    }
  }
}
