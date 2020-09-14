package uk.co.odinconsultants.fp.zio.kafka

import zio.{Chunk, ExitCode, URIO, ZLayer}
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde._
import org.apache.kafka.clients.producer.ProducerRecord

/**
  * From https://github.com/zio/zio-kafka
  */
object KafkaExampleMain extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val consumerSettings: ConsumerSettings =
      ConsumerSettings(List("localhost:9092")).withGroupId("group")
    val producerSettings: ProducerSettings = ProducerSettings(
      List("localhost:9092")
    )

    val consumerAndProducer =
      ZLayer.fromManaged(Consumer.make(consumerSettings)) ++
        ZLayer.fromManaged(
          Producer.make(producerSettings, Serde.int, Serde.string)
        )

    val consumeProduceStream = Consumer
      .subscribeAnd(Subscription.topics("my-input-topic"))
      .plainStream(Serde.int, Serde.long)
      .map { record =>
        val key: Int = record.record.key()
        val value: Long = record.record.value()
        val newValue: String = value.toString

        val producerRecord: ProducerRecord[Int, String] =
          new ProducerRecord("my-output-topic", key, newValue)
        (producerRecord, record.offset)
      }
      .mapChunksM { chunk =>
        val records = chunk.map(_._1)
        val offsetBatch = OffsetBatch(chunk.map(_._2).toSeq)

        Producer.produceChunk[Any, Int, String](records) *> offsetBatch.commit
          .as(Chunk(()))
      }
      .runDrain
      .provideSomeLayer(consumerAndProducer)

    consumeProduceStream.as(ExitCode.success)
    ???
  }
}
