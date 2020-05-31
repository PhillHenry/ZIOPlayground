package uk.co.odinconsultants.fp.zio.streams

import java.io.{IOException, InputStream, PipedInputStream, PipedOutputStream}

import zio.blocking.{Blocking, effectBlocking}
import zio.clock.Clock
import zio.stream.ZStream
import zio.{Chunk, ZIO}

object LargePipeMain {

  val pipes: ZIO[Any, Nothing, (PipedInputStream, PipedOutputStream)] = for {
    out <- ZIO.effectTotal(new PipedOutputStream)
    in  <- ZIO.effectTotal(new PipedInputStream(out))
  } yield (in, out)


  def piping(input: ZStream[Clock, IOException, Byte], chunkSize: Int = 5): ZStream[Clock with Blocking, Throwable, Byte] = {

    ZStream.fromEffect(pipes).flatMap { case (in, out) =>
      ZStream.fromInputStream(in).drainFork(input.chunkN(chunkSize).mapM { b => ZIO{out.write(b)} })
    }

  }

}