import akka.actor.{ActorLogging, Actor}
import twitter4j.Twitter
import twitter4j.Query
import scala.collection.JavaConversions._
import twitter4j.Status
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Message object of searching.
 * @param keyword
 */
case class SearchTwitter(keyword: String)

/**
 * Actor of Searcher.
 * @param client
 * @param limitCount
 * @param intervalSec
 * @param messageFormat
 * @param noticeFormat
 * @param twitter
 */
class TwitterSearcher(
  client: Client,
  limitCount: Int,
  intervalSec: Int,
  messageFormat: String,
  noticeFormat: String,
  twitter: Twitter
  ) extends Actor with ActorLogging {
  val formatter = new SimpleDateFormat("(MM/dd HH:mm)", Locale.JAPAN)
  var maxId = 0L

  override def receive: Receive = {
    case SearchTwitter(keyword) =>
      log.info(s"Twitter searching. keyword:${keyword}")
      val query = new Query
      query.setQuery(keyword)
      query.setSinceId(maxId)
      query.setResultType(Query.RECENT);
      val tweets = twitter.search(query).getTweets().reverse
      if (maxId != 0L) tweets.filter(t => t.getId() > maxId).foreach(sendNoticeToIRC)
      val count = tweets.filter(t => t.getId() > maxId).size
      if (count > limitCount && maxId != 0L) sendMessageToIRC(count, keyword)
      tweets.foreach(t => {
        if (t.getId() > maxId) maxId = t.getId()
      })
    case m =>
      log.error(s"Not supported command. ${m.toString}")
  }

  private def sendNoticeToIRC(t: Status) {
    val notice = noticeFormat.format(
      t.getUser().getScreenName(),
      t.getText(),
      formatter.format(t.getCreatedAt()))
    client.sendNotice(notice)
  }

  private def sendMessageToIRC(count: Int, keyword: String) {
    val message = messageFormat.format(intervalSec.toString, keyword, count.toString);
    client.sendMessage(message)
  }
}