package frontendCluster

import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import java.util.concurrent.atomic.AtomicInteger

import beckendCluster.BackendRegistration
import org.apache.logging.log4j.scala.Logging

class FrontendCluster extends Actor with Logging  {

  var backends: IndexedSeq[ActorRef] = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  override def receive: Receive = {
    case _: String if backends.isEmpty =>
      logger.info("Service unavailable, cluster doesn't have backend node.")

    case job: String =>
      logger.info("JOB "+job)
      jobCounter += 1
      backends(jobCounter % backends.size) ! job

    case BackendRegistration if !backends.contains(sender()) =>
      context watch sender()
      backends = backends :+ sender()

    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}

object FrontendCluster {

  private var _frontend: ActorRef = _

  def main(args: Array[String]): Unit = {

    // Override the configuration of the port when specified as program argument
    val clusterPort = if (args.isEmpty) "0" else args(0)

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$clusterPort").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    _frontend = system.actorOf(Props[FrontendCluster], name = "frontend")

    val counter = new AtomicInteger
    import system.dispatcher
    system.scheduler.schedule(2.seconds, 2.seconds) {
      implicit val timeout = Timeout(5 seconds)
      (_frontend  ! ("Hello World " + counter.incrementAndGet()))
    }
  }

}
