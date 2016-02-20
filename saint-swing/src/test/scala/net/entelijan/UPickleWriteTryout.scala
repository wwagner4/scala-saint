package net.entelijan

object UPickleWriteTryout extends App {
  println("-start-")
  val recs: List[Recordable] = List(REC_ColorBlack, REC_Brightness(22))
  val str = upickle.default.write(recs)
  println(str)
  val recs1 = upickle.default.read[List[Recordable]](str)
  println(recs1.mkString("\n"))
  println("-end-")
}
