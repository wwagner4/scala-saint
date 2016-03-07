package net.entelijan

case object OverviewPage {

  def render(renderer: ImageRenderer): String = {
    val ids = renderer.ids
    val rows = ids.grouped(4)

    val rowStr = rows.map { row =>
      val colStr = row.map { id =>
        colTemplate(id)
      }.mkString("\n")
      rowTemplate(colStr)
    }.mkString("\n")
    pageTemplate(rowStr)
  }

  def pageTemplate(rowsStr: String): String = s"""
<html>
<head>
<title>Saint Overview</title>
<style type="text/css">
body {
  background-color: #eafaff;
  font-family: monospace;
}
p {
    margin-top: 0px;
    margin-bottom: 3px;
    margin-right: 0px;
    margin-left: 0px;  
    padding: 0px;
}
.heading {
    margin-top: 20px;
    margin-bottom: 3px;
    margin-right: 0px;
    margin-left: 0px;  
    padding: 0px;
}
a {
}
table {
  border-collapse: collapse;
  margin: 0px;
  padding: 0px;
}
tr {
  height: 30px;
}
td {
  padding: 5px;
}
.txt {
  margin-left: 6px;
  margin-bottom: 30px;
  margin-top: 60px;
}
</style>
</head>
<body>
<div class="txt">
<p><b>saint</b>, yet another drawing program. <a href="http://entelijan.net/art/saint/index.html">home</a></p>
<p>create your own images using the <a target="_blank" href="editnew">saint graphic editor</a> on your computer or portable device</p>
<p>download the image of your choise in the size you like (S, M, L, XL)</p>
<p>feel free to edit any of the existing images</p>
</div>
<table>
$rowsStr
</table>
</body>
</html>    
"""

  def colTemplate(id: String) = s"""
<td>
<p>
<img src="images/S/$id"/>
</p>
<p>
$id
<a href="images/S/$id">S</a>
<a href="images/M/$id">M</a>
<a href="images/L/$id">L</a>
<a href="images/XL/$id">XL</a>
<a target="_blank" href="editexisting/$id">edit</a>
</p>
</td>
"""

  def rowTemplate(colStr: String) = s"""
<tr>
$colStr
<tr>
"""
}