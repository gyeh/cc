package cc

import java.io.{FileWriter, File}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

import com.google.common.collect.HashMultiset

/**
 * Calculates a rolling median of unique words for tweets.
 */
class MedianTracker(outputFile: File) extends Aggregator {

  type BucketIndex = Int
  private val countMap: HashMultiset[String] = HashMultiset.create()

  private val MAX_BUCKETS: Int = 140
  private val buckets: Array[Long] = new Array[Long](MAX_BUCKETS)
  private var numEntries: Long = 0L
  private var minBucket: BucketIndex = MAX_BUCKETS

  private val log = Logger(LoggerFactory.getLogger("MedianTracker"))

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
        var nextBucket = bucketIndex + 1
        while (nextBucket < buckets.length) {
          // ignore empty buckets
          if (buckets(nextBucket) > 0) {
            return (bucketIndex + nextBucket) / 2.0
          }
          nextBucket += 1
        }
        bucketIndex
      }
    }
  }

  private def numUniqueWords(words: Array[String]): Int = {
    var i = 0
    while (i < words.length) {
      countMap.add(words(i))
      i += 1
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
    var currBucket = minBucket
    while(currBucket < buckets.length) {
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
      currBucket += 1
    }
    // if no unique-counts are recorded in 'buckets'
    (0, false)
  }
}
