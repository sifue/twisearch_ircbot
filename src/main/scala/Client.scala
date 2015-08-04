import com.sorcix.sirc.IrcAdaptor
import com.sorcix.sirc.IrcConnection
import com.sorcix.sirc.User
import com.sorcix.sirc.Channel
import java.nio.charset.Charset

import scala.annotation.tailrec

class Client(address: String, channel: String, nickname: String, charset: String) extends IrcAdaptor {
  val irc = new IrcConnection
  irc.setServerAddress(address)
  irc.setCharset(Charset.forName(charset))
  irc.setNick(nickname)
  irc.addServerListener(this)
  irc.addMessageListener(this)
  connectSafely

  override def onMessage(irc: IrcConnection, sender: User, target: Channel, message: String) = {
    if (message.contains("ping " + nickname) || message.contains("PING " + nickname)) {
      sendNotice("Working now. > " + sender.getNick())
    }
  }

  override def onConnect(irc: IrcConnection) = {
    irc.createChannel(channel).join()
  }

  override def onDisconnect(irc: IrcConnection) = {
    val waitMsec = 60000
    Console.println(s"Disconnected. After ${waitMsec} milliseconds, try to reconnect. ")
    reconnect(waitMsec)
  }

  def sendMessage(message: String) = {
    connectSafely
    irc.createChannel(channel).send(message)
  }
  
  def sendNotice(notice: String) = {
    connectSafely
    irc.createChannel(channel).sendNotice(notice)
  }

  private def connectSafely: Unit = {
    if (!irc.isConnected) irc.connect()
  }

  @tailrec
  private def reconnect(waitMsec: Long): Unit = {
    if (!irc.isConnected) {
      Thread.sleep(waitMsec)
      connectSafely
      reconnect(waitMsec)
    }
  }
}