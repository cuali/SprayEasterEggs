organization  := "cua.li"

version       := "0.3"

scalaVersion  := "2.10.4"

libraryDependencies ++= {
  val akkaV  = "2.3.8"
  val sprayV = "1.3.2"
  Seq(
//  "org.java-websocket"  %   "Java-WebSocket" % "1.3.1" withSources(),
    "io.spray"            %%  "spray-json"     % "1.3.1" withSources(),
    "io.spray"            %%  "spray-can"      % sprayV  withSources(),
    "io.spray"            %%  "spray-routing"  % sprayV  withSources(),
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV   withSources(),
    //
    "com.typesafe.akka"   %%  "akka-testkit"   % akkaV    % "test" withSources(),
    "io.spray"            %%  "spray-testkit"  % sprayV   % "test" withSources(),
    "org.scalatest"       %%  "scalatest"      % "2.2.3"  % "test" withSources(),
    "junit"               %   "junit"          % "4.12"   % "test",
    "org.specs2"          %%  "specs2"         % "2.4.15" % "test"
  )
}

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.7", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.7", "-target", "1.7", "-g:vars")

doc in Compile <<= target.map(_ / "none")

publishArtifact in (Compile, packageSrc) := false

//sublimeTransitive := true

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

seq(Revolver.settings: _*)

