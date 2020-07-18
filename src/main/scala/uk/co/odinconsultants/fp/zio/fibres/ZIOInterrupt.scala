package uk.co.odinconsultants.fp.zio.fibres

import zio.blocking._
import zio.duration._
import zio.{ZIO, console}

import scala.util.Try

/**
 * From https://gist.github.com/kamilkloch/f9190dca1eb71de0fa4fe5771a23d2b4
 */
object ZIOInterrupt extends App {

  val runtime = zio.Runtime.default

  val t1 = effectBlockingInterrupt {
    try {
      println(s"t1 evaluating on ${Thread.currentThread()}")
      Thread.sleep(200)
      println("t1 finished")
      "t1"
    } catch {
      case e: InterruptedException => Console.err.println(s"t1 interrupted on ${Thread.currentThread()}"); throw e
    }
  }

  val t2 = t1.flatMap { x =>
    try {
      println(s"inner begin evaluating on ${Thread.currentThread()}")
      Thread.sleep(100)
      println("inner after sleep")
      effectBlockingInterrupt {
        try {
          println(s"inner Task begin evaluating on ${Thread.currentThread()}")
          Thread.sleep(1000)
          println("inner Task after sleep")
          s"t2($x)"
        } catch {
          case e: InterruptedException => Console.err.println(s"t2 inner task interrupted on ${Thread.currentThread()}"); throw e
        }
      }
    } catch {
      case e: InterruptedException => Console.err.println(s"t1 interrupted on ${Thread.currentThread()}"); throw e
    }
  }

  val f2 = runtime.unsafeRunToFuture(t2)
  Thread.sleep(100)
  f2.cancel()

  println("===============================================")

  val g = for {
    x <- t2.fork
    _ <- console.putStrLn(s"main loop: evaluating on ${Thread.currentThread()}")
    _ <- ZIO.sleep(100.millis) >>> x.interrupt
    r <- x.join
  } yield r

  runtime.unsafeRun(g)
}

/*
Result:
t1 evaluating on Thread[zio-default-blocking-2-252480153,5,zio-default-blocking]
t1 interrupted on Thread[zio-default-blocking-2-252480153,5,zio-default-blocking]
===============================================
t1 evaluating on Thread[zio-default-blocking-1-252480153,5,zio-default-blocking]
main loop: evaluating on Thread[main,5,main]
t1 finished
inner begin evaluating on Thread[zio-default-async-5-205721196,5,zio-default-async]
inner after sleep
Exception in thread "main" zio.FiberFailure: Fiber failed.
An interrupt was produced by #3.
No ZIO Trace available.
Question:
why does unsafeRun behave differently?
*/
