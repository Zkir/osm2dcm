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
?>