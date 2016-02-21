package net.entelijan

import doctus.core._
import doctus.core.color._
import doctus.core.util.DoctusPoint

/**
 * Implements recording of SaintTransport elements.
 * These elements are buffered locally and sent every 200 seconds
 * somehow somewhere. Somehow Somewhere is defined by 'recordTransport'
 *
 * Reloading is not defined in that thread
 */
trait RecorderReloaderScheduling extends RecorderReloader {

  def sched: DoctusScheduler

  def recordTransport(t: SaintTransport)

  private var allRecordables = scala.collection.mutable.Map.empty[String, List[Recordable]]

  private def save(): Unit = {
    def save(id: String, recordables: List[Recordable]): List[Recordable] = {
      if (recordables.nonEmpty) {
        val (head, rest) = recordables.splitAt(30)
        // TODO Add thread safety here. Some Recordables might get lost. Not really a problem for saint.
        val recCopy = head.reverse
        val transp = SaintTransport(id, recCopy)
        recordTransport(transp)
        rest
      } else {
        recordables
      }
    }

    allRecordables.keySet.foreach { id =>
      allRecordables(id) = save(id, allRecordables(id))
    }
  }

  sched.start(save, 200)

  def record(id: String, rec: Recordable): Unit = {
    if (!allRecordables.contains(id)) {
      allRecordables(id) = List.empty[Recordable]
    }
    allRecordables(id) ::= rec
  }

}

