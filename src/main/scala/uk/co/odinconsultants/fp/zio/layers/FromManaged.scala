package uk.co.odinconsultants.fp.zio.layers
import uk.co.odinconsultants.fp.zio.layers.FromManaged.ServiceHello.prodServiceHello
import zio._

object FromManaged extends zio.App {

  type ServiceHello = Has[ServiceHello.Service]

  object ServiceHello {

    trait Service {
      def getHello: UIO[String]
    }

    def callHello: URIO[ServiceHello, String] = print("callHello") *> ZIO.accessM(_.get.getHello)

    def prodServiceHello: Service = new Service {
      println("ServiceHello.Service created")
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
      println("ServiceWorld.Service created")
      override def getWorld: UIO[String] = UIO {
        println("getWorld")
        "world"
      }
    }

    def zioServiceWorld: UIO[Service] = UIO(prodServiceWorld)

  }

  def print(x: String) = ZIO(println(x)).catchAll(h => UIO(h.printStackTrace()))

  val program: ZIO[ServiceHello with ServiceWorld, Nothing, String] = for {
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

  /**
   * Managed resource released before String concatenation (see 'yield') but after the 'program' monad has been evaluated.
   */
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    for {
      x <- program.provideLayer(layers)
      x2 <- program.provideLayer(layers) // this creates and closes a second ServiceWorld.Service but does not create a ServiceHello.Service
      _ <- print(s"Program finished. Final outcome = '$x'")
      _ <- print(s"Program finished. Final outcome = '$x2'")
    } yield ExitCode.success
  }
}
