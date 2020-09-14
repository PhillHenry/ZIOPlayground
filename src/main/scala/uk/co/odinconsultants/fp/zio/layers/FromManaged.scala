package uk.co.odinconsultants.fp.zio.layers
import uk.co.odinconsultants.fp.zio.layers.FromManaged.ServiceHello.prodServiceHello
import uk.co.odinconsultants.fp.zio.layers.FromManaged.ServiceWorld.prodServiceWorld
import zio._

object FromManaged extends zio.App {

  type ServiceHello = Has[ServiceHello.Service]

  object ServiceHello {

    trait Service {
      def getHello: UIO[String]
    }

    def callHello: URIO[ServiceHello, String] = print("callHello") *> ZIO.accessM(_.get.getHello)

    def prodServiceHello: Service = new Service {
      override def getHello: UIO[String] = UIO {
        println("getHello")
        "hello, "
      }
    }

  }

  type ServiceWorld = Has[ServiceWorld.Service]

  object ServiceWorld {

    trait Service {
      def getWorld: UIO[String]
    }


    def callWorld: URIO[ServiceWorld, String] = print("callWorld") *> ZIO.accessM(_.get.getWorld)

    def prodServiceWorld: Service = new Service {
      override def getWorld: UIO[String] = UIO {
        println("getWorld")
        "world"
      }
    }

    def zioServiceWorld = UIO(prodServiceWorld)

  }

  def print(x: String) = ZIO(println(x)).catchAll(h => UIO(h.printStackTrace()))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val x: ZIO[ServiceHello with ServiceWorld, Nothing, String] = for {
      hello <- ServiceHello.callHello
      world <- ServiceWorld.callWorld
    } yield {
      println("yield")
      hello + world
    }

    val managed = ZManaged.make(ServiceWorld.zioServiceWorld)(x => print(s"released s${x.getClass}"))
    val worldLayer: ULayer[ServiceWorld] = ZLayer.fromManaged(managed)
    val helloLayer: ULayer[ServiceHello] = ZLayer.succeed(prodServiceHello)
    val layers: ZLayer[Any, Nothing, ServiceWorld with ServiceHello] = worldLayer ++ helloLayer

    for {
      x <- x.provideLayer(layers)
      _ <- print(x)
    } yield ExitCode.success
  }
}
