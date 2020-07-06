package uk.co.odinconsultants.fp.zio.errors

import zio._

object CauseMain extends zio.App {

  val someList = List(1, 2, 3, 4)

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val allResults:       ZIO[Any, Nothing,   List[Cause[Throwable]]] = ZIO.foreachParN(someList.size)(someList)(item => toEffect(item).cause)
    val aggregatedErrors: ZIO[Any, Nothing,   Cause[Throwable]]       = allResults.map(_.reduce(_ && _))
    val uncaused:         ZIO[Any, Throwable, Unit]                   = aggregatedErrors.uncause
    val purged:           ZIO[Any, Nothing,   Unit]                   = uncaused.catchAll(e => UIO(e.printStackTrace()))
    purged.as(ExitCode.success)
  }

  private def toEffect(x: Int): IO[Throwable, Unit] = if (x % 2 == 0) {
    IO(println(x))
  } else {
    IO(throw new Exception(s"$x"))
  }
}
