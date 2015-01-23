package reactive.api

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http.{ MediaTypes, StatusCodes }

class ReactiveApiSpec extends Specification with Specs2RouteTest with MainActors with ReactiveApi {
  def actorRefFactory = system
  "Reactive API" should {
    "return the correct page for GET requests to the pages' path" in {
      Get() ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must contain ("Easter Eggs")
      }
      Get("/") ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must contain ("Easter Eggs")
      }
      Get("/find") ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must contain ("-- Find")
        responseAs[String] must contain ("find.js")
        responseAs[String] must contain (">Find<")
      }
      Get("/hide") ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must contain ("-- Hide")
        responseAs[String] must contain ("hide.js")
        responseAs[String] must contain (">Hide<")
      }
    }
    "return the javascripts for GET requests to the js files" in {
      Get("/find/find.js") ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must contain ("function")
      }
      Get("/hide/hide.js") ~> routes ~> check {
        status === StatusCodes.OK
        responseAs[String] must contain ("function")
      }
    }
    "return the image for GET requests to the markers files" in {
      Get("/markers/marker0.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker1.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker2.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker3.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker4.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker5.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker6.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker7.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker8.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/marker9.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/markerB.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
      Get("/markers/markerC.png") ~> routes ~> check {
        status === StatusCodes.OK
        mediaType must be (MediaTypes.`image/png`)
      }
    }
    "redirect the GET requests to websocket paths" in {
      Get("/find/ws") ~> routes ~> check {
        status === StatusCodes.PermanentRedirect
      }
      Get("/hide/ws") ~> routes ~> check {
        status === StatusCodes.PermanentRedirect
      }
    }
    "leave GET requests to unknown paths unhandled" in {
      Get("/play") ~> routes ~> check {
        status === StatusCodes.NotFound
      }
    }
  }
}
