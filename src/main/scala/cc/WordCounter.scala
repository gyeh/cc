package cc

import java.io.{File, FileWriter}

import scala.collection.JavaConversions._

import com.google.common.collect.HashMultiset

/**
 * Records the number of unique words from Tweet stream.
 */
class WordCounter(outputFile: File) extends Aggregator {

  // ensure initial map size accomodates potential number of unique words
  private val countMap: HashMultiset[String] = HashMultiset.create(1000000)

  outputFile.delete()

  /**
   * Count words per line
   */
  override def processLine(words: Array[String]): Unit = {
    words.foreach { word =>
      if (!word.isEmpty) countMap.add(word)
    }
  }

  override def cleanUp(): Unit = {
    // sort lexicographically, not optimized
    val sortedList = countMap.entrySet().toList.sortBy(_.getElement)

    // output results
    val fw = new FileWriter(outputFile, true)
    sortedList.foreach(entry => fw.write(s"${entry.getElement} ${entry.getCount}\n"))
    fw.close()
  }
}
