package app

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.scalalogging.LazyLogging
import config.Config
import exception.ScalerNotFoundException

import scala.concurrent.duration._

object Application extends App with LazyLogging {
  implicit val config: Config = Config.load()

  logger.info("Starting")

  val decider: Supervision.Decider = { e =>
    if (!e.isInstanceOf[ScalerNotFoundException]) {
      logger.error(e.toString)
      e.printStackTrace
    }

    Supervision.Resume
  }

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  )

  Source.tick(1.second, 60.seconds, 1)
    .via(RabbitFlow.queueSourcesFlow)
    .runWith(ScaleFlow.scaleSink)
}
