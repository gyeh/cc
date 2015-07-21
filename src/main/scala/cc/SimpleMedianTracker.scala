package cc

import java.io.{FileWriter, FileReader, BufferedReader, File}
import java.util.regex.Pattern

import scala.collection.JavaConversions._

import com.google.common.collect.HashMultiset

/**
 * Used only for testing purposes to compare with 'MedianTracker'
 */
class SimpleMedianTracker(outputFile: File) extends Aggregator {

  type BucketIndex = Int
  private val countMap: HashMultiset[String] = HashMultiset.create()

  private val MAX_BUCKETS: Int = 140
  private val buckets: Array[Long] = new Array[Long](MAX_BUCKETS)
  private var numEntries: Long = 0L
  private var minBucket: BucketIndex = MAX_BUCKETS

  outputFile.delete()
  val fw = new FileWriter(outputFile, true)

  override def processLine(words: Array[String]): Unit = {
    val bucket = numUniqueWords(words)
    if (bucket < minBucket) minBucket = bucket
    try {
      buckets(bucket) += 1
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        throw new RuntimeException(s"Attempted increment a count not within [0, $MAX_BUCKETS)")
    }
    numEntries += 1
    fw.write(f"${calculateMedian(buckets.map(_.toInt))}%.1f\n")
  }

  override def cleanUp(): Unit = {
    fw.close()
  }

  // naive approach has memory-issues if bucketSize is too large
  def calculateMedian(buckets: Array[Int]): Double = {
    def naiveMedian(seq: Seq[Int]): Double = {
      val (l, u) = seq.sortWith(_<_).splitAt(seq.size / 2)
      if (seq.size % 2 == 0)
        (l.last + u.head) / 2.0
      else u.head
    }

    val flattenedBuckets: Array[Int] = buckets.zipWithIndex.flatMap { case ((bucketSize, bucketIndex)) =>
      List.fill(bucketSize)(bucketIndex)
    }
    naiveMedian(flattenedBuckets)
  }

  private def numUniqueWords(words: Array[String]): Int = {
    words.foreach { word =>
      countMap.add(word)
    }
    val numUniques = countMap.elementSet().size()
    countMap.clear()
    numUniques
  }

}
