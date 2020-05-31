package uk.co.odinconsultants.fp.zio.streams

import zio.duration._
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, _}
import scala.language.postfixOps

object LargePipeMainSpec extends DefaultRunnableSpec {

  import LargePipeMain._

  val n = 10000
  val original      = ("0123456789" * n) + "remainder"
  val originalBytes = original.map(_.toByte).toList

  def toString(xs: List[Byte]): String = new String(xs.toArray)

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("efficiency of large streams")(
      testM ("should maintain integrity"){
        val actual = piping(ZStream.fromIterable(originalBytes), 1024).take(original.length)
        for {
          sameSize    <- assertM(actual.runCollect.map(x => toString(x).length))(equalTo(original.length))
          sameContent <- assertM(actual.runCollect.map(toString))(equalTo(original))
        } yield sameSize && sameContent

      } @@ timeout(10 seconds)
    )
  }

}
