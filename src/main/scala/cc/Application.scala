package cc

import java.io.{FileReader, BufferedReader, File}
import java.util.regex.Pattern

import scopt.OptionParser

object Application extends App {
  case class Config(inputFile: File,
                    wcOutputFile: File,
                    medianOutputFile: File)

  val defaultConfig = Config(
    new File("tweet_input/tweets.txt"),
    new File("tweet_output/ft1.txt"),
    new File("tweet_output/ft2.txt"))

  val parser = new OptionParser[Config]("word-counter") {
    head("word-counter", "0.1")
    opt[File]('i', "in") valueName("<file>") action { (x, c) =>
      c.copy(inputFile = x) } text("Input file containing tweets")
    opt[File]('w', "word-out") valueName("<file>") action { (x, c) =>
      c.copy(wcOutputFile = x) } text("Output file for word count results")
    opt[File]('m', "median-out") valueName("<file>") action { (x, c) =>
      c.copy(medianOutputFile = x) } text("Output file for median results")
  }

  val configOpt = parser.parse(args, defaultConfig)

  configOpt match {
    case Some(config) =>
      val counter = new WordCounter(config.wcOutputFile)
      val tracker = new MedianTracker(config.medianOutputFile)

      execute(config.inputFile, List(counter, tracker), "concurrent")
      println("Successfully completed.")
    case None =>
      println("Unable to parse configuration.")
      System.exit(1)
  }

  /**
   * Process input file against configured Aggregators
   */
  def execute(inputFile: File, aggregators: List[Aggregator], descriptor: String): Unit = {
    val br = new BufferedReader(new FileReader(inputFile))
    val delimiter: Pattern = Pattern.compile("\\s+") // compile pattern for reuse

    // read line-by-line without reading entire file into memory
    var line: String = br.readLine()
    var count = 1
    while (line != null) {
      val trimmedLine = line.trim
      val words = delimiter.split(trimmedLine)
      aggregators.foreach(_.processLine(words))
      line = br.readLine()
      if (count % 100000 == 0) println(s"Processing $descriptor tweet #: $count")
      count += 1
    }
    br.close()

    println("Starting clean-up...")
    aggregators.foreach(_.cleanUp())
  }
}


