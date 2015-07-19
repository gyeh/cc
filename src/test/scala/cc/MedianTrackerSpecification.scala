package cc

import java.io.File

import org.scalacheck._
import Prop._

/**
 * Property-based test suite using ScalaCheck
 */
object MedianTrackerSpecification extends Properties("MedianTracker") {

  /**
   * Generators
   */
  val genIntArray = Gen.containerOfN[Array, Int](140, Gen.chooseNum(0, 100000))
  val genLongArray = Gen.containerOfN[Array, Long](140, Gen.chooseNum(0, Long.MaxValue / 140))

  /**
   * Helper functions
   */
  def minBucket(buckets: Array[Long]): Int = {
    buckets.zipWithIndex.foreach { case (bucketSize, bucketIndex) =>
      if (bucketSize > 0) return bucketIndex
    }
    0
  }

  def numUniqueBuckets(buckets: Array[Long]): Int = {
    buckets.foldLeft(0) { (acc, bucketSize) =>
      if (bucketSize > 0) acc + 1
      else acc
    }
  }

  // memory-issues if bucketSize is too large
  def calculateMedianNaively(buckets: Array[Int]): Double = {
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

  /**
   * Property tests
   */
  property("verify median") = forAll(genIntArray) { (buckets: Array[Int]) =>
    val r = scala.util.Random
    buckets(r.nextInt(buckets.length)) += 1 // ensure at least one word counted
    println("generated buckets: " + buckets.deep.toString)

    val tracker = new MedianTracker(new File("tweet_output/tracker_test.txt"))
    val longArray = buckets.map(_.toLong)

    val naiveMedian = calculateMedianNaively(buckets)
    val trackerMedian = tracker.calculateMedian(longArray, longArray.sum, minBucket(longArray))
    println("naiveMedian: " + naiveMedian)
    println("trackerMedian: " + trackerMedian + "\n")

    naiveMedian == trackerMedian
  }

  property("verify large numbers when calculating median") = forAll(genLongArray) { (buckets: Array[Long]) =>
    println("generated buckets: " + buckets.deep.toString)
    val tracker = new MedianTracker(new File("tweet_output/tracker_test.txt"))
    println("sum: " + buckets.sum)
    val trackerMedian = tracker.calculateMedian(buckets, buckets.sum, minBucket(buckets))
    println("trackerMedian: " + trackerMedian + "\n")
    true
  }


  // TODO: generate values from random tweets (compare the two)
}
