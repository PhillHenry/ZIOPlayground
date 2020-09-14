package uk.co.odinconsultants.fp.zio.layers
import uk.co.odinconsultants.fp.zio.layers.FromManaged.ServiceHello.ProdServiceHello
import uk.co.odinconsultants.fp.zio.layers.FromManaged.ServiceWorld.ProdServiceWorld
import zio.{ExitCode, Has, IO, UIO, ULayer, URIO, ZIO, ZLayer}

object FromManaged extends zio.App {

  type ServiceHello = Has[ServiceHello.Service]

  object ServiceHello {

    trait Service {
      def getHello: UIO[String]
    }

    def callHello: URIO[ServiceHello, String] = ZIO.accessM(_.get.getHello)

    object ProdServiceHello extends Service {
      override def getHello: UIO[String] = UIO("hello, ")
    }

    def prodServiceHello = ZIO(ProdServiceHello)

  }

  type ServiceWorld = Has[ServiceWorld.Service]

  object ServiceWorld {

    trait Service {
      def getWorld: UIO[String]
    }

    object ProdServiceWorld extends Service {
      override def getWorld: UIO[String] = UIO("world")

    }

    def callWorld: URIO[ServiceWorld, String] = ZIO.accessM(_.get.getWorld)

    def prodServiceWorld = ZIO(ServiceWorld)

  }

  def print(x: String) = ZIO(println(x)).catchAll(h => UIO(h.printStackTrace()))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val x: ZIO[ServiceHello with ServiceWorld, Nothing, String] = for {
      hello <- ServiceHello.callHello
      world <- ServiceWorld.callWorld
    } yield hello + world

    val prodWorld: ServiceWorld.Service = ProdServiceWorld
    val prodHello: ServiceHello.Service = ProdServiceHello

    val layers = ZLayer.succeed(prodWorld) ++ ZLayer.succeed(prodHello)
    val toRun  = x.provideLayer(layers)

    for {
      x <- toRun
      _ <- print(x)
    } yield ExitCode.success
  }
}
