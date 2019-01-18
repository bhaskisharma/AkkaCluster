import beckendCluster.BeckEndCluster
import frontendCluster.FrontendCluster

object StartCluster {

  def main(args: Array[String]): Unit = {
    // starting 2 frontend nodes and 3 backend nodes
    FrontendCluster.main(Seq("2551").toArray)
    BeckEndCluster.main(Seq("2552").toArray)
    BeckEndCluster.main(Array.empty)
    BeckEndCluster.main(Array.empty)
    FrontendCluster.main(Array.empty)
  }

}
