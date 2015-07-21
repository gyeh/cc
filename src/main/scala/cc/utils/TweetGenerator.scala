package cc.utils

import java.io.{File, BufferedWriter, FileWriter}

import scala.annotation.tailrec
import scala.io.Source

object TweetGenerator extends App {
  val NUM_TWEETS = 20000000
  val MULTIPLIER = 16

  val lines = Source.fromFile("/usr/share/dict/words").getLines().toArray // ~250k words
  val suffixes = (1 to MULTIPLIER).map(_.toString).toArray

  val r = scala.util.Random
  val file = new File("tweet_input/tweets.txt")
  val bw = new BufferedWriter(new FileWriter(file, false))

  @tailrec
  def generateTweet(builder: StringBuilder = new StringBuilder(""),
                    remaining: Int = r.nextInt(120) + 20): String = {
    val word = lines(r.nextInt(lines.length))
    val suffix = suffixes(r.nextInt(suffixes.length))
    val newWordLength = word.length + suffix.length
    val newRemaining = remaining - newWordLength

    if (newRemaining >= 0) {
      builder.append(word).append(suffix).append(' ')
      generateTweet(builder, newRemaining)
    } else {
      builder.append("\n")
      builder.mkString
    }
  }

  for (i <- 1 to NUM_TWEETS) {
    bw.write(generateTweet())
    if (i % 1000000 == 0) println(s"at tweet #: $i")
  }
  bw.close()
}
