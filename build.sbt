organization  := "cua.li"

version       := "0.5"

scalaVersion  := "2.11.6"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= {
  val akkaV  = "2.3.10"
  val sprayV = "1.3.3"
  val kamonV = "0.3.5"
  Seq(
    "com.wandoulabs.akka" %%  "spray-websocket"       % "0.1.4"           withSources() withJavadoc,
    "io.spray"            %%  "spray-json"            % "1.3.1"           withSources() withJavadoc,
    "io.spray"            %%  "spray-can"             % sprayV            withSources() withJavadoc,
    "io.spray"            %%  "spray-routing"         % sprayV            withSources() withJavadoc,
    "com.typesafe.akka"   %%  "akka-actor"            % akkaV             withSources() withJavadoc,
    "com.typesafe.akka"   %%  "akka-slf4j"            % akkaV             withSources() withJavadoc,
    /*
    "org.aspectj"         %   "aspectjweaver"         % "1.8.5"           withSources() withJavadoc,
    "io.kamon"            %%  "kamon-core"            % kamonV            withSources() withJavadoc,
    "io.kamon"            %%  "kamon-spray"           % kamonV            withSources() withJavadoc,
    "io.kamon"            %%  "kamon-statsd"          % kamonV            withSources() withJavadoc,
    "io.kamon"            %%  "kamon-log-reporter"    % kamonV            withSources() withJavadoc,
    "io.kamon"            %%  "kamon-system-metrics"  % kamonV            withSources() withJavadoc,
    "io.kamon"            %%  "kamon-testkit"         % kamonV   % "test" withSources() withJavadoc,
    // */
    "com.typesafe.akka"   %%  "akka-testkit"          % akkaV    % "test" withSources() withJavadoc,
    "io.spray"            %%  "spray-testkit"         % sprayV   % "test" withSources() withJavadoc,
    "org.scalatest"       %%  "scalatest"             % "2.2.4"  % "test",
    "junit"               %   "junit"                 % "4.12"   % "test",
    "org.specs2"          %%  "specs2"                % "2.4.17" % "test", // until spray-testkit gets compiled against specs 3.3
    "ch.qos.logback"      %   "logback-classic"       % "1.1.3"
  )
}

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

scalacOptions in Test ++= Seq("-Yrangepos")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

doc in Compile <<= target.map(_ / "none")

publishArtifact in (Compile, packageSrc) := false

logBuffered in Test := false

Keys.fork in Test := false

parallelExecution in Test := false

Revolver.settings

import com.typesafe.sbt.SbtAspectj._

aspectjSettings

fork in run := true

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

enablePlugins(JavaServerAppPackaging)