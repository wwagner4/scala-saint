package net.entelijan

import doctus.core._
import doctus.core.color._
import doctus.core.util.DoctusPoint
import scala.concurrent.Future

trait RecorderReloaderBufferingImpl extends RecorderReloaderBasic with RecorderReloaderBuffering {

  def sched: DoctusScheduler

  def recordTransport(t: SaintTransport)

  def record(id: String, rec: Recordable): Unit = {
    if (!allRecordables.contains(id)) {
      allRecordables(id) = List.empty[Recordable]
    }
    allRecordables(id) ::= rec
  }

  sched.start(recordBuffered, 200)
  
  private var allRecordables = scala.collection.mutable.Map.empty[String, List[Recordable]]

  private def recordBuffered(): Unit = {
    def recordBuffered(id: String, recordables: List[Recordable]): List[Recordable] = {
      if (recordables.nonEmpty) {
        val (head, rest) = recordables.splitAt(30)
        // TODO Not threadsave. Better would be a separate implementation for each platform.
        // swing could use actors.
        val recCopy = head.reverse
        val transp = SaintTransport(id, recCopy)
        recordTransport(transp)
        rest
      } else {
        recordables
      }
    }

    allRecordables.keySet.foreach { id =>
      allRecordables(id) = recordBuffered(id, allRecordables(id))
    }
  }

}

