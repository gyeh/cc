package cc

import java.io.File

import org.scalatest._

import scala.io.Source

class MedianTrackerSpec extends FlatSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {

  private def runCounter(inputFile: File, outputFile: File): Unit = {
    val wc = new MedianTracker(outputFile)
    Application.execute(inputFile, List(wc), "test")
  }

  "MedianTracker" should "calculate a rolling median from sample input" in {
    val inputFile = new File("src/test/resources/input_1.txt")
    val outputFile = new File("target/output_3.txt")

    runCounter(inputFile, outputFile)
    Source.fromFile(outputFile).getLines().toList.zip(List("11.0", "12.5", "14.0")).foreach {
      case (line, num) => line.trim shouldEqual num
    }
  }

  "MedianTracker" should "calculate a rolling median from sample input with empty tweets" in {
    val inputFile = new File("src/test/resources/input_3.txt")
    val outputFile = new File("target/output_6.txt")

    runCounter(inputFile, outputFile)
    val lines = Source.fromFile(outputFile).getLines().toList
    val desiredOutput = List("1.0", "1.0", "1.0", "1.0", "1.0")
    lines.length shouldEqual desiredOutput.length
    lines.zip(desiredOutput).foreach {
      case (line, num) => line.trim shouldEqual num
    }
  }

  "MedianTracker" should "generate an empty file from empty input text" in {
    val inputFile = new File("src/test/resources/input_2.txt")
    val outputFile = new File("target/output_4.txt")

    runCounter(inputFile, outputFile)
    Source.fromFile(outputFile).getLines().length shouldEqual 0
  }

  "MedianTracker::calculateMedian()" should "generate correct median with input freq map" in {
    val tracker = new MedianTracker(new File("target/output_5.txt"))
    val simpleTracker = new SimpleMedianTracker(new File("target/output_5.txt"))

    tracker.calculateMedian(Array(1), 1, 0) shouldEqual simpleTracker.calculateMedian(Array(1))
    tracker.calculateMedian(Array(0, 1), 1, 1) shouldEqual simpleTracker.calculateMedian(Array(0, 1))
    tracker.calculateMedian(Array(1, 1), 2, 0) shouldEqual simpleTracker.calculateMedian(Array(1, 1))
    tracker.calculateMedian(Array(1, 1, 1), 3, 0) shouldEqual simpleTracker.calculateMedian(Array(1, 1, 1))
    tracker.calculateMedian(Array(1, 2, 1), 4, 0) shouldEqual simpleTracker.calculateMedian(Array(1, 2, 1))
    tracker.calculateMedian(Array(0, 1, 2, 1, 0, 5), 9, 1) shouldEqual simpleTracker.calculateMedian(Array(0, 1, 2, 1, 0, 5))
    tracker.calculateMedian(Array(0, 1, 2, 1, 1, 0, 5), 10, 1) shouldEqual simpleTracker.calculateMedian(Array(0, 1, 2, 1, 1, 0, 5))
  }
}
