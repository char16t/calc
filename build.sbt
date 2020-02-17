name := "calc"
organization := "com.manenkov"
version := "0.1"
scalaVersion := "2.13.1"

licenses += ("Public Domain", url("https://unlicense.org/"))
bintrayOrganization := Some("char16t")
bintrayRepository := "maven"
publishTo := Some("bintray" at "https://api.bintray.com/maven/char16t/maven/calc/;publish=1")

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
