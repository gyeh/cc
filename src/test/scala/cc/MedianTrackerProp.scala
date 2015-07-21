package cc

import java.io.File

import org.scalacheck._
import Prop._

/**
 * A property-based test suite using ScalaCheck
 */
object MedianTrackerProp extends Properties("MedianTracker") {

  /**
   * Generators
   */
  val genIntArray = Gen.containerOfN[Array, Int](140, Gen.chooseNum(0, 100000))
  val genLongArray = Gen.containerOfN[Array, Long](140, Gen.chooseNum(0, Long.MaxValue / 140))

  /**
   * Helper functions
   */
  def minBucket(buckets: Array[Long]): Int = {
    val (_, index) = buckets.zipWithIndex.find { case (bucketSize, bucketIndex) =>
      bucketSize > 0
    }.get
    index
  }

  def numUniqueBuckets(buckets: Array[Long]): Int = {
    buckets.foldLeft(0) { (acc, bucketSize) =>
      if (bucketSize > 0) acc + 1
      else acc
    }
  }

  /**
   * Property tests
   */
  property("verify median") = forAll(genIntArray) { (buckets: Array[Int]) =>
    val r = scala.util.Random
    buckets(r.nextInt(buckets.length)) += 1 // ensure at least one word counted
    println("generated buckets: " + buckets.deep.toString)

    val tracker = new MedianTracker(new File("tweet_output/tracker_test.txt"))
    val simpleTracker = new SimpleMedianTracker(new File("tweet_output/tracker_test.txt"))
    val longArray = buckets.map(_.toLong)

    val naiveMedian = simpleTracker.calculateMedian(buckets)
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
}
