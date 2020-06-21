package uk.co.odinconsultants.fp.zio.errors

import zio.ZIO.fail
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.{Cause, IO, ZIO}
import zio.test.TestAspect._

object AccumulationSpec extends DefaultRunnableSpec {

  val failure1: IO[Int, Nothing] = fail(1)
  val failure2: IO[Int, Nothing] = fail(2)

  val failures  = List(failure1, failure2)

  override def spec: ZSpec[TestEnvironment, Any] = suite("Errors should be accumulated")(
  testM("from ZIOs") {
      val result: ZIO[Any, Int, List[Nothing]] = ZIO.collectAllPar(failures)
      assertM(result.either)(equalTo(Left(1)))
    } @@ ignore,
    testM("from ZIOs") {
      val result: ZIO[Any, Int, (Nothing, Nothing)] = failure1.validate(failure2)
      assertM(result.either)(equalTo(Left(1)))
    },
    testM("combines both cause") {
      assertM(failure1.validate(failure2).sandbox.either)(
        isLeft(equalTo(Cause.Then(Cause.Fail(1), Cause.Fail(2))))
      )
    }
  )

}
