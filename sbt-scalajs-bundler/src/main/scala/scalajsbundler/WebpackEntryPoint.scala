package scalajsbundler

import sbt.{File, IO, Logger}

import scalajsbundler.util.JS

object WebpackEntryPoint {

  /**
    * @return The written loader file (faking a `require` implementation)
    * @param entryPoint File to write the loader to
    * @param logger Logger
    */
  def writeEntryPoint(
      imports: Seq[String],
      exports: Seq[String],
      entryPoint: File,
      logger: Logger
  ) = {
    logger.info("Writing the webpack entry-point file")
    val depsFileContent =
      JS.ref("module")
        .dot("exports")
        .assign(
          JS.obj(
            Seq(
              "require" -> JS.fun(name =>
                JS.obj(imports.map { moduleName =>
                    moduleName -> JS.ref("require").apply(JS.str(moduleName))
                  }: _*)
                  .bracket(name))) ++ exports.map(_ -> null): _*)
        )
    IO.write(entryPoint, depsFileContent.show)
    ()
  }

}
