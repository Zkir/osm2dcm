<?php
#============================================
# Разные полезные функции
# (c) Zkir 2012
#============================================		
Function GetMapGroup($strCountryCode)
{
    switch ($strCountryCode)
    {
        case 'RU':
            $result="Россия";
            break;
        case "AZ":
        case "AM":
        case "BY":
        case "GE":
        case "KZ":
        case "KG":
        case "MD":
        case "UA":
        case "FI":
        case "LV":
        case "LT":
        case "EE": 
        case "UZ":         	
            $result="Ближнее Зарубежье";
            break;
        default:
            $result="Дальнее Зарубежье";
            break;
    }

return $result;
}
//=====================================================================================
// Описания тестов 
//=====================================================================================	
function GetDeadEndsTestDescription()
{
  $str='В этом тесте показываются тупики дорог trunk, primary и secondary. Основная идея очень простая: 
        важная дорога не может просто так (посереди поля) заканчиваться, а должна куда-то вести. Тупиковый участок, т.е. участок после последнего перекрестка,
        по определению не имеет никакого значения, кроме местного. Таким образом тупики - это ошибки присвоения статусов. <BR /> В случае же,
        если статусы расставлены правильно, данный тест позволяет обнаруживать <b>серьезные дефекты</b> дорожного графа, такие как <b>разрывы</b>
       (выпадение сегментов) <b>несоединенные вершины</b> и даже <b>нерутинговые паромы</b>.';	
  
  return $str;
}

//=====================================================================================
// Навигатор по тестам с картой 
//=====================================================================================	

function PrintTestNavigator($mapid)
{
  global $zPage;
  $zPage->WriteHtml('<table cellpadding="6px" width="100%">
  	                   <tr>
  	                     <td><b> Тест адрески,<br/> дома </b></td><td><b> Тест адрески,<br/> улицы </b></td>
  	                     <td><b> Изолированные <br /> рутинговые ребра </b></td><td><b> Тупики <br/>магистралей </b></td> 
  	                     <td><b> Дубликаты <br/>ребер </b></td>  <td><b> Просроченные <br />строящиеся дороги</b></td>
  	                   </tr>
  	                   <tr>
                         <td><a href="/qa/'.$mapid.'/addr-map">На карте</a></td>
  	                     <td>На карте</td>
  	                     <td><a href="/qa/'.$mapid.'/routing-map">На карте</a></td> 
  	                     <td><a href="/qa/'.$mapid.'/dnodes-map">На карте</a></td>
  	                     <td><a href="/qa/'.$mapid.'/rd-map">На карте</a></td>
  	                     <td><a href="/qa/'.$mapid.'/hwc-map">На карте</a></td>
  	                   </tr>
  	                   <tr>
  	                     <td><a href="/qa/'.$mapid.'#addr">Список</a></td>
  	                     <td>Список</td>
  	                     <td>Список</td>
  	                     <td><a href="/qa/'.$mapid.'#deadends">Список</a></td> 
  	                     <td><a href="/qa/'.$mapid.'#rdups">Список</a></td>
  	                     <td><a href="/qa/'.$mapid.'#hwconstr_chk">Список</a></td>
  	                   </tr>
  	                 </table>' );
  
  $zPage->WriteHtml('<p><a href="/qa/'.$mapid.'">К сводному отчету по данной карте('.$mapid.')</a><br/>');
  $zPage->WriteHtml('<a href="/qa">Назад к списку регионов</a></p>');	  
}
?>