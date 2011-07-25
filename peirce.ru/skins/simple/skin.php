<?php
#==================================================
# Шаблон страницы для z-site :)
# (с) Zkir, 2008
#==================================================

# Используется класс TZSitePage
# В нем должны быть объявлены переменные:
# $title -   Заголовок страницы;
# $header -  Заголовок  (H1);
# $content - содержимое ;

  if(!isset($this))
    {
      die ("Содержимое страницы не определено");
    }
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>
      <?php
        echo $this->title;
      ?>
     </title>
    <meta http-equiv="Content-Type"   content="text/html; charset=UTF-8">
    <script src="http://peirce.gis-lab.info/sorttable.js"> </script>
  </head>

  </head>
  <body>
  <img src="cglogo.gif"  />

<?php
  echo $this->content;
?>
<p>
<small>
&copy; Карты — участники проекта <a href="http://openstreetmap.org">OpenStreetMap</a>,
 по лицензии <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>.<br />
</small>
</p>
<HR/>
<center>
<a href="http://gis-lab.info" target=_blank>
<img src="http://gis-lab.info/images/gis-lab-button.gif" border="0" width="88" height="31" alt="GIS-Lab.info"></a>
</center>
<!-- Yandex.Metrika -->
<script src="//mc.yandex.ru/metrika/watch.js" type="text/javascript"></script>
<div style="display:none;"><script type="text/javascript">
try { var yaCounter1224821 = new Ya.Metrika(1224821); } catch(e){}
</script></div>
<noscript><div style="position:absolute"><img src="//mc.yandex.ru/watch/1224821" alt="" /></div></noscript>
<!-- /Yandex.Metrika -->

</body>
</HTML>

