/*
Copyright 2012 Think Big Analytics, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This script was adapted from the tutorial/Tutorial[2-4].scala scripts that
come with the Scalding distribution, which is subject to the same Apache License.
*/

import com.twitter.scalding._

/**
 * This exercise uses the same features as the previous exercise, but this time
 * we'll look at the year-over-year average of Apple's stock price (so you'll 
 * know which entry points you missed...).
 */

class StockAverages3(args : Args) extends Job(args) {

  val stockSchema = 
    ('ymd, 'price_open, 'price_high, 'price_low, 'price_close, 'volume, 'price_adj_close)

  /*
   * We read CSV input for the stock records. However, Scalding Doesn't currently 
   * have a built-in class for CSV, like there is for TSV data (Tsv).
   * So we'll use an easy Scala hack. There is a base class inherited by Tsv with
   * the separated defined to "\t". We just need to instantiate a Tsv and provide
   * a class body that overrides the separator to be ",". You're welcome...
   */
  new Tsv(args("input"), stockSchema) { override val separator = "," }
    .read
    .project('ymd, 'price_close)

  /*
   * Unfortunately, we have to pass a single tuple argument to the anonymous function. 
   * It would be nice if we could use "(ymd: String, close: String)" as the argument
   * list. Note that you reference the Nth field in a tuple with the "_N" method
   * (it's not zero-indexed).
   */
    .mapTo(('ymd, 'price_close) -> ('year, 'closing_price)) { 
      ymd_close: (String, String) =>   // (String, String) === Tuple2[String, String]
      // TODO: Add exception handling logic!
      (year(ymd_close._1), java.lang.Double.parseDouble(ymd_close._2))
    }

  /*
   * Finally, group by the year and average the closing price over each year.
   */
    .groupBy('year) {group => group.average('closing_price -> 'average_close)}

    .write(Tsv(args("output")))

  def year(date: String): Int = Integer.parseInt(date.split("-")(0))
}
