package cc

import java.io.{FileWriter, FileReader, BufferedReader, File}
import java.util.regex.Pattern

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

import com.google.common.collect.HashMultiset

/**
 * To be space-optimized and not track the unique counts for all tweets, MedianTracker uses an Long
 * array ('buckets') to track the unique words counts from each tweet. The array index represents
 * the number of unique words per tweet. The array value represents the number of tweets which falls
 * into the bucket.
 *
 * To calculate the new median, 'buckets' is traversed linearly each time to reach the median.
 * Because tweets are limited to 140 characters and we're assuming that only whitespaces can delimit
 * words, the 'buckets' size is bounded to a small constant -- resulting in a small traversal cost.
 */
class MedianTracker(outputFile: File) extends Aggregator {
  
  type BucketIndex = Int
  private val delimiter: Pattern = Pattern.compile("\\s+") // compile pattern for reuse
  private val countMap: HashMultiset[String] = HashMultiset.create()

  private val MAX_BUCKETS: Int = 140
  private val buckets: Array[Long] = new Array[Long](MAX_BUCKETS)
  private var numEntries: Long = 0L
  private var minBucket: BucketIndex = MAX_BUCKETS

  private val log = Logger(LoggerFactory.getLogger("MedianTracker"))

  outputFile.delete()
  val fw = new FileWriter(outputFile, true)


  override def processLine(line: String): Unit = {
    val bucket = numUniqueWords(line)
    if (bucket < minBucket) minBucket = bucket
    try {
      buckets(bucket) += 1
    } catch {
      case e: ArrayIndexOutOfBoundsException =>
        throw new RuntimeException(s"Attempted increment a count not within [0, $MAX_BUCKETS)")
    }
    numEntries += 1
    fw.write(f"${calculateMedian(buckets, numEntries, minBucket)}%.1f\n")
  }

  override def cleanUp(): Unit = {
    fw.close()
  }

  /**
   * O(n = MAX_BUCKETS) to calculate the median
   *
   * @param buckets An array which maps a tweet's unique-counts to the # of tweets which falls into
   *                that bucket
   * @param numEntries Total number of entries stored in 'buckets'
   * @param minBucket Smallest bucket within 'buckets'. Used for optimization.
   * @return The median calculated from 'buckets'
   */
  protected[cc] def calculateMedian(buckets: Array[Long], numEntries: Long, minBucket: BucketIndex): Double = {
    val isOddCount = numEntries % 2 != 0
    val (bucketIndex, isMedianAtTheEnd) = findBucket(buckets, numEntries, minBucket, isOddCount)

    if (isOddCount) bucketIndex
    else {
      // for the even case, need to find right side of median to find average
      // (left side is already in bucket)
      if (!isMedianAtTheEnd) {
        // can assume left and right side of median are within the same bucket
        bucketIndex
      } else {
        // if median is at the end of bucket, need to grab the next bucket for the right-side median
        for (nextBucket <- bucketIndex + 1 until buckets.length) {
          // ignore empty buckets
          if (buckets(nextBucket) > 0) {
            return (bucketIndex + nextBucket) / 2.0
          }
        }
        bucketIndex
      }
    }
  }

  private def numUniqueWords(line: String): Int = {
    val words = delimiter.split(line)
    words.foreach { word =>
      countMap.add(word)
    }
    val numUniques = countMap.elementSet().size()
    countMap.clear()
    numUniques
  }

  /**
   * Find the bucket where the median is located in.
   *
   * @return The median bucket and whether the median is located at the end of bucket
   */
  private def findBucket(buckets: Array[Long],
                         numEntries: Long,
                         minBucket: BucketIndex,
                         isOddCount: Boolean): (BucketIndex, Boolean) = {
    val medianPosition =
      if (numEntries <= 2) 0
      else if (isOddCount) numEntries / 2
      else numEntries / 2 - 1

    var currEntries = 0L
    // iterate through all the buckets starting with smallest non-empty bucket
    for (currBucket <- minBucket until buckets.length) {
      // ignore empty buckets
      if (buckets(currBucket) > 0) {
        val bucketSize = buckets(currBucket)
        if ((currEntries <= medianPosition) && (medianPosition < currEntries + bucketSize)) {
          // median exists in this bucket
          val isMedianAtTheEnd = currEntries + bucketSize - 1 == medianPosition
          return (currBucket, isMedianAtTheEnd)
        } else {
          currEntries += bucketSize
        }
      }
    }
    // if no unique-counts are recorded in 'buckets'
    (0, false)
  }
}
