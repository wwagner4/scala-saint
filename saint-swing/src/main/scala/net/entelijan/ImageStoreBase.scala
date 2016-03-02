package net.entelijan

import java.io.File

trait ImageStoreBase {
  
  def dir: File

  protected def getTxtFile(id: String): Option[File] = {
    val re = txtFile(id)
    if (!re.exists()) None else Some(re)
  }
  
  protected def txtFile(id: String): File = {
    new File(dir, txtFileName(id))
  }

  private def txtFileName(id: String) = "saint_%s.txt" format id


}