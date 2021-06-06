name := "calc"
organization := "com.manenkov"
version := "0.2"
scalaVersion := "2.13.6"

licenses += ("Public Domain", url("https://unlicense.org/"))

githubOwner := "char16t"
githubRepository := "calc"
githubTokenSource := TokenSource.GitConfig("github.token")

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"
