package uk.co.odinconsultants.fp.zio.layers

import uk.co.odinconsultants.fp.zio.layers.MyLayers.{KeyVault, KeyVaultLayer}
import uk.co.odinconsultants.fp.zio.layers.MyLayers.KeyVault.Credential
import zio.blocking.Blocking
import zio.{FiberFailure, Has, Runtime, ULayer, ZEnv, ZIO, ZLayer}

object MyLayersMain {

  val blockingLayer:  ULayer[Blocking] = ZLayer.succeed(Blocking.Service.live)

  val kvService:      ULayer[KeyVaultLayer] = ZLayer.succeed(new KeyVault.KeyVaultService {
    override def keyAndPassphrase(credential: Credential): ZIO[Any, Throwable, (String, String)] = ZIO { ("key", "phrase") }
  })

  def main(args: Array[String]): Unit = {
    val zioGetKeyPhrase: ZIO[KeyVaultLayer with Blocking, Throwable, (String, String)] = for {
      credential <- Actions.blockingZIO
      keyPhrase  <- MyLayers.KeyVault.getKeyVault(credential)
    } yield keyPhrase

    val layers: ZLayer[Any, Nothing, KeyVaultLayer with Blocking] = kvService ++ blockingLayer
    val zio = zioGetKeyPhrase.provideLayer(layers)

    Runtime.default.unsafeRun(zio)
  }

}
