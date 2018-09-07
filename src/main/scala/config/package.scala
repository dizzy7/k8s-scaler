import com.typesafe.config.ConfigFactory

package object config {
  case class RabbitmqConfig(
    url: String,
    username: String,
    password: String,
  )

  case class Config(rabbitmqManagementApi: RabbitmqConfig)

  object Config {

    import pureconfig._

    def load(configFile: String = "application.conf"): Config = {
      loadConfig[Config](ConfigFactory.load(configFile)) match {
        case Right(config) => config
        case Left(e) => throw new RuntimeException(e.toString)
      }
    }
  }

}
