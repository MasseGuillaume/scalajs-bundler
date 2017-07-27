package scalajsbundler.util

import org.scalajs.core.ir.Trees.JSNativeLoadSpec
import org.scalajs.core.tools.io.VirtualScalaJSIRFile
import org.scalajs.core.tools.linker._
import org.scalajs.core.tools.linker.standard._
import org.scalajs.core.tools.linker.backend.{BasicLinkerBackend, LinkerBackend}
import org.scalajs.sbtplugin.Loggers
import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.ModuleKind
import sbt.Logger

object ScalaJSOutputAnalyzer {

  /**
    * @return The list of ES modules imported by a Scala.js project
    * @param linkerConfig Configuration of the Scala.js linker
    * @param linker Scala.js linker
    * @param irFiles Scala.js IR files
    * @param moduleInitializers Scala.js module initializers
    * @param logger Logger
    */
  def findImportedModules(
      linkerConfig: StandardLinker.Config,
      linker: ClearableLinker,
      irFiles: Seq[VirtualScalaJSIRFile],
      moduleInitializers: Seq[ModuleInitializer],
      logger: Logger
  ): List[String] = {
    require(linkerConfig.moduleKind == ModuleKind.CommonJSModule,
            s"linkerConfig.moduleKind was ${linkerConfig.moduleKind}")
    val symbolRequirements = {
      val backend = new BasicLinkerBackend(linkerConfig.semantics,
                                           linkerConfig.outputMode,
                                           linkerConfig.moduleKind,
                                           linkerConfig.sourceMap,
                                           LinkerBackend.Config())
      backend.symbolRequirements
    }
    val linkingUnit =
      linker.linkUnit(irFiles,
                      moduleInitializers,
                      symbolRequirements,
                      Loggers.sbtLogger2ToolsLogger(logger))
    linkingUnit.classDefs
      .flatMap(_.jsNativeLoadSpec)
      .flatMap {
        case JSNativeLoadSpec.Import(module, _) => List(module)
        case JSNativeLoadSpec.ImportWithGlobalFallback(
            JSNativeLoadSpec.Import(module, _),
            _) =>
          List(module)
        case JSNativeLoadSpec.Global(_) => Nil
      }
      .distinct
  }

  /**
    * @return The list of ES modules exported by a Scala.js project
    * @param linkerConfig Configuration of the Scala.js linker
    * @param linker Scala.js linker
    * @param irFiles Scala.js IR files
    * @param moduleInitializers Scala.js module initializers
    * @param logger Logger
    */
  def findTopLevelExports(
                           linkerConfig: StandardLinker.Config,
                           linker: ClearableLinker,
                           irFiles: Seq[VirtualScalaJSIRFile],
                           moduleInitializers: Seq[ModuleInitializer],
                           logger: Logger
                         ): List[String] = {
    require(linkerConfig.moduleKind == ModuleKind.CommonJSModule,
      s"linkerConfig.moduleKind was ${linkerConfig.moduleKind}")
    val symbolRequirements = {
      val backend = new BasicLinkerBackend(linkerConfig.semantics,
        linkerConfig.outputMode,
        linkerConfig.moduleKind,
        linkerConfig.sourceMap,
        LinkerBackend.Config())
      backend.symbolRequirements
    }
    val linkingUnit =
      linker.linkUnit(irFiles,
        moduleInitializers,
        symbolRequirements,
        Loggers.sbtLogger2ToolsLogger(logger))
    linkingUnit.classDefs
      .flatMap(_.topLevelExportNames)
      .distinct
  }

}
