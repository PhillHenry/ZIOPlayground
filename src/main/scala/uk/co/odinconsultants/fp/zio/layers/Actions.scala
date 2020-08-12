package uk.co.odinconsultants.fp.zio.layers

import uk.co.odinconsultants.fp.zio.layers.MyLayers.KeyVault.Credential
import zio.RIO
import zio.blocking.Blocking

object Actions {

  def myBlockCode(): Credential = {
    println("Well, not really that blocking...")
    "credential"
  }

  def blockingZIO: RIO[Blocking, Credential] = zio.blocking.effectBlocking(myBlockCode())

}
