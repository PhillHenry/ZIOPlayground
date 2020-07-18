package uk.co.odinconsultants.fp.zio.fibres

import zio.blocking._
import zio.{URIO, ZEnv, _}

object ZioStartCancelMain extends zio.App {

  def sleeping = {
    println("About to sleep...")
    Thread.sleep(1000L)
    println("Finished")
  }

  val cancel: URIO[Any, Unit] = URIO {
    println("cancel")
  }

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val effect = effectBlockingCancelable(sleeping)(cancel)
    effect.catchAll(e => URIO(e.printStackTrace())).map(_ => ExitCode.success)
  }

}
