package net.entelijan

import java.io.File

object FileUtil {

  def dir(pathFromHome: List[String]): File = {
    val dirPath = (System.getProperty("user.home") :: pathFromHome).mkString(File.separator)
    val dir = new File(dirPath)
    if (!dir.exists()) {
      val mkdirOk = dir.mkdirs()
      if (!mkdirOk) throw new IllegalStateException("Could not create output directory '%s'" format dirPath)
    }
    dir
  }

}