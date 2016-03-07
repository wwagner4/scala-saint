package net.entelijan

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import doctus.core.DoctusCanvas
import doctus.core.DoctusDraggable
import doctus.core.DoctusScheduler
import doctus.swing.DoctusCanvasSwing
import doctus.swing.DoctusComponentFactory
import doctus.swing.DoctusDraggableSwing
import doctus.swing.DoctusSchedulerSwing
import javax.swing.JFrame
import javax.swing.JPanel
import com.typesafe.config.Config

case class ServerConfig(hostName: String, port: Int)

trait SaintSwing {

  def editMode: Editmode

  def run: Unit = {

    val myConfig = ConfigFactory.parseString("akka.http.client.parsing.max-content-length=100m");
    val regularConfig = ConfigFactory.load()
    val combined = myConfig.withFallback(regularConfig)
    val complete = ConfigFactory.load(combined)

    implicit val system = ActorSystem("sys", complete)
    implicit val materializer = ActorMaterializer()

    val top = new JFrame()
    val wl = new WindowListener {
      def windowActivated(evt: WindowEvent): Unit = ()

      def windowClosed(evt: WindowEvent): Unit = system.shutdown()

      def windowClosing(evt: WindowEvent): Unit = ()

      def windowDeactivated(evt: WindowEvent): Unit = ()

      def windowDeiconified(evt: WindowEvent): Unit = ()

      def windowIconified(evt: WindowEvent): Unit = ()

      def windowOpened(evt: WindowEvent): Unit = ()

    }
    top.addWindowListener(wl)
    val panel = DoctusComponentFactory.component

    val canvas = DoctusCanvasSwing(panel)
    val sched = DoctusSchedulerSwing
    val draggable = DoctusDraggableSwing(panel)

    val cp = new JPanel
    cp.setLayout(new BorderLayout)
    cp.add(panel, BorderLayout.CENTER)

    val screenSize = Toolkit.getDefaultToolkit.getScreenSize

    top.setContentPane(cp)
    top.setTitle("Saint")
    top.setSize(new Dimension(screenSize.width, screenSize.height))
    //top.setSize(new Dimension(700, 600))
    top.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    top.setVisible(true)

    runController(editMode, canvas, sched, draggable, system, materializer)
  }

  def runController(editMode: Editmode, canvas: DoctusCanvas, sched: DoctusScheduler, draggable: DoctusDraggable,
                    system: ActorSystem, mat: Materializer): Unit

}

