package example

import scala.collection.JavaConverters._
import java.lang.annotation.{ Retention, RetentionPolicy }
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicReference
import javax.websocket.{ OnMessage, OnOpen, OnClose, Session }
import javax.websocket.server.ServerEndpoint

@Retention(RetentionPolicy.RUNTIME)
@ServerEndpoint("/myapp")
class ExampleEndpoint {
  @OnOpen
  def onOpen(session: Session): Unit = {
    ExampleEndpoint.sessions.add(session)
  }

  @OnClose
  def onClose(session: Session): Unit = {
    ExampleEndpoint.sessions.remove(session)
  }

  @OnMessage
  def onMessage(message: String, session: Session): Unit = {
    ExampleEndpoint.state.set(message)
    ExampleEndpoint.sendUpdate
  }

}

object ExampleEndpoint {
  val sessions = new CopyOnWriteArraySet[Session]()

  val state = new AtomicReference[String]("")

  def sendUpdate(): Unit = {
    val st = state.get
    sessions.asScala.foreach { ses =>
      if (ses.isOpen) {
        ses.getAsyncRemote().sendText(st)
      }
    }
  }
}
