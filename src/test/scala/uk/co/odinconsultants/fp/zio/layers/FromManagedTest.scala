package uk.co.odinconsultants.fp.zio.layers

import org.junit.runner.RunWith
import zio.test.{DefaultRunnableSpec, ZSpec, suite, testM}
import zio.test.junit.ZTestJUnitRunner
import zio.test.Assertion._
import zio.test._

@RunWith(classOf[ZTestJUnitRunner])
class FromManagedTest extends DefaultRunnableSpec {

  import FromManaged._

  override def spec: ZSpec[_root_.zio.test.environment.TestEnvironment, Any] = suite("Order of layers being applied")(
    testM("result") {
      assertM(program.provideLayer(layers))(equalTo("hello, world"))
    }
  )
}
