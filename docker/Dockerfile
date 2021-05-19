FROM openjdk:8-alpine


RUN apk --update add git curl tar bash ncurses && \
    rm -rf /var/lib/apt/lists/* && \
    rm /var/cache/apk/*

ARG SBT_VERSION=1.1.0
ARG SBT_HOME=/usr/local/sbt
RUN curl -sL "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar -xz -C /usr/local

ARG SPARK_VERSION=2.4.7
ARG SPARK_HOME=/usr/local/spark-$SPARK_VERSION-bin-hadoop2.7
RUN curl -sL "http://www-us.apache.org/dist/spark/spark-$SPARK_VERSION/spark-$SPARK_VERSION-bin-hadoop2.7.tgz" | tar -xz -C /usr/local

ENV PATH $PATH:$SBT_HOME/bin:$SPARK_HOME/bin
#WORKDIR $SPARK_HOME/bin
ENV SPARK_MASTER local[*]

ENV SPARK_DRIVER_PORT 5001
ENV SPARK_UI_PORT 5002
ENV SPARK_BLOCKMGR_PORT 5003
EXPOSE $SPARK_DRIVER_PORT $SPARK_UI_PORT $SPARK_BLOCKMGR_PORT

COPY ./config-1.3.2.jar /
COPY ./log4j-api-2.11.2.jar /
COPY ./log4j-core-2.11.2.jar /
COPY ./thelogparser_2.11-0.1.jar /
COPY ./log_parser.properties /

COPY ./run.sh /
#ADD ./run.sh /
RUN chmod +x ./run.sh

#CMD [ "./run.sh" ]

#ENTRYPOINT [ "/run.sh " ]
#CMD ["spark-submit","--class","TestRun","--jars","config-1.3.2.jar","testdocker_2.11-0.1.jar"]

#CMD ["spark-submit","--class","com.util.parser.LogParser","--files","log_parser.properties","--conf spark.driver.extraJavaOptions=-Dconfig.file=log_parser.properties","--conf spark.executor.extraJavaOptions=-Dconfig.file=log_parser.properties","--jars","config-1.3.2.jar,log4j-api-2.11.2.jar,log4j-core-2.11.2.jar","thelogparser_2.11-0.1.jar","dev"]

#CMD ["spark-submit","--class","com.util.parser.LogParser","--files","./log_parser.properties","--jars","config-1.3.2.jar,log4j-api-2.11.2.jar,log4j-core-2.11.2.jar","thelogparser_2.11-0.1.jar","dev"]
CMD ["spark-submit","--class","com.util.parser.LogParser","--files","log_parser.properties","--conf", "spark.driver.extraJavaOptions=-Dconfig.file=log_parser.properties","--conf", "spark.executor.extraJavaOptions=-Dconfig.file=log_parser.properties","--jars","config-1.3.2.jar,log4j-api-2.11.2.jar,log4j-core-2.11.2.jar","thelogparser_2.11-0.1.jar","dev"]
#CMD [" spark-submit --class     TestRun   testdocker_2.11-0.1.jar"]
