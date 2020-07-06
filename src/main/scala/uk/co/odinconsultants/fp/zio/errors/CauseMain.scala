package uk.co.odinconsultants.fp.zio.errors

import java.io.{IOException, InputStream, PipedInputStream, PipedOutputStream}

import zio.blocking.{Blocking, effectBlocking}
import zio.clock.Clock
import zio.stream.ZStream
import zio.{Chunk, ExitCode, IO, UIO, URIO, ZEnv, ZIO}

object CauseMain extends zio.App {

  val someList = List(1, 2, 3)


  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val x = ZIO.foreachParN(someList.size)(someList)(item => IO(println(item)).cause) // ZIO[Any, Nothing, List[Cause[Throwable]]]
      .map(_.reduce(_ && _)) // ZIO[Any, Nothing, Cause[Throwable]]
      .uncause
    x.catchAll(e => UIO(e.printStackTrace())).as(ExitCode.success)
  }

}
