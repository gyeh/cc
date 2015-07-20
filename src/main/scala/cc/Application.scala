package cc

import java.io.{FileReader, BufferedReader, File}

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
      execute(config.inputFile, List(counter, tracker))
    case None =>
      println("Unable to parse configuration.")
      System.exit(1)
  }

  /**
   * Process input file against configured Aggregators
   */
  def execute(inputFile: File, aggregators: List[Aggregator]): Unit = {
    val br = new BufferedReader(new FileReader(inputFile))

    // read line-by-line without reading entire file into memory
    var line: String = br.readLine()
    while (line != null) {
      aggregators.foreach(_.processLine(line))
      line = br.readLine()
    }
    br.close()

    aggregators.foreach(_.cleanUp())
  }
}


