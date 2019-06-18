name := "LMDBProject"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.lmdbjava" % "lmdbjava" % "0.7.0"


libraryDependencies += "org.agrona" % "Agrona" % "0.9.1"


// https://mvnrepository.com/artifact/org.hamcrest/hamcrest-all
libraryDependencies += "org.hamcrest" % "hamcrest-all" % "1.3" % Test

// https://mvnrepository.com/artifact/org.hamcrest/java-hamcrest
//libraryDependencies += "org.hamcrest" % "java-hamcrest" % "2.0.0.0" % Test ... not works

//libraryDependencies += "org.hamcrest" % "hamcrest-junit" % "2.0.0.0" % Test ... not works

libraryDependencies += "org.lmdbjava" % "lmdbjava-native-windows-x86_64" % "0.9.23-1"
// https://mvnrepository.com/artifact/junit/junit
libraryDependencies += "junit" % "junit" % "4.12" % Test
