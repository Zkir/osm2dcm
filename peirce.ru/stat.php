<?php
#============================================
#Ежедневные сборки
#(c) Zkir 2010
#============================================	
include("ZSitePage.php");

  $zPage=new TZSitePage;
  $zPage->title="Cтатистика";
  $zPage->header="Cтатистика";

  $xml = simplexml_load_file("statistics.xml"); //Интерпретирует XML-файл в объект

  $zPage->WriteHtml( "<H1>Статистика</H1>");
  $zPage->WriteHtml( '<p>   На этой странице приведены основные статистические данные 
  	  для участвующих в конвертации регионов. </p>
   <P>И между прочим, таблица сортируется. Нужно кликнуть на заголовок столбца. Описание столбцов <a href="#descr">см. ниже</a>. </P>
  ');
  
  $zPage->WriteHtml( "<H2>Россия</H2>");
  PrintStatistics ($xml,'Россия');
  
  $zPage->WriteHtml( "<H2>Зарубежье</H2>");
  PrintStatistics ($xml,'Зарубежье');
  
  $zPage->WriteHtml('
  	  
<small> 
<p> 
<a name="descr"/>	
<b>Площадь, в тыс кв. км.</b>  - Площадь региона. Считается по poly-файлу, по которому делается обрезка, может на 20-30%% превосходить "паспортное" значение.
</p> 
<p> 
<b>Число объектов</b>  - общее число объектов в исходном osm-файле региона. Объектами считаются точки (node), линии (way) и отношения (relation)
</p> 
<p> 
<b>Правок в день</b>  - среднее количество измененных или вновь созданных объектов в день за последние 14 дней.  Все правки одного объекта считаются одной правкой.	
</p> 
<p> 
<b>14 дней, 100 дней, 365 дней</b> - доля объектов моложе 14, 100 и 365 дней соответственно. 			
</p> 
<p> 
<b> Ср. возраст</b> - средний возраст объектов, в днях. Под возрастом объекта понимается время, прошедшее момента его последнего редактирования (или создания, для новых объектов).
</p> 
<p> 
<b>Число объектов на кв. км </b>  - число объектов на единицу площади.
</p> 
<p> 
<b>Правок в день на тыс. кв. км </b>  - среднее число правок объектов за последние 14 дней, отнесенное на тысячу квадратных километров.
</p> 
<p> 
<b>Активные участники </b>  - Число участников, сделавших хотя бы одну правку за последние 14 дней в данном регионе.
</p>
<p>
  <b>Последняя правка </b>  - Дата и время последнеей попавшей в выборку правки, UTC
</p>
</small>');
  
  $zPage->WriteHtml(' <H2>См. также</H2>
  	 <ul> 	 
   	   <li>
   	     <a href="http://gis-lab.info/projects/osm-stats.html">Статистика роста OSM по объектам по регионам </a> (GIS-Lab) 
       </li>
      <li>
   	     <a href="http://stat.latlon.org/ru/">Статистика OSM-РФ от Latlon.ru </a> (за все время существования OSM) 
       </li>
      
     </ul>
   ');	  
  
 $zPage->Output("1");



function PrintStatistics($xml, $strGroup)
{
   global $zPage;
   $zPage->WriteHtml( '<table width="900px" class="sortable">
   	   
   	    <tr>
                  <td width="80px"><b>Код</b></td>
                  <td width="80px"><b>Карта</b></td>
                  <td><b>Пло-щадь,<br/> тыс кв. км. </b></td>
                  <td><b>Число объек-тов</b></td>
                  <td><b>Правок в день</b></td>
                  <td><b>14 дней</b></td>
                  <td><b>100 дней</b></td>
                  <td><b> 365 дней</b></td>
                  <td><b>Ср. воз-раст</b></td>
                  <td><b>Число объек-тов <br/> на кв. км</b></td>
                  <td><b>Правок в день <br> на тыс.кв. км</b></td>
                  <td><b>Актив-<BR/>ные участ-<BR/>ники</b></td>
                  <td><b>Послед-няя правка</b></td>
                  <td><b>Дата</b></td>
         </tr>');

  foreach ($xml->mapinfo as $item)
    {
      if( ($strGroup=='Россия' AND substr($item->MapId,0,2)=='RU') OR 
      	  ($strGroup=='Зарубежье' AND substr($item->MapId,0,2)<>'RU')   )
      {
        $zPage->WriteHtml( '<tr>');
        $zPage->WriteHtml( '<td>'.$item->MapId.'</td>');
        $zPage->WriteHtml( '<td>'.$item->MapName.'</td>');
        $zPage->WriteHtml( '<td>'.number_format($item->Square/1000,0,'.', ' ').'</td>');
        $zPage->WriteHtml( '<td>'.$item->NumberOfObjects.'</td>');
        $zPage->WriteHtml( '<td>'.$item->EditsPerDay.'</td> ');
        $zPage->WriteHtml( '<td>'.$item->M14.'%</td> ');
        $zPage->WriteHtml( '<td>'.$item->M100.'%</td> ');
        $zPage->WriteHtml( '<td>'.$item->M365.'%</td> ');
        $zPage->WriteHtml( '<td>'.number_format($item->AverageObjectAge,0,'.', ' ').'</td> ');
        $zPage->WriteHtml( '<td>'.number_format($item->ObjectsPerSquareKm,2,'.', ' ').'</td> ');
        $zPage->WriteHtml( '<td>'.number_format((((float)$item->EditsPerDayPerSquareKm)*1000.0) ,1,'.', ' ').'</td> ');
       // $zPage->WriteHtml( '<td>'..'</td> ');
        $zPage->WriteHtml( '<td>'.$item->ActiveUsers.'</td> ');
        $zPage->WriteHtml( '<td>'.$item->LastKnownEdit.'</td>');
        $zPage->WriteHtml( '<td>'.$item->MapDate.'</td>');
        $zPage->WriteHtml( '</tr>');      
      }
    }

  $zPage->WriteHtml( '</table>');
}
?>
