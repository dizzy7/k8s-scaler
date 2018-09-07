package app

import akka.stream.scaladsl.{Flow, Sink}
import akka.{Done, NotUsed}
import app.RabbitFlow.Queue
import app.Scalers.{Deployment, Scaler}
import com.typesafe.scalalogging.LazyLogging
import exception.ScalerNotFoundException
import io.kubernetes.client.Configuration
import io.kubernetes.client.apis.AppsV1Api
import io.kubernetes.client.util.Config

import scala.concurrent.Future

object ScaleFlow extends LazyLogging {
  private val k8sClient = Config.defaultClient
  private val namespace = "statistic-prod"
  Configuration.setDefaultApiClient(k8sClient)
  private val k8sApi = new AppsV1Api()

  private val getDeployment: Flow[Queue, (Queue, Deployment, Scaler), NotUsed] = Flow[Queue].map { queue =>
    val deploymentNameO = Scalers.scalers.get(queue.name)

    deploymentNameO match {
      case Some((deploymentName, scaler)) => (queue, deploymentName, scaler)
      case _ => throw new ScalerNotFoundException
    }
  }

  private val scaleDeployment: Sink[(Queue, Deployment, Scaler), Future[Done]] = Sink.foreach[(Queue, Deployment, Scaler)] {
    i =>
      val (queue, deployment, scaler) = i

      val scale = k8sApi.readNamespacedDeploymentScale(deployment, namespace, null)
      val currentSize = scale.getStatus.getReplicas
      val newSize = scaler(queue)

      if (currentSize != newSize) {
        logger.warn(s"deployment $deployment scale $currentSize => $newSize (${queue.messages} messages, ${queue.messages_details.rate} rate)")
        scale.getSpec.setReplicas(newSize)
        k8sApi.replaceNamespacedDeploymentScale(deployment, namespace, scale, "pretty_example")
      }
  }

  val scaleSink: Sink[Queue, NotUsed] = getDeployment.to(scaleDeployment)
}
