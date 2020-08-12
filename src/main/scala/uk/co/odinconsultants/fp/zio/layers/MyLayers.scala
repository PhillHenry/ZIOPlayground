package uk.co.odinconsultants.fp.zio.layers

import zio.{Has, ZIO}

object MyLayers {

  type KeyVaultLayer = Has[KeyVault.KeyVaultService]

  object KeyVault {

    type KeyAndPhrase = (String, String)
    type Credential   = String

    trait KeyVaultService {
      def keyAndPassphrase(credential: Credential): ZIO[Any, Throwable, KeyAndPhrase]
    }

    def getKeyVault(credential: Credential): ZIO[KeyVaultLayer, Throwable, KeyAndPhrase] = ZIO.accessM(_.get.keyAndPassphrase(credential))

  }

}
