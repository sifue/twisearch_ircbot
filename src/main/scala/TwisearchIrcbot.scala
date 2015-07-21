import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.util.Properties
import java.io.FileInputStream

/**
 * Main application.
 */
object TwisearchIrcbot extends App {
  val conf = new TwisearchIrcbotConfig(args)

  val ircClient = new Client(
    conf.ircAddress,
    conf.ircChannel,
    conf.ircNickname,
    conf.ircCharset)

  val twitter = TwitterFactory.getSingleton
  twitter.setOAuthConsumer(
    conf.consumerKey,
    conf.consumerSecret)
  twitter.setOAuthAccessToken(new AccessToken(
    conf.accessToken,
    conf.accessTokenSecret
  ))

  val actorSystem = ActorSystem("twisearch")
  val searcher = actorSystem.actorOf(Props(classOf[TwitterSearcher],
    ircClient,
    conf.limitCount,
    conf.intervalSec,
    conf.messageFormat,
    conf.noticeFormat,
    twitter))

  actorSystem.scheduler.schedule(0 seconds, conf.intervalSec seconds, searcher, SearchTwitter(conf.keyword))

  val bootNotice = "Irc bot started successfly. args:[limitCount:%1$d, intervalSec:%2$d, keyword:'%3$s']"
    .format(conf.limitCount, conf.intervalSec, conf.keyword)
  ircClient.sendNotice(bootNotice);
  println(bootNotice)
}

/**
 * Configuration of this application.
 * @param args
 */
class TwisearchIrcbotConfig(args: Array[String]) {
  private[this] val conf = new Properties
  conf.load(new FileInputStream(if (args.length < 1) "twisearch_ircbot.properties" else args(0)))

  val ircAddress = conf.getProperty("irc.address")
  val ircChannel = conf.getProperty("irc.channel")
  val ircNickname = conf.getProperty("irc.nickname")
  val ircCharset = conf.getProperty("irc.charset")

  val limitCount = conf.getProperty("limitCount").toInt
  val intervalSec = conf.getProperty("intervalSec").toInt
  val keyword = conf.getProperty("keyword")
  val messageFormat = conf.getProperty("messageFormat")
  val noticeFormat = conf.getProperty("noticeFormat")

  val consumerKey = conf.getProperty("consumerKey")
  val consumerSecret = conf.getProperty("consumerSecret")
  val accessToken = conf.getProperty("accessToken")
  val accessTokenSecret = conf.getProperty("accessTokenSecret")
}
