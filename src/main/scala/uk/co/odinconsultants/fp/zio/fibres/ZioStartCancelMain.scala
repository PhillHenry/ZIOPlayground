package uk.co.odinconsultants.fp.zio.fibres

import zio.blocking._
import zio.{URIO, ZEnv, _}
import zio.duration._
import scala.language.postfixOps

object ZioStartCancelMain extends zio.App {

  def sleeping: Unit = {
    println("About to sleep...")
    Thread.sleep(1000L)
    println("Finished")
  }

  val cancel: URIO[Any, Unit] = URIO {
    println("cancel")
  }

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val effect = for {
      x <- ZIO(sleeping).onInterrupt(cancel).fork.onInterrupt(cancel)
      _ <- ZIO.sleep(1 second)
      _ <- x.interrupt
    } yield ()

    effect.map(_ => ExitCode.success)
  }

}
