name := "shopmessasges"
organization :="com.shop"
version := "1.1"

scalaVersion := "2.12.6"
val akkaVersion = "2.5.13"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)