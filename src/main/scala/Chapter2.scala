import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

/**
 * 0 = input filename
 * 1 = output filename
 * 2 = partition size
 * 3 = master
 *    Depending on running environment, check master is set to correct data
 *    local - single machine
 *    yarn - for yarn
 *    Check here https://spark.apache.org/docs/latest/submitting-applications.html#master-urls
 **/

object Chapter2 {



  def main(args:Array[String]) {
    val inputFile = args(0)
    val outputFile = args(1)
    val partition = Integer.parseInt(args(2),10)
    val master = args(3)
    val conf = new SparkConf().setMaster(master).setAppName("wordCount")
    val sc = new SparkContext(conf)
    val input = sc.textFile(inputFile, partition)
    val words = input.flatMap(line => line.split(" "))
    val counts = words.map(word => (word,1)).reduceByKey{case (x,y) => x + y}
    counts.saveAsTextFile(outputFile)
  }
}
