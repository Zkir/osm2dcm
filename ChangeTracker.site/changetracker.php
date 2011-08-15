<?PHP
  require_once("settings.php");

  header('Content-Type: text/html; charset="utf-8"');

  $MapId=$_GET["map_id"];
  if ($MapId=="")
    $MapId="RU-IVA";

  $Mode=$_GET["mode"];
  if ($Mode=="")
    $Mode=0;

  /* Подключение к серверу БД: */
  $connect=mssql_connect($g_DB_Server, $g_DB_User,$g_DB_Password)or exit("Не удалось соединиться с сервером");

  /* Выбор БД: */
  $db=mssql_select_db("[".$g_DB_Name."]", $connect) or exit("Не удалось выбрать БД");

  switch ($Mode) {
    case 0:
        $strsql=" WHERE MapID='$MapId' ";
        break;
    case 1:
        $strsql=" WHERE MapID='$MapId' and  IsNull(ApprovedUser,'')<>''";
        break;
    case 2:
        $strsql=" WHERE MapID='$MapId' and IsNull(ApprovedUser,'')=''";
        break;
   }
  $strsql="SELECT * FROM WayLog ".$strsql." order by ChangeDate desc";


  //echo $strsql;
  //setlocale(LC_ALL,"" );
  //echo "time:'".strftime("%V,%G,%Y", mktime(0, 0, 0, 12, 22, 1978)). "'\n";
  //echo "time:'". mktime(0, 0, 0, 12, 22, 1978). "'\n";

  $result=mssql_query($strsql, $connect);




  echo "<html>\n";
  echo '
  <h1>Свежие правки ('.$MapId.')</h1>
  <p><a href="'.$g_SelfUrl.'/changetracker.php?map_id='.$MapId.'&mode=0">Все</a> -
     <a href="'.$g_SelfUrl.'/changetracker.php?map_id='.$MapId.'&mode=1">Одобренные</a> -
     <a href="'.$g_SelfUrl.'/changetracker.php?map_id='.$MapId.'&mode=2">Ожидающие одобрения</a></p>
  <table padding="2">
  <tr>
    <td><b>Дата</b></td>
    <td><b>ID вея</b></td>
    <td><b>Highway</b></td>
    <td><b>Name</b></td>
    <td><b>Ref</b></td>
    <td><b>Номер версии</b></td>
    <td><b>Что случилось</b></td>
    <td><b>Кто изменил</b></td>

     <!--<td><b>Номер одобренной версии</b></td> -->
    <td><b>Кто одобрил</b></td>
    <td><b>Когда</b></td>
    <!-- bbox -->
  </tr>' ;

  while (($row = mssql_fetch_array($result)))
    {
      echo "  <tr>\n";
      echo '
       <td> '. date("d.m.Y H:i", strtotime($row['ChangeDate'])).'</td>
       <td><a href="http://www.openstreetmap.org/browse/way/'.$row['WayID'].'/history">'
      .$row['WayID'].'</a> </td>
        <td> '
        .$row['Highway'].' </td>
        <td> '.$row['Name'].'</td>
        <td> '.$row['Ref'].'</td>
        <td> '.$row['CurrentVersion'].'</td>
        <!-- <td> '.$row['ChangeDescription'].'</td> -->
        <td> '.iconv('Windows-1251', 'UTF-8', $row['ChangeDescription']).'</td>
        <td> <a href="http://www.openstreetmap.org/user/'.$row['ChangeUser'].'">'
          .$row['ChangeUser'].'</a></td>
        <!-- <td> '.$row['ApprovedVersion'].'-</td>   -->
        <td> '.$row['ApprovedUser'].'</td>';

        if ($row['ApprovedDate']<>'')
          {echo '<td> '.date("d.m.Y H:i", strtotime($row['ApprovedDate'])).'</td> ';}
        else
          {echo '<td></td>';}
      echo "  </tr>\n";
    }

  echo "</table>\n";
  echo "</html>\n";

  mssql_free_result($result);
  /*отключение от БД */
  mssql_close($connect);

function XML_node($key,$value)
{
	$t1=str_replace('&','&amp;',$value);
	$t1=str_replace('<','&lt;',$t1);
	$t1=str_replace('>','&gt;',$t1);

	$tmp="<".$key.">".trim($t1)."</".$key.">\n";
	return $tmp;
}
?>