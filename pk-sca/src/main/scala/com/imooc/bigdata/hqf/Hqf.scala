import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.linalg.Vectors
object Hqf {
  def main(args:Array[String]) = {
    // 屏蔽日志
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.apache.jetty.server").setLevel(Level.OFF)
    // 设置运行环境
    val conf = new SparkConf().setAppName("K-Means").setMaster("local")
//      .setJars(Seq("E:\\Intellij\\Projects\\SimpleGraphX\\SimpleGraphX.jar"))
    val sc = new SparkContext(conf)
    // 装载数据集
    val data = sc.textFile("./pk-sca/data/kmeans_data.txt", 1)
    val parsedData = data.map(s => Vectors.dense(s.split(" ").map(_.toDouble)))
    // 将数据集聚类,2个类,20次迭代,形成数据模型
    val numClusters = 2
    val numIterations = 20
    val model = KMeans.train(parsedData, numClusters, numIterations)
    // 数据模型的中心点
    println("Cluster centres:")
    for(c <- model.clusterCenters) {
      println(" " + c.toString)
    }
    // 使用误差平方之和来评估数据模型
    val cost = model.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + cost)
    // 使用模型测试单点数据
    import org.apache.spark.mllib.linalg.Vector
    val vector: Vector = Vectors.dense("7.3 1.5 10.9 ".split(" ").map(_.toDouble))

    val pre: Int = model.predict(vector)
    println(pre)

    println("Vectors 4.2 11.2 2.7 is belong to cluster:" + model.predict(Vectors.dense("4.2 11.2 2.7".split(" ")
      .map(_.toDouble))))
    println("Vectors 18.0 4.5 3.8 is belong to cluster:" + model.predict(Vectors.dense("1.0 14.5 73.8".split(" ")
      .map(_.toDouble))))
    // 返回数据集和结果
    val result = data.map {
      line =>
        val linevectore = Vectors.dense(line.split(" ").map(_.toDouble))
        val prediction = model.predict(linevectore)
        line + " " + prediction
    }.collect.foreach(println)
    sc.stop
  }
}