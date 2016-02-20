package net.entelijan

object EditorPage {

  def render(mode: Editmode): String = {
    s"""<!DOCTYPE html>
<html>
  <head lang="en">
    <meta charset="UTF-8">
    <title>Saint Editor</title>
    <style type="text/css">
body  {
  margin: 0px;
  overflow: hidden;
}
    </style>
  </head>
  <body>
    <canvas id="canvas" style="background-color: #EEEEEE; ">HTML5 canvas not supported in your browser :-(</canvas>
    <script  type="text/javascript" >
              function resize() {
          var canv = document.getElementById("canvas") 
                  canv.width = window.innerWidth;
                  canv.height = window.innerHeight;
              }
              resize();
              window.onresize = function() {resize();}; 
    </script>
    <script type="text/javascript" src="/js/saint-scalajs-opt.js"></script>
    <script type='text/javascript'>SaintScalaJs().main('${upickle.default.write(mode)}');</script>
  </body>
</html>
"""
  }

}