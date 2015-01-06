package reactive.api

import java.util.UUID
import spray.json._
import spray.http.StatusCode
import spray.httpx.SprayJsonSupport
import scala.reflect.ClassTag

trait ApplicationJsonFormats extends DefaultJsonProtocol with SprayJsonSupport {
  def jsonObjectFormat[A : ClassTag] : RootJsonFormat[A] = new RootJsonFormat[A] {
    val tag = implicitly[ClassTag[A]]
    def write(obj : A) : JsValue = JsObject("object" -> JsString(tag.runtimeClass.getSimpleName))
    def read(value : JsValue) : A = tag.runtimeClass.newInstance().asInstanceOf[A]
  }

  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(id : UUID) = JsString(id.toString)
    def read(value : JsValue) = value match {
      case JsString(id) => UUID.fromString(id)
      case whatever     => deserializationError("Expected JsString, but got " + whatever)
    }
  }
}
