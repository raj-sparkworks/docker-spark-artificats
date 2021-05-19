#!/bin/sh
#export SPARK_HOME=/usr/local/spark-2.4.7-bin-hadoop2.7
echo "Spark Home $SPARK_HOME"
echo "Submitting jar: $jarfile"
echo "Application main class: $MAIN_CLASS"
echo "Application arguments: $APP_ARGS"
echo "Spark master: $SPARK_MASTER"
echo "Spark driver host: $SPARK_DRIVER_HOST"
echo "Spark driver port: $SPARK_DRIVER_PORT"
echo "Spark UI port: $SPARK_UI_PORT"
echo "Spark block manager port: $SPARK_BLOCKMGR_PORT"
echo "Spark configuration settings: $SPARK_CONF"
echo "which spark-submit ----"
echo "----"
ls
echo "----"
ls /usr
echo "---"
ls /usr/local/
which spark-submit
spark-submit --master "$SPARK_MASTER" --class "$MAIN_CLASS" ./testdocker_2.11-0.1.jar $APP_ARGS
echo "End -------------------------------------------"
