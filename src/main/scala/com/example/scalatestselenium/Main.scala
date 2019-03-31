package com.example.scalatestselenium

import java.io.File
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.WebDriver
import org.scalatestplus.selenium.WebBrowser
import org.scalatest.concurrent.Eventually._
import scala.concurrent.duration._
import org.openqa.selenium.By.ByName

object Main {

  import WebBrowser._

  def main(args: Array[String]): Unit = {

    var wd: WebDriver = null
    try {
      wd = chrome(false)
      implicit val iwd = wd

      go to "https://google.com"

      val e = eventually {
        find( name("q"))
      }

      println("Element is "+e)

    } finally {
      stopChrome()
    }

  }

  val debug = false


  private var chromeDriverService: Option[ChromeDriverService] = None
  private var webDriver: Option[WebDriver] = None

  private def chrome( headless: Boolean ) = {
    // does not work
//    val options = new ChromeOptions()
//    options.addArguments("--verbose", "--log-path=C:\\temp\\chrome_test.log")

    val logfile = new File("logs", s"chromedriver.log")

    val service = if (debug) {
      println( s"Logfile for chromedriver is ${logfile}" )
      new ChromeDriverService.Builder()
                      .usingAnyFreePort()
                      .withSilent(false)
                      .withLogFile(logfile)
                      .withVerbose(true)
                      .build()
    } else {
      new ChromeDriverService.Builder()
                      .usingAnyFreePort()
                      .withSilent(true)
                      .build()
    }

    try {
      chromeDriverService = Some(service)
      service.start()
      val options = new ChromeOptions
      options.addArguments("--disable-infobars")
      if (headless) {
        options.addArguments("--headless")
        options.addArguments("--window-size=1920,1080")
      }
      val capabilities = DesiredCapabilities.chrome();
      capabilities.setCapability(ChromeOptions.CAPABILITY, options);
     println("Starting remote driver for chrome")
      val dr = new RemoteWebDriver(service.getUrl(), capabilities)
      println("Started remote driver for chrome")
      webDriver = Some(dr)
      dr
    } catch {
      case x: Throwable =>
        println("Exception starting remote driver for chrome")
        x.printStackTrace()
        service.stop()
        chromeDriverService = None
        throw x
    }

  }

  def stopChrome(): Unit = synchronized {
    webDriver match {
      case Some(wd) =>
        webDriver = None
        wd.close()
        wd.quit()
      case None =>
    }

    chromeDriverService.map( cds => cds.stop() )
    chromeDriverService = None
  }

}
