package com.util.parser


import java.net.URL

import java.io.InputStream
import java.io.OutputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream

import org.apache.log4j.{ Level, Logger }


object FileUtil {

  val logger = Logger.getLogger(this.getClass.getName())

  //def main(args: Array[String]): Unit = {
  def initiateFileDownload(inputURL : String, filePath : String) : Int = {

    val url = new URL(inputURL)
    var return_code = 1
    var inputStream: InputStream = null
    var outputStream: OutputStream = null

    try  {
      val connection = url.openConnection()
      connection.setConnectTimeout(5000)
      connection.setReadTimeout(5000)
      connection.connect()
      logger.info("Connection to FTP server successfull.")
      System.out.println("Connected to FTP...")
      inputStream = connection.getInputStream
      val fileToDownloadAs = new java.io.File(filePath)
      outputStream = new BufferedOutputStream(new FileOutputStream(fileToDownloadAs))
      logger.info("Downloading file from location- " + inputURL)
      logger.info("Downloading file to location- " + filePath)
      val byteArray = Stream.continually(inputStream.read).takeWhile(-1 !=).map(_.toByte).toArray
      outputStream.write(byteArray)
      return return_code
    } catch {
      case ex: Exception =>
      {
        //logger.error(s"getFile from ftp failed. Msg: $ex");
        System.out.println("Error : "+ex.printStackTrace())
        return_code = 0
      }

    } finally {
      try {
        inputStream.close()
        outputStream.close()
      } catch {
        case ex: Exception => System.out.println(s"Closing input/output streams. Msg: $ex")
      }

    }

    return return_code

  }

}

