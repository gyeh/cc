package cc

import java.io.{File, FileWriter}
import java.util.regex.Pattern

import scala.collection.JavaConversions._

import com.google.common.collect.HashMultiset

class WordCounter(outputFile: File) extends Aggregator {
  private val countMap: HashMultiset[String] = HashMultiset.create()
  private val delimiter: Pattern = Pattern.compile("\\s+") // compile pattern for reuse

  outputFile.delete()

  /**
   * Count words per line
   */
  override def processLine(line: String): Unit = {
    val words = delimiter.split(line)
    words.foreach { word =>
      countMap.add(word)
    }
  }

  override def cleanUp(): Unit = {
    // sort lexicographically
    val sortedList = countMap.entrySet().toList.sortBy(_.getElement)

    // output results
    val fw = new FileWriter(outputFile, true)
    sortedList.foreach(entry => fw.write(s"${entry.getElement} ${entry.getCount}\n"))
    fw.close()
  }
}
