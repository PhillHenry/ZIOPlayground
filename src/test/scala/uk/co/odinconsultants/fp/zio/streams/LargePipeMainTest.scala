package uk.co.odinconsultants.fp.zio.streams

import org.junit.runner.RunWith
import zio.Chunk
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment.TestEnvironment
import zio.test.junit.ZTestJUnitRunner
import zio.test.{DefaultRunnableSpec, ZSpec, _}

import scala.language.postfixOps

@RunWith(classOf[ZTestJUnitRunner])
object LargePipeMainTest extends DefaultRunnableSpec {

  import LargePipeMain._

  val n = 10000
  val original      = ("0123456789" * n) + "remainder"
  val originalBytes = original.map(_.toByte).toList

  def toString(xs: Chunk[Byte]): String = new String(xs.toArray)

  val LargeChunkSize: Int = original.length / 2

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Using Java PipedXXXStreams")(
      testM ("chunks but mapping bytes"){
        val actual = piping(ZStream.fromIterable(originalBytes), LargeChunkSize).take(original.length)
        assertSameSizeAndContent(actual)
      } @@ timeout(10 seconds)
      ,
      testM ("but chunks but mapping Chunks doesn't work"){
        val actual = pipingChunks(ZStream.fromIterable(originalBytes), LargeChunkSize).take(original.length)
        assertSameSizeAndContent(actual)
      } @@ timeout(10 seconds)
    )
  }

  private def assertSameSizeAndContent(actual: ZStream[Clock with Blocking, Throwable, Byte]) = {
    for {
      sameSize <- assertM(actual.runCollect.map(x => toString(x).length))(equalTo(original.length))
      sameContent <- assertM(actual.runCollect.map(toString))(equalTo(original))
    } yield sameSize && sameContent
  }
}
