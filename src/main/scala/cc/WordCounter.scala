package cc

import java.io.{File, FileWriter}
import java.util.regex.Pattern
import org.mapdb._

import scala.collection.JavaConversions._

/**
 * Using a disk backed map for word-count frequency, in order to track potentially billions of
 * unique "words" without overwhelming the heap (also cannot be dependent on in-memory storage b/c
 * unable to tune the jvm due to unknown system specs of host machine).
 *
 * Alternative solutions include: using alternative off-heap data stores (e.g. mmap, berkeleydb),
 * using a trie, use a more efficient String encoding scheme.
 */
class WordCounter(outputFile: File) extends Aggregator {

  private val countMap = DBMaker.newTempHashMap[String, Long]()
  private val delimiter = Pattern.compile("\\s+") // compile pattern for reuse

  outputFile.delete()

  /**
   * Count words per line
   */
  override def processLine(line: String): Unit = {
    val words = delimiter.split(line)
    words.foreach { word =>
      val count = countMap.get(word)
      if (count == null) {
        countMap.put(word, 1)
      } else {
        countMap.put(word, count + 1)
      }
    }
  }

  override def cleanUp(): Unit = {
    // sort lexicographically. not optimized for lots of words
    val sortedList = countMap.entrySet().toList.sortBy(_.getKey)

    // output results
    val fw = new FileWriter(outputFile, true)
    sortedList.foreach(entry => fw.write(s"${entry.getKey} ${entry.getValue}\n"))
    fw.close()
    countMap.close()
  }
}
