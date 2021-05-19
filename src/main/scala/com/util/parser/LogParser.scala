package com.util.parser

/**
 * This file is to parse the log file
 * Source - compressed text file
 * Output - Aggregated results output as csv reports
 * Author Rajkumar Sukumar
 *
 */

import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.expressions.{Window, WindowSpec}
import org.apache.spark.sql.functions.{col, dense_rank,
  regexp_extract, substring, to_date}
import com.typesafe.config.ConfigFactory
import org.apache.logging.log4j.LogManager

object LogParser {

  private val LOGGER = LogManager.getLogger(LogParser.getClass)

  def main(args: Array[String]): Unit = {

    LOGGER.info("Loading the config file... ")

    /**
     * Load the env variable from the command and
     * parse the configurations
     */

    val envConfig = ConfigFactory.load().getConfig(args(0))

    val FILE_URL = envConfig.getString("file_url").trim
    val LOCAL_FILE_PATH = envConfig.getString("file_path").trim
    val LOCAL_FILE_NAME = envConfig.getString("file_name").trim
    val THE_N_COUNT = envConfig.getString("n_count").trim
    val PULL_BAD_RECS = envConfig.getString("bad_records").trim
    val VISITOR_OUT_PATH = envConfig.getString("visitor_output").trim
    val URL_OUT_PATH = envConfig.getString("url_output").trim
    val BAD_REC_OUT_PATH = envConfig.getString("url_output").trim
    val theNCount = Integer.parseInt(THE_N_COUNT)

    LOGGER.info("Calling initiateFileDownload : "+FILE_URL)

    val fileCode = FileUtil.initiateFileDownload(FILE_URL,LOCAL_FILE_NAME)

    //If unable to download the file, exits the process
    if(fileCode == 0) {
      printf("Unable to download the file..")
      return
    }

    //Create the SparkSession
    val spark = SparkSession.builder.
      appName("LogParser").
      getOrCreate()
    import spark.implicits._

    /**
     * If the input gz file having SPLITTABLE file format
     * like JSON, XML, AVRO etc.,
     * then better ** DECOMPRESS the gz file **
     * for improved performance
     *
     * Since the input .gz contain txt file, giving as it is
     */

    val rawDF = spark.read.text(LOCAL_FILE_PATH)


    /**
     * Using regular expression to parse the log file
     * ([^(\s|,)]+) - Any alpha-numerical without
     * white space and comma
     * RegEx is faster to parse compare to other techniques
     * */
    val parsedDF = rawDF.select(
      regexp_extract($"value","""^([^(\s|,)]+)""",1).alias("Visitor"),
      regexp_extract($"value","""^.*\[(\d\d/\w{3}/\d{4}:\d{2}:\d{2}:\d{2})""",1).as("Timestamp"),
      regexp_extract($"value","""^.*\w+\s+([^\s]+)\s+HTTP.*""",1).as("URL"))

    //If we need report for invalid/bad recs
    if("ON".equals(PULL_BAD_RECS.trim)) {
      /**
       * Since we are split the text file by space [""]
       * better use logical condition === "" to check empty
       * instead of col.isNull
       */

      val badRecDF = parsedDF.filter($"Visitor" === ""
        || $"Timestamp" === ""
        || $"URL" === "")

      outputStore(badRecDF,BAD_REC_OUT_PATH)
    }

    //Dropping the null records in any of the column
    val cleanRec = parsedDF.na.drop(how = "any")

    //Sometimes we might have empty values, lets to one more check
    val cleanedDF = cleanRec.filter(!($"Visitor" === ""
      || $"Timestamp" === ""
      || $"URL" === ""))

    /**
     * Now we have non-null and non-empty records
     * Convert the timestamp to Date for the below aggregation
     *
     */

    val processedDF = cleanedDF.
      withColumn("date",
        to_date(substring($"timestamp",1,11), "dd/MMM/yyyy")).
      drop("timestamp")

    //Setting up the window spec, partition by date
    val windowSpec = Window.
      partitionBy($"date").
      orderBy($"count".desc)

    //Calculate the Top N Most Freq Visitors
    val topVisitors = getTopFreqVisitors(
      processedDF,
      windowSpec,
      theNCount)

    //Calculate the Top N Most Freq URLs
    val topURL = getTopFreqURL(
      processedDF,
      windowSpec,
      theNCount)

    //Just print the sample records in console
    println("Top Visitors : ")
    topVisitors.show(10,false)

    //Just print the sample records in console
    println("Top URL : ")
    topURL.show(10,false)

    println("Writing the output to file system...")
    outputStore(topVisitors,VISITOR_OUT_PATH)
    outputStore(topURL,URL_OUT_PATH)

    LOGGER.info("All Done !!!")

  }

  //Function to calculate Top N Most Freq Visitors
  def getTopFreqVisitors(
                          processedDF : Dataset[Row],
                          windowSpec : WindowSpec,
                          theNCount : Int
                        ): Dataset[Row] =
  {
    //Group the records based on visitor
    val groupByVisitorDF = processedDF.
      groupBy("Visitor", "Date").
      count()

    /**
     * Apply dense rank fn along with date partition
     * If theNCount is 1 we will get top 1 visitor per day
     */
    val rankedVisitors = groupByVisitorDF.
      withColumn("TopVisitorsRank",
        dense_rank over windowSpec).
      filter(col("TopVisitorsRank") <= theNCount)
      .orderBy(col("date").asc, col("TopVisitorsRank").asc)

    return rankedVisitors

  }

  //Function to calculate Top N Most Freq Visitors
  def getTopFreqURL(
                     processedDF : Dataset[Row],
                     windowSpec : WindowSpec,
                     theNCount : Int
                   ): Dataset[Row] =
  {
    //Group the records based on URL
    val groupByURLDF = processedDF.
      groupBy("URL", "Date").
      count()

    /**
     * Apply dense rank fn along with date partition
     * If theNCount is 1 we will get top 1 url per day
     */

    val rankedURL = groupByURLDF.
      withColumn("TopURLRank",
        dense_rank over windowSpec).
      filter(col("TopURLRank") <= theNCount)
      .orderBy(col("date").asc, col("TopURLRank").asc)

    return rankedURL

  }

  //Function to write the results on FileSystem
  def outputStore(
                   theOutputDF: Dataset[Row],
                   theOutputPath: String) = {
   theOutputDF
      .write.option("header", "true")
      .mode("overwrite")
      .csv(theOutputPath)

    LOGGER.info("Output written to "+theOutputPath)
  }

}

