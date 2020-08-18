package uk.co.odinconsultants.fp.zio.errors

import org.junit.runner.RunWith
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.{Cause, IO, ZIO}
import zio.test.TestAspect._
import zio.test.junit.ZTestJUnitRunner

@RunWith(classOf[ZTestJUnitRunner])
object AccumulationTest extends DefaultRunnableSpec {

  val failure1: IO[Int, Nothing] = ZIO.fail(1)
  val failure2: IO[Int, Nothing] = ZIO.fail(2)

  val failures: Seq[ZIO[Any, Int, Any]] = List(failure1, failure2)

  override def spec: ZSpec[TestEnvironment, Any] = suite("Errors should be accumulated")(
  testM("from ZIOs") {
      val result = ZIO.collectAllPar(failures)
      assertM(result.either)(equalTo(Left(1)))
    } @@ ignore /* as it's non-deterministic */
    ,
    testM("from ZIOs") {
      assertM(failure1)(equalTo(1))
    } @@ ignore // A checked error was not handled. 1
    ,
    testM("from validation of ZIOs") {
      val result: IO[Int, (Nothing, Nothing)] = failure1.validate(failure2)
      assertM(result.run)(
        fails(
          equalTo(1)
        )
      )
    }
    ,
    testM("from ZIOs vai Either") {
      val result: ZIO[Any, Int, (Nothing, Nothing)] = failure1.validate(failure2)
      assertM(result.either)(
        isLeft(equalTo(1))
      )
    }
    ,
    testM("combines both cause (taken from ZIO codebase)") {
      assertM(failure1.validate(failure2).sandbox.either)(
        isLeft(equalTo(Cause.Then(Cause.Fail(1), Cause.Fail(2))))
      )
    }
    ,
    testM("combines both cause") {
      assertM(failure1.validate(failure2).sandbox.either)(
        isLeft(equalTo(Cause.Then(Cause.Fail(1), Cause.Fail(2))))
      )
    }
  )

}
