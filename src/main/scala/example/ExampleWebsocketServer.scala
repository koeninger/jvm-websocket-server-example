package example

import scala.collection.JavaConverters._
import java.{ util => ju }
import java.util.concurrent.atomic.{ AtomicLong, AtomicReference }
import java.util.concurrent.ConcurrentHashMap

import io.undertow.Undertow
import io.undertow.util.Headers
import io.undertow.server.{ HttpHandler, HttpServerExchange }
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.websockets.core.AbstractReceiveListener
import io.undertow.websockets.core.BufferedTextMessage
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.spi.WebSocketHttpExchange

import io.undertow.Handlers.path
import io.undertow.Handlers.websocket

object ExampleWebsocketServer {
  val metrics = new AtomicLong(0)

  val peers = new AtomicReference[ju.Set[WebSocketChannel]](new ju.HashSet[WebSocketChannel]())

  def main(args: Array[String]): Unit = {
    val server: Undertow = Undertow.builder()
      .addHttpListener(8080, "localhost")
      .setHandler(path()
        .addPrefixPath("/myapp", websocket(new WebSocketConnectionCallback() {
          override def onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel): Unit = {
            if (peers.get.isEmpty) {
              peers.set(exchange.getPeerConnections)
            }
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
              override def onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage): Unit = {
              }
            })
            channel.resumeReceives()
          }
        }))
        .addPrefixPath("/update", new HttpHandler() {
          override def handleRequest(exchange: HttpServerExchange): Unit = {
            val data = s"""[{"id": 23, "metrics": ${metrics.incrementAndGet}}, {"id": 66, "metrics":0}]"""
            peers.get.asScala.foreach { peer =>
              WebSockets.sendText(data, peer, null)
            }
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("updating");
          }
        }))
      .build()
    server.start()
  }

}
