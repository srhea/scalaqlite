import sbt._
import Process._

class ScalaqliteProject(info: ProjectInfo) extends DefaultProject(info) {

  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val nativeSourcePath = path("src") / "main" / "native"
  val targetPath = path("target")
  val nativeTargetPath = targetPath / "native"

  def execAll(tasks: String*): Option[String] = {
    for (task <- tasks)
      if ((task ! log) != 0)
        return Some("nonzero exit code")
    None
  }

  lazy val jni = task {
    execAll("cp -R " + nativeSourcePath + " " + targetPath, "make -C " + nativeTargetPath)
  } dependsOn(compile)

  override def testAction = super.testAction dependsOn(jni)
  override def packageAction = super.packageAction dependsOn(jni)
}

