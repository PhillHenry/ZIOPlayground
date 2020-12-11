package uk.co.odinconsultants.fp.zio.errors

import org.junit.runner.RunWith
import zio.test.Assertion.equalTo
import zio.test.environment.TestEnvironment
import zio.test.junit.ZTestJUnitRunner
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.{Task, URIO, ZIO}

@RunWith(classOf[ZTestJUnitRunner])
class ClosingTimeTest extends DefaultRunnableSpec {

  val acquireMessage  = "acquire"

  val closeMessage    = "close"

  val closeException  = new Exception(closeMessage)

  val closeZIO: AutoCloseable => URIO[Any, Any] = x => URIO { x.close() }

  def printAndReturn(x: String): Task[String] = ZIO {
    println(x)
    x
  }

  class PathologicalResource extends AutoCloseable {
    override def close(): Unit = throw closeException
  }

  val failRelease: ZIO[Any, Throwable, String] = ZIO {
    println("resource creation")
    new PathologicalResource
  }.bracket(closeZIO) { _ =>
    printAndReturn(acquireMessage)
  }

  override def spec: ZSpec[TestEnvironment, Any] = suite("error handling")(
    testM("creation OK, acquire OK, release barfs") {
      // this works:
      val x: ZIO[Any, Throwable, String] = ZIO { throw closeException }
      val either: URIO[Any, Either[Throwable, String]] = x.either

      // this fails
//      val either: URIO[Any, Either[Throwable, String]] = failRelease.either

      val expected: Either[Throwable, String] = Left(closeException)
      assertM(either)(equalTo(expected))
    }
  )
}
