package app

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.{HttpRequest, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import app.Application.{materializer, system}
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.concurrent.ExecutionContext.Implicits.global

object RabbitFlow {
  case class MessagesDetails(rate: Float)
  case class Queue(name: String, messages: Int, messages_details: MessagesDetails)


  private val rabbitConfig = Application.config.rabbitmqManagementApi
  private val rabbitUrl = s"${rabbitConfig.url}/api/queues/%2F/"
  private val auth = Authorization(headers.BasicHttpCredentials(rabbitConfig.username, rabbitConfig.password))
  private val request = HttpRequest(uri = rabbitUrl, headers = List(auth))

  private val sourceRequest: Flow[Int, String, NotUsed] = Flow[Int].mapAsync(1) { i =>
    Http().singleRequest(request).flatMap({ response => Unmarshal(response.entity).to[String] })
  }

  private val decodeResponse: Flow[String, List[Queue], NotUsed] = Flow[String].map { s =>
    decode[List[Queue]](s) match {
      case Right(x) => x
      case Left(e) => throw e
    }
  }

  val queueSourcesFlow: Flow[Int, Queue, NotUsed] =
    sourceRequest
      .via(decodeResponse)
      .mapConcat(identity)
}
