package ru.zkir.mp2mp.taskgeocoder;

/**
 * Created with IntelliJ IDEA.
 * User: Zkir
 * Date: 15.01.13
 * Time: 21:03
 * To change this template use File | Settings | File Templates.
 */

public class GeocoderConfig{
  String[] levelsForCity;
  String[] levelsForRegion;
  String language;
  boolean blnHamletsExcluded;
  GeocoderConfig(String strCountryCode)
  {
    //Cхема адресации зависит от страны.

    levelsForCity = new String[] {};
    levelsForRegion=new String[] {};
    language="";
    blnHamletsExcluded=false;

    //Страно-специфичные правила
    //===========================================================================================
    //Европа
    //===========================================================================================
    //Андорра
    if (strCountryCode.equals("AD"))
    {
      levelsForCity=new String[] {"7"};
      levelsForRegion=new String[] {"2"};
    }

    //Албания
    if (strCountryCode.equals("AL"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }

    //Австрия
    if (strCountryCode.equals("AT"))
    {
      levelsForCity=new String[] {"8","6"};
      levelsForRegion=new String[] {"6"};
    }

    //Бельгия
    if (strCountryCode.equals("BE"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6","4"};
    }

    //Чехия
    if (strCountryCode.equals("CZ"))
    {
      levelsForCity=new String[] {"8","7"};
      levelsForRegion=new String[] {"6"};
    }

    //Эстония
    if (strCountryCode.equals("EE"))
      levelsForCity=new String[] {"9"};

    //Испания
    if (strCountryCode.equals("ES"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }

    //Ирландия
    if (strCountryCode.equals("IE"))
    {
      levelsForCity=new String[] {"7","8","10","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6"};
    }

    //Исландия
    if (strCountryCode.equals("IS"))
    {
      levelsForCity=new String[] {"6","8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Хорватия
    if (strCountryCode.equals("HR"))
    {
      levelsForCity=new String[] {"8","7","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6"};
    }
    //Лихтенштейн
    if (strCountryCode.equals("LI"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6","2"};
    }

    //Монако
    if (strCountryCode.equals("MC"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"2"};
    }

    //Голландия
    if (strCountryCode.equals("NL"))
    {
        levelsForCity=new String[] {"10","8"};
        levelsForRegion=new String[] {"4"};
    }

    //Португалия
    if (strCountryCode.equals("PT"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6"};
    }

    //Швеция
    if (strCountryCode.equals("SE"))
    {
      levelsForCity=new String[] {"7"};
      levelsForRegion=new String[] {"4"};
    }

    //Сан-Марино
    if (strCountryCode.equals("SM"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"2"};
    }

    //Греция
    if (strCountryCode.equals("GR"))
    {
      levelsForCity=new String[] {"7","8","10"};
      levelsForRegion=new String[] {"4"};
    }

    //Турция
    if (strCountryCode.equals("TR"))
    {
      levelsForCity=new String[] {"6"};
      levelsForRegion=new String[] {"4"};
    }

    //Кипр
    if (strCountryCode.equals("CY"))
    {
      levelsForCity=new String[] {"CITY_POLYGON","8","7","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6","2"};
    }

    //Польша
    if (strCountryCode.equals("PL"))
    {
      levelsForCity=new String[] {"8","10", "7", "6"};
      levelsForRegion=new String[] {"6"};
    }

    if (strCountryCode.equals("SK"))
    {
      levelsForCity=new String[] {"9"};
      levelsForRegion=new String[] {"4"};
    }

    //Словения
    if (strCountryCode.equals("SI"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"5"};
    }

    //Венгрия
    if (strCountryCode.equals("HU"))
    {
      levelsForCity=new String[] {"8", "7"};
      levelsForRegion=new String[] {"6"};
    }

    //Румыния
    if (strCountryCode.equals("RO"))
    {
      levelsForCity=new String[] {"CITY_POLYGON","6","4","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"5","4"};
    }

    if (strCountryCode.equals("LT"))
      levelsForCity=new String[] {"8"};

    if (strCountryCode.equals("IT"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }

    //Великобритания. Как-то не понятно.
    if (strCountryCode.equals("GB"))
    {
      levelsForCity=new String[] {"8","10","6"};
      levelsForRegion=new String[] {"5","4"};
    }

    //Болгария
    if (strCountryCode.equals("BG"))
    {
      levelsForCity=new String[] {"10","8","6"}; //Проблема - София единственная имеет admin_level=6
      levelsForRegion=new String[] {"6"};
    }

    //Сербия
    if (strCountryCode.equals("RS"))
    {
      levelsForCity=new String[] {"8","7"};
      levelsForRegion=new String[] {"6"};
    }

    //Македония
    if (strCountryCode.equals("MK"))
      levelsForCity=new String[] {"8","7"};

    //Босния и герцеговина
    if (strCountryCode.equals("BA"))
    {
      levelsForCity=new String[] {"8","7","6"};
      levelsForRegion=new String[] {"5"};
    }
    //Норвегия
    if (strCountryCode.equals("NO"))
    {
      levelsForCity=new String[] {"7"};
      levelsForRegion=new String[] {"4","6"};
    }

    //Дания
    if (strCountryCode.equals("DK"))
    {
      levelsForCity=new String[] {"8","7"};
      levelsForRegion=new String[] {"4"};
    }

    //Швейцария
    if (strCountryCode.equals("CH"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"4"};
    }

    //Франция
    if (strCountryCode.equals("FR"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }

    //Мальта
    if (strCountryCode.equals("MT"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //===========================================================================================
    //Америка
    //===========================================================================================
    //США
    if (strCountryCode.equals("US"))
      levelsForCity=new String[] {"8","6"};

    //Чили
    if (strCountryCode.equals("CL"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }
    //Venezuela
    if (strCountryCode.equals("VE"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6"};
    }
    //Парагвай
    if (strCountryCode.equals("PY"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Фолклендские о-ва
    if (strCountryCode.equals("FK"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Куба
    if (strCountryCode.equals("CU"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      //levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"4"};
    }

    //Панама
    if (strCountryCode.equals("PA"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }
    //Белиз
    if (strCountryCode.equals("BZ"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Коста-Рика CR
    if (strCountryCode.equals("CR"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Гватемала - GT
    if (strCountryCode.equals("GT"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }
    //Гондурас -- HN
    if (strCountryCode.equals("HN"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }
    //Никарагуа NI
    if (strCountryCode.equals("NI"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Сальвадор - SV
    if (strCountryCode.equals("SV"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //Ямайка - JM
    if (strCountryCode.equals("JM"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6"};
    }

    //Мексика - MX
    if (strCountryCode.equals("MX"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"4"};
    }

    //===========================================================================================
    //Азия
    //===========================================================================================
    //Египет
    if (strCountryCode.equals("EG"))
    {
      language="en";
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      blnHamletsExcluded=true;
      levelsForRegion=new String[] {"4"};
    }
  }

}
