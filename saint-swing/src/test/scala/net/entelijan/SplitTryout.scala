package net.entelijan

object SplitTryout extends App {

  val l = List(1, 2, 3, 4, 5, 6, 7)
  println(l)

  {
    val (a, b) = l.splitAt(3)
    println("split at 3")
    println(a)
    println(b)
  }
  {
    val (a, b) = l.splitAt(0)
    println("split at 0")
    println(a)
    println(b)
  }
  {
    val (a, b) = l.splitAt(-1)
    println("split at -1")
    println(a)
    println(b)
  }
  {
    val (a, b) = l.splitAt(-100)
    println("split at -100")
    println(a)
    println(b)
  }
  {
    val (a, b) = l.splitAt(6)
    println("split at 6")
    println(a)
    println(b)
  }
  {
    val (a, b) = l.splitAt(7)
    println("split at 7")
    println(a)
    println(b)
  }
  {
    val (a, b) = l.splitAt(8)
    println("split at 8")
    println(a)
    println(b)
  }
}