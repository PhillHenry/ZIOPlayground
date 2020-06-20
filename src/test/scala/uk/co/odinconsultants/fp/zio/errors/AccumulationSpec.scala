package uk.co.odinconsultants.fp.zio.errors

import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import zio.ZIO

object AccumulationSpec extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] = suite("Errors should be accumulated")(testM("from ZIOs") {
    val result =ZIO.fail(1) *> ZIO.fail(2)
    assertM(result.either)(equalTo(Left(1)))
  })

}
