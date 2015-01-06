package reactive.api

import reactive.find.FindActor
import reactive.hide.HideActor

import akka.actor.Props

trait MainActors {
  this : AbstractSystem =>

  lazy val find = system.actorOf(Props[FindActor], "find")
  lazy val hide = system.actorOf(Props[HideActor], "hide")
}
