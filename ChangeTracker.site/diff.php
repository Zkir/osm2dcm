<?PHP
  require_once("settings.php");

  header('Content-Type: text/html; charset="utf-8"');
  
  $way_id=$_GET["id"];
  $nv1=$_GET["v1"];
  $nv2=$_GET["v2"];
  
  if ($nv2==0)
  	  $nv2=$nv1+1;
  	  
  if ($nv1!='')
    $v1 = simplexml_load_file("http://www.openstreetmap.org/api/0.6/way/$way_id/$nv1"); 
  
  if ($nv2!='')
    $v2 = simplexml_load_file("http://www.openstreetmap.org/api/0.6/way/$way_id/$nv2");
  
  echo "<H1>Линия: $way_id</H1>";
  
  echo "<p><b>Изменено:</b> ".$v2->way['timestamp']."<br/> ";
  echo "<b>Пользователь:</b> ".$v2->way['user']."<br/>";
  echo "<b>Версия:</b> ".$v2->way['version']."<br/>";
  echo "<b>В пакете правок:</b> ".$v2->way['changeset']."</p>";

  
  echo '<table CELLSPACING="5">';
  echo "<tr>" ;
    echo "<td></td>";
    echo "<td><b> Версия $nv1 <br/> (было) </b></td>";
    echo "<td><b> Версия $nv2 <br/> (стало) </b></td>";
  echo "</tr>" ;
  
  echo "<tr>";
  echo '<td valign="top"><b>Теги:<b></td>';
   echo '<td valign="top">';
  if ($nv1!='')
  foreach ($v1->way->tag as $item)
    {
    	 echo $item['k'].'='.$item['v']."</br>";
    }
  echo "</td>";
  echo '<td valign="top">';
  foreach ($v2->way->tag as $item)
    {
    	 echo $item['k'].'='.$item['v']."</br>";
    }
  echo "</td>"; 
  echo "</tr>";
  echo "<tr>";
  echo '<td valign="top"><b>Вершины:</b></td>';  	  
  echo "</tr>";
  echo "<tr>";
  echo '<td valign="top"></td>';  	  
  echo '<td valign="top">';
  if ($nv1!='')
  foreach ($v1->way->nd as $item)
    {
    	 echo $item['ref']."</br>";
    }
  echo "</td>";
  echo "<td>";
  
  foreach ($v2->way->nd as $item)
    {
    	 echo $item['ref']."</br>";
    }
  echo "</td>";
  echo "</tr>" ;  
  echo "</table>";

?>
