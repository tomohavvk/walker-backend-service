import Library.CompilerPlugins
import sbt.Keys._
import sbt.Def
import sbt._
import sbtbuildinfo.BuildInfoKeys._
import sbtbuildinfo._
import com.typesafe.sbt.SbtNativePackager.autoImport.packageName
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._

object BuildConfig {

  object DockerConfig {

    val settings = Seq(
      dockerBaseImage := "openjdk:8-jre-slim",
      dockerUsername := Some("ihorzadyra"),
      Docker / packageName := "walker-backend-service",
      dockerExposedPorts := Seq(9000)
    )
  }

  val scalacOptionsConfig: Seq[String] = Seq(
    "-language:higherKinds",
    "-language:postfixOps",
    "-Ypartial-unification",
    "-language:implicitConversions"
  )

  val additionalSettings: Seq[Def.Setting[_]] = Seq(
    Test / parallelExecution := false,
    addCompilerPlugin(CompilerPlugins.kindProjector),
    addCompilerPlugin(CompilerPlugins.macros)
  )

  object BuildInfoConfig {
    private val serviceName         = BuildInfoKey.map(name)(_._1 -> "walker-backend-service")
    private val packageName         = "org.tomohavvk.walker"
    private val buildInfoKeysCustom = Seq[BuildInfoKey](version, scalaVersion, sbtVersion, serviceName)

    val settings: Seq[Def.Setting[_]] = Seq(
      buildInfoKeys := buildInfoKeysCustom,
      buildInfoPackage := packageName,
      buildInfoOptions += BuildInfoOption.BuildTime,
      buildInfoOptions += BuildInfoOption.ToJson
    )
  }

}
