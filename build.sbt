name := "dofollow"

version := "1.0"

organization := "org.hablapps"

scalacOptions ++= Seq("-feature")

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Restlet repository" at "http://maven.restlet.org",
  "Another maven repo" at "http://mavenhub.com/")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.2",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "org.restlet.jse" % "org.restlet" % "2.0.14",
  "org.restlet.jse" % "org.restlet.ext.crypto" % "2.0.14", 
  "org.restlet.jse" % "org.restlet.ext.fileupload" % "2.0.14", 
  "org.restlet.jse" % "org.restlet.ext.jetty" % "2.0.14", 
  "org.restlet.jse" % "org.restlet.ext.json" % "2.0.14", 
  "org.restlet.jse" % "org.restlet.ext.slf4j" % "2.0.14", 
  "org.restlet.jse" % "org.restlet.ext.ssl" % "2.0.14", 
  "org.simpleframework" % "org.simpleframework" % "4.1.1", 
  "commons-dbcp" % "commons-dbcp" % "1.3", 
  "commons-fileupload" % "commons-fileupload" % "1.2", 
  "commons-io" % "commons-io" % "1.4", 
  "commons-lang" % "commons-lang" % "2.5", 
  "commons-pool" % "commons-pool" % "1.5", 
  "org.json" % "org.json" % "2.0", 
  "org.jsslutils" % "jsslutils" % "1.0.5",
  "org.mortbay.jetty" % "servlet-api-2.5" % "6.1.2rc1",
  "org.eclipse.jetty" % "jetty-ajp" % "7.1.6.v20100715", 
  "org.eclipse.jetty" % "jetty-continuation" % "7.1.6.v20100715", 
  "org.eclipse.jetty" % "jetty-http" % "7.1.6.v20100715", 
  "org.eclipse.jetty" % "jetty-io" % "7.1.6.v20100715",
  "org.eclipse.jetty" % "jetty-server" % "7.1.6.v20100715", 
  "org.eclipse.jetty" % "jetty-util" % "7.1.6.v20100715", 
  "log4j" % "log4j" % "1.2.17",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.5", 
  "org.slf4j" % "jul-to-slf4j" % "1.7.5", 
  "org.slf4j" % "slf4j-api" % "1.7.5", 
  "org.slf4j" % "slf4j-log4j12" % "1.7.5",
  "org.scala-lang" % "scala-actors" % "2.10.2",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "joda-time" % "joda-time" % "2.1",
  "org.joda" % "joda-convert" % "1.2")

initialCommands in console := "import org.hablapps.{updatable,react,speech}, updatable._"