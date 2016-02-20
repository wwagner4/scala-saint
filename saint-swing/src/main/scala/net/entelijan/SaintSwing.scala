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

case class ServerConfig(hostName: String, port: Int)

object SaintSwingConfig {

  private lazy val conf = ConfigFactory.load()

  def editMode: Editmode = {
    try {
      val id = conf.getString("id")
      val NUM = "(\\d*)".r
      id match {
        case NUM(n) => EM_Existing(n)
        case _      => EM_New
      }
    } catch {
      case ex: com.typesafe.config.ConfigException => EM_New
    }
  }

  def serverConfig: ServerConfig = {
    val host = conf.getString("host")
    val port = conf.getInt("port")
    ServerConfig(host, port)
  }

  def workdir: File = {
    val workdir = conf.getString("workdir")
    val dir = new File(workdir)
    require(dir.exists())
    dir
  }

}

trait SaintSwing {

  def run: Unit = {
    val editMode = SaintSwingConfig.editMode
    println("editMode: " + editMode)

    val system = ActorSystem()

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
    //  top.setSize(new Dimension(screenSize.width, screenSize.height))
    top.setSize(new Dimension(700, 600))
    top.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    top.setVisible(true)

    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val sa = SaintAffine(0, 0, 1)

    runController(editMode, canvas, sched, draggable, sa, system)
  }

  def runController(editMode: Editmode, canvas: DoctusCanvas, sched: DoctusScheduler, draggable: DoctusDraggable, sa: SaintAffine,
                    system: ActorSystem)(implicit mat: Materializer): Unit

}

