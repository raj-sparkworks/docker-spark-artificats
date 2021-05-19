

import com.util.parser.LogParser.{getTopFreqURL, getTopFreqVisitors}
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{regexp_extract, substring, to_date}
import org.scalatest.FunSuite



class LogParserTest extends
  FunSuite with SparkSessionTestWrapper {

  import spark.implicits._

  var theNCount: Int = _
  var filterResponseCodesSeq_test1: Seq[String] = _
  var filterResponseCodesSeq_test2: Seq[String] = _

  var rawData: Seq[String] = _

  //Test env setup
  def setup_test_env() = {
    theNCount = 1

    rawData = Seq(
      ("205.189.154.54 - - [01/Jul/1995:00:00:40 -0400] \"GET /images/NASA-logosmall.gif HTTP/1.0\" 200 786"),
      ("ix-orl2-01.ix.netcom.com - - [01/Jul/1995:00:00:41 -0400] \"GET /shuttle/countdown/ HTTP/1.0\" 200 3985"),
      ("ppp-mia-30.shadow.net - - [01/Jul/1995:00:00:41 -0400] \"GET /images/NASA-logosmall.gif HTTP/1.0\" 200 786"),
      ("ppp-mia-30.shadow.net - - [01/Jul/1995:00:00:41 -0400] \"GET /images/MOSAIC-logosmall.gif HTTP/1.0\" 200 363"),
      ("205.189.154.54 - - [01/Jul/1995:00:00:41 -0400] \"GET /images/NASA-logosmall.gif HTTP/1.0\" 200 1204")
    )

  }


  //Setup environment variables and data
  setup_test_env()

  //Create spark datasets from the input data
  val rawDF = spark.createDataset(rawData)


  val parsedDF = rawDF.select(
    regexp_extract($"value","""^([^(\s|,)]+)""",1).alias("Visitor"),
    regexp_extract($"value","""^.*\[(\d\d/\w{3}/\d{4}:\d{2}:\d{2}:\d{2})""",1).as("Timestamp"),
    regexp_extract($"value","""^.*\w+\s+([^\s]+)\s+HTTP.*""",1).as("URL"))

  //Convert the timestamp to Date for the below aggrigation
  val processedDF = parsedDF.
    withColumn("date",
      to_date(substring($"timestamp",1,11), "dd/MMM/yyyy")).
    drop("timestamp")

  //Setting up the window spec
  val windowSpec = Window.
    partitionBy($"date").
    orderBy($"count".desc)

  val topVisitors = getTopFreqVisitors(
    processedDF,
    windowSpec,
    theNCount)

  val topURL = getTopFreqURL(
    processedDF,
    windowSpec,
    theNCount)

  //Test Scenarios: Begin
  test("topFreqVisitors" ) {
    assert(topVisitors.count() == 2)
  }

  test("topFreqURL") {
    assert(topURL.count() == 1)
  }




  //Test Scenarios: End




}