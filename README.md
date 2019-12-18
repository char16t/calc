Clone, build and publish library to local repository.
```bash
git clone https://github.com/char16t/calc
cd calc
sbt publishLocal
```

Add to your `build.sbt` (Scala 2.13.1 or higher):
```scala
libraryDependencies += "com.manenkov" %% "calc" % "0.1"
```
