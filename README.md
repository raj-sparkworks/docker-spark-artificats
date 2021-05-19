Requirement / Business case

Write a program in Scala that downloads the dataset at ftp://ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz and uses Apache Spark to determine the top-n most frequent visitors and urls for each day of the trace.  Package the application in a docker container.

Analysis / Approach to the Business case

1.Write Scala utility to download the file [Loosly coupled, if we pass any url it will download the file]
2.Write the following Scala api
a)API for parsing the log file [Should be optimized parsing technique]
b)Since the requirement is to find the top N most frequent visitor/url for each day, lets keep only these three columns ‘visitor’, ‘date’, ‘url’ and drop the remaining columns
c)Additional api to pull the null/empty records
d)All configurations should be bundled outside of the application code [no hard-coded values in the source file]
e)API for getTopNVisitor & getTopNURL
f)API to write the output to the file system
g)Package the application in Docker container

Source Code Location


Class	Description	Location

com.util.parser.LogParser
	Main class having all utility methods
	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/src/main/scala/com/util/parser/LogParser.scala


com.util.parser.FileUtil
	A class to download the file from FTP location
	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/src/main/scala/com/util/parser/FileUtil.scala


log_parser.properties 
	This file is not packed with the main jar file and it is located external holding configuration information	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/main_jar/log_parser.properties



The configuration file [log_parser.properties]

This is the configuration file, having the various config’s to run the application smoothly. The configurations are starts with dev, uat or prod. If we pass ‘dev’ as argument spark-submit then the properties related to dev will be picked on <same for uat / prod>. Hence we can use this file for multiple staging env.

dev.file_url=ftp://ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz
#Local file path
#dev.file_path=file:///home/rajkumar_sukumar/NASA_access_log_Jul95.gz
dev.file_path=./NASA_access_log_Jul95.gz
dev.file_name=NASA_access_log_Jul95.gz
#The frequency count
dev.n_count=2
#Output path for visitor report
dev.visitor_output=/home/rajkumar_sukumar/output/visitor
dev.bad_output=/home/rajkumar_sukumar/output/bad_rec
….…………….
….……………
#UAT Configurations

#Output path for url report
uat.url_output=/home/rajkumar_sukumar/output/url
#Do we need bad records ?
uat.bad_records=OFF
#Bad records report path
uat.bad_output=/home/rajkumar_sukumar/output/bad_rec




Artifacts / Dependencies

Artifacts	Description
thelogparser_2.11-0.1.jar
	The main artifacts having business use case 
config-1.3.2.jar
	Dependency jar for reading config file
log4j-api-2.11.2.jar
	Dependency jar for logger
log4j-core-2.11.2.jar
	Dependency jar for logger



Software Version

Software	Version	Description
Spark	2.3.4	
Scala	2.11.12	
SBT	0.13.18	
Platform	Google Cloud - DataProc Cluster
Image version : 1.4.61-debian10	


How to run in Spark Cluster ?

I have tested this application in GCP - DataProc Cluster, Hortonworks Sandbox [Quickstart VM] and in Docker container; I believe it should run in any Spark cluster

1.Please download the following artifacts, dependencies and config files from the below git hub location

Artifacts	Location
thelogparser_2.11-0.1.jar
	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/main_jar/thelogparser_2.11-0.1.jar

config-1.3.2.jar
	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/dependencies/config-1.3.2.jar

log4j-api-2.11.2.jar
	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/dependencies/log4j-api-2.11.2.jar

log4j-core-2.11.2.jar
	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/dependencies/log4j-core-2.11.2.jar

log_parser.properties	https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/main_jar/log_parser.properties


2.We can keep the dependencies, main jar file and the config file in different location or in same location [just for simplicity]. 

3.Please execute the following spark-submit command 
4.[Pls change the dependencies, main jar and config file location accordingly]
5.Instead of using dependency jar files we can use --packages for maven coordinates as well. However for production we can provide any nexus url.




spark-submit --class com.util.parser.LogParser --files /home/rajkumar_sukumar/log_parser.properties --conf spark.driver.extraJavaOptions=-Dconfig.file=log_parser.properties --conf spark.executor.extraJavaOptions=-Dconfig.file=log_parser.properties --jars /home/rajkumar_sukumar/log4j-core-2.11.2.jar,/home/rajkumar_sukumar/log4j-api-2.11.2.jar,/home/rajkumar_sukumar/config-1.3.2.jar /home/rajkumar_sukumar/thelogparser_2.11-0.1.jar dev



6.Explanation about the parameters used in spark-submit
 
Parameter	Description
--class	Conveying Spark to load and run this main class
--files	Passing our external config files
--config	Passing our config file to driver/executors
--jar	Including all our dependencies. We can achieve by using --package, but it is not advisable in PROD.
dev	The argument which Spark looks in the properties file and loads the configuration accordingly


How to run unit test ?

1.Please download the project from this location 
a)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/theProject/TheLogParser.7z

2.Extract and import to IDE like IntelliJ
3.Go to Terminal and run sbt test
4.The test output will be displayed in the console


Execute the application as Docker container 


1.Download the docker image compressed as tar file from the following location
a)https://drive.google.com/file/d/1CkGkUNAn_7EynVAXp1O59vKECuD-ienH/view?usp=sharing

2.Use this command to extract the docker image 
a)docker load < nasa-spark-container.tar
3.Run the image
a)docker run -ti --rm -p 5000-5010:5000-5010 -e SPARK_MASTER="local" -e MAIN_CLASS="com.util.parser.LogParser" nasa-spark-docker:1.0
4.The sample output will be displayed in the console
5.The complete report will be written to the system as per the output path defined in the properties file


Build the application in Docker container

1.Download the Dockerfile to any vm having docker installed
a)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/docker/Dockerfile

2.Download the artifacts from the below location
a)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/docker/run.sh
b)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/dependencies/config-1.3.2.jar
c)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/dependencies/log4j-api-2.11.2.jar
d)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/dependencies/log4j-core-2.11.2.jar
e)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/main_jar/thelogparser_2.11-0.1.jar
f)https://github.com/raj-sparkworks/docker-spark-artificats/blob/main/main_jar/log_parser.properties

3.Build the image
a)docker build -t nasa-spark-docker:1.0 .
4.Run the image

a)docker run -ti --rm -p 5000-5010:5000-5010 -e SPARK_MASTER="local" -e MAIN_CLASS="com.util.parser.LogParser" nasa-spark-docker:1.0

6.The sample output will be displayed in the console
7.The complete report will be written to the system as per the output path defined in the properties file


How we can extend / automate this project.

There are few manual process in the build & deploy process of this application. Like first we have to build the application, then build the Docker image using Dockerfile and then finally run the docker container. Instead of this we can use the docker plugin for mvn/sbt and when we run  the sbt package, it should build, unit test, sonar validation, int testing, create the docker image and finally run the application.
