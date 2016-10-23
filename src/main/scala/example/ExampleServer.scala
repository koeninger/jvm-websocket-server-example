package example


import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.DefaultByteBufferPool
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.ClassPathResourceManager
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager
import io.undertow.servlet.api.ServletContainer
import io.undertow.websockets.jsr.WebSocketDeploymentInfo

object ExampleServer {
  def main(args: Array[String]): Unit = {
    val path: PathHandler = Handlers.path()

    val server: Undertow = Undertow.builder()
      .addHttpListener(8080, "localhost")
      .setHandler(path)
      .build()
    server.start()

    val container: ServletContainer = ServletContainer.Factory.newInstance()

    val builder: DeploymentInfo = new DeploymentInfo()
      .setClassLoader(this.getClass.getClassLoader())
      .setContextPath("/")
      .setResourceManager(new ClassPathResourceManager(this.getClass.getClassLoader(), this.getClass.getPackage()))
      .addServletContextAttribute(
      WebSocketDeploymentInfo.ATTRIBUTE_NAME,
        new WebSocketDeploymentInfo()
          .setBuffers(new DefaultByteBufferPool(true, 100))
          .addEndpoint(ExampleEndpoint.getClass)
    )
      .setDeploymentName("example.war")

    val manager: DeploymentManager = container.addDeployment(builder)
    manager.deploy()
    path.addPrefixPath("/", manager.start())
  }
}
