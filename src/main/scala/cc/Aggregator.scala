package cc

trait Aggregator {
  def processLine(line: String): Unit
  def cleanUp(): Unit
}
