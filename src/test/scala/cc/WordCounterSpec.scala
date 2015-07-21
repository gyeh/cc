package cc

import java.io.File

import org.scalatest._

import scala.io.Source

class WordCounterSpec extends FlatSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll {

  private def runCounter(inputFile: File, outputFile: File): Unit = {
    val wc = new WordCounter(outputFile)
    Application.execute(inputFile, List(wc), "test")
  }

  private def verifyFilesEquivalent(file1: File, file2: File): Unit = {
    val lines1 = Source.fromFile(file1).getLines()
    val lines2 = Source.fromFile(file2).getLines()

    lines1.zip(lines2).foreach { case (line1, line2) =>
      val entries1 = line1.split("\\s+")
      val entries2 = line2.split("\\s+")

      entries1(0) shouldEqual entries2(0)
      entries1(1) shouldEqual entries2(1)
    }
  }

  "WordCounter" should "count unique words from input text" in {
    val inputFile = new File("src/test/resources/input_1.txt")
    val outputFile = new File("target/output_1.txt")
    val validatedOutputFile = new File("src/test/resources/output_1.txt")

    runCounter(inputFile, outputFile)
    verifyFilesEquivalent(outputFile, validatedOutputFile)
  }

  "WordCounter" should "generate an empty file from empty input text" in {
    val inputFile = new File("src/test/resources/input_2.txt")
    val outputFile = new File("target/output_2.txt")

    runCounter(inputFile, outputFile)
    Source.fromFile(outputFile).getLines().length shouldEqual 0
  }
}
