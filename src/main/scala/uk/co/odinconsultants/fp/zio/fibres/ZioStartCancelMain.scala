package uk.co.odinconsultants.fp.zio.fibres

import zio.{URIO, ZEnv, _}

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
    val zio = ZIO(sleeping).onInterrupt(cancel)
    val effect = for {
      x <- (zio >>> zio >>> zio).fork
      _ <- x.interrupt
    } yield ()

    effect.map(_ => ExitCode.success)
  }

}
