## To run:
`./run.sh`

Tested on MBP 64-bit + Debian 8 64-bit

## Dependencies
* Java JVM 7 or 8 to run the uber jar
* Uber jar contains all libraries and dependencies.

## Design
Preliminary runs indicate that `WordCounter` and `MedianTracker` takes ~3 minutes to process
20 millions tweets, which is about the number of tweets Twitter generates per hour (from 
http://goo.gl/eWTrtz).

Some Scala-specific idioms were set aside (e.g. lambdas, for-comprehensions) to eek out minor
performance gains on the hot-path.

Because specs of the running machine is unknown, there was no JVM-tuning involved (e.g. minimal
heap size, GC algo, etc).

### WordCounter
`WordCounter` uses an in-memory Guava Multi-Set map to handle the primary string frequency operations.
The primary concern is memory usage: to store a high volume of potential unique words in
memory. (The number of unique words in the english language is about 1 million (http://goo.gl/f4wGg9).
This is not taking to account of other languages or the various permutations due to uncleaned
puncuations and grammar.)
 
A casual experiment with 4 million unique words from a corpus of 20 million genererated tweets,
resulted in a ~350MB map on a 64-bit JVM. This makes a bit more sense considering that a 17 character 
Java String can be 72 bytes in memory (not accounting the hashmap and long counter overhead).

Potential further optimizations include a more efficient string encoding, tries, alternative map
data structures, off-heap solutions to mitigate GC issues.

### MedianTracker
To be space-optimized and not track the unique counts for all tweets, `MedianTracker` uses a `long`
array ('buckets') to track the unique words counts from each tweet. The array index represents
the number of unique words per tweet. The array value represents the number of tweets which falls
into the bucket.

To calculate the new median, 'buckets' is traversed linearly each time to reach the median.
Because tweets are limited to 140 characters and we're assuming that only whitespaces can delimit
words, the 'buckets' size is bounded to a small constant, resulting in a small traversal cost.

