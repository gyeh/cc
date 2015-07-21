package cc

trait Aggregator {
  def processLine(words: Array[String]): Unit
  def cleanUp(): Unit
}
