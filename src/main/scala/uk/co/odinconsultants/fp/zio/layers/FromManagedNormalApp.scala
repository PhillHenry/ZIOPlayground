package uk.co.odinconsultants.fp.zio.layers

import uk.co.odinconsultants.fp.zio.layers.FromManaged.ServiceHello.prodServiceHello
import uk.co.odinconsultants.fp.zio.layers.FromManaged.{ServiceHello, ServiceWorld, layers, program}
import zio.ZLayer

object FromManagedNormalApp {

  val myLayer: ZLayer[Any, Nothing, ServiceWorld with ServiceHello] = ZLayer.succeed(prodServiceHello) ++ ZLayer.succeed(ServiceWorld.prodServiceWorld)

  def main(args: Array[String]): Unit = {
    println("Running program first time:")
    zio.Runtime.default.unsafeRun(program.provideLayer(myLayer))
    println("\nRunning program second time:")
    zio.Runtime.default.unsafeRun(program.provideLayer(myLayer))
  }

}
