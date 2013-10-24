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
  String[] redundantWords;
  String language;
  boolean blnHamletsExcluded;
  boolean blnPerformTransliteration;

  static final String CITY_POLYGON="CITY_POLYGON";
  static final String NEAREST_CITY_POINT= "NEAREST_CITY_POINT";
  static final String ADMIN_LEVEL_2="2";
  static final String ADMIN_LEVEL_4="4";
  static final String ADMIN_LEVEL_5="5";
  static final String ADMIN_LEVEL_6="6";
  static final String ADMIN_LEVEL_7="7";
  static final String ADMIN_LEVEL_8="8";
  static final String ADMIN_LEVEL_9="9";
  static final String ADMIN_LEVEL_10="10";

  GeocoderConfig(String strCountryCode)
  {
    //Cхема адресации зависит от страны.

    levelsForCity = new String[] {};
    levelsForRegion=new String[] {};
    redundantWords=new String[] {};
    language="";
    blnHamletsExcluded=false;
    blnPerformTransliteration=true;

    //Страно-специфичные правила
    //===========================================================================================
    //Европа
    //===========================================================================================
    //Андорра
    if (strCountryCode.equals("AD"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_2};
    }

    //Албания
    if (strCountryCode.equals("AL"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }

    //Австрия
    if (strCountryCode.equals("AT"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_6};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String [] {"Bezirk","Kreis","Gemeinde","(Stadt)"};
    }

    //Бельгия
    if (strCountryCode.equals("BE"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6,ADMIN_LEVEL_4};
    }

    //Чехия
    if (strCountryCode.equals("CZ"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }
    //Германия
    if (strCountryCode.equals("DE"))
    {
      levelsForCity=new String[] {CITY_POLYGON,ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String [] {"Bezirk","Kreis"};
    }

    //Эстония
    if (strCountryCode.equals("EE"))
      levelsForCity=new String[] {ADMIN_LEVEL_9};

    //Испания
    if (strCountryCode.equals("ES"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }

    //Греция
    if (strCountryCode.equals("GR"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_7,ADMIN_LEVEL_8,ADMIN_LEVEL_10};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      redundantWords=new String [] {"Demos","Periphereia","Kentorikes"};
    }

    //Ирландия
    if (strCountryCode.equals("IE"))
    {
      levelsForCity=new String[] {CITY_POLYGON,ADMIN_LEVEL_8,ADMIN_LEVEL_10,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String [] {"County"};
    }

    //Исландия
    if (strCountryCode.equals("IS"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_6,ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Хорватия
    if (strCountryCode.equals("HR"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_7,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }
    //Лихтенштейн
    if (strCountryCode.equals("LI"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6,ADMIN_LEVEL_2};
    }

    //Монако
    if (strCountryCode.equals("MC"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_2};
    }

    //Голландия
    if (strCountryCode.equals("NL"))
    {
        levelsForCity=new String[] {ADMIN_LEVEL_10,ADMIN_LEVEL_8};
        levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Португалия
    if (strCountryCode.equals("PT"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }

    //Швеция
    if (strCountryCode.equals("SE"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      redundantWords =new String[]{"Kommune","lan"};
    }

    //Сан-Марино
    if (strCountryCode.equals("SM"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_2};
    }



    //Турция
    if (strCountryCode.equals("TR"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_6};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Кипр
    if (strCountryCode.equals("CY"))
    {
      levelsForCity=new String[] {CITY_POLYGON,ADMIN_LEVEL_8,ADMIN_LEVEL_7,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_6,ADMIN_LEVEL_2};
    }

    //Польша
    if (strCountryCode.equals("PL"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_10, ADMIN_LEVEL_7, ADMIN_LEVEL_6};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String []{"gmina","powiat"};
    }

    if (strCountryCode.equals("SK"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_6,ADMIN_LEVEL_9};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Словения
    if (strCountryCode.equals("SI"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_5};
    }

    //Венгрия
    if (strCountryCode.equals("HU"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8, ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String []{"megye"};
    }

    //Румыния
    if (strCountryCode.equals("RO"))
    {
      levelsForCity=new String[] {CITY_POLYGON,ADMIN_LEVEL_6,ADMIN_LEVEL_4,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_5,ADMIN_LEVEL_4};
      redundantWords=new String []{"Municipiul"};
    }

    if (strCountryCode.equals("LT"))
      levelsForCity=new String[] {ADMIN_LEVEL_8};

    //Италия
    if (strCountryCode.equals("IT"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }

    //Великобритания. Как-то не понятно.
    //Admin_level=6 это графства, но кое где они и города
    //Place используется в северной ирландии
    if (strCountryCode.equals("GB"))
    {
      levelsForCity=new String[] {CITY_POLYGON, ADMIN_LEVEL_8,ADMIN_LEVEL_10,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_6,ADMIN_LEVEL_5,ADMIN_LEVEL_4};
    }

    //Болгария
    if (strCountryCode.equals("BG"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_10,ADMIN_LEVEL_8,ADMIN_LEVEL_6}; //Проблема - София единственная имеет admin_level=6
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String [] {"Област","Община","Град"};
      blnPerformTransliteration=false;
    }

    //Сербия
    if (strCountryCode.equals("RS"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
      redundantWords=new String [] {"Komuna","Opshtina","Grad" };
    }

    //Македония
    if (strCountryCode.equals("MK"))
    {
      levelsForCity=new String[] {CITY_POLYGON,ADMIN_LEVEL_8,ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      redundantWords=new String []{"Opshtina"};
    }

    //Босния и герцеговина
    if (strCountryCode.equals("BA"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_7,ADMIN_LEVEL_6};
      levelsForRegion=new String[] {ADMIN_LEVEL_5};
      redundantWords=new String [] {"Opstina","Obchina"};

    }
    //Норвегия
    if (strCountryCode.equals("NO"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_4,ADMIN_LEVEL_6};
    }

    //Дания
    if (strCountryCode.equals("DK"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_7};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      redundantWords =new String[]{"Kommune","Region"};
    }

    //Швейцария
    if (strCountryCode.equals("CH"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Франция
    if (strCountryCode.equals("FR"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }

    //Мальта
    if (strCountryCode.equals("MT"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Туркмения
    if (strCountryCode.equals("TM"))
    {
      levelsForCity=new String[] {CITY_POLYGON,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Израиль
    if (strCountryCode.equals("IL"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      language="en";
    }

    //===========================================================================================
    //Америка
    //===========================================================================================
    //США
    if (strCountryCode.equals("US"))
      levelsForCity=new String[] {ADMIN_LEVEL_8,ADMIN_LEVEL_6};

    //Чили
    if (strCountryCode.equals("CL"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }
    //Venezuela
    if (strCountryCode.equals("VE"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      blnHamletsExcluded=true;
    }
    //Парагвай
    if (strCountryCode.equals("PY"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Фолклендские о-ва
    if (strCountryCode.equals("FK"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Куба
    if (strCountryCode.equals("CU"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      //levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Панама
    if (strCountryCode.equals("PA"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }
    //Белиз
    if (strCountryCode.equals("BZ"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Коста-Рика CR
    if (strCountryCode.equals("CR"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Гватемала - GT
    if (strCountryCode.equals("GT"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }
    //Гондурас -- HN
    if (strCountryCode.equals("HN"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }
    //Никарагуа NI
    if (strCountryCode.equals("NI"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Сальвадор - SV
    if (strCountryCode.equals("SV"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Ямайка - JM
    if (strCountryCode.equals("JM"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }

    //Мексика - MX
    if (strCountryCode.equals("MX"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Бразилия BR
    if (strCountryCode.equals("BR"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //===========================================================================================
    //Азия
    //===========================================================================================
    //Египет
    if (strCountryCode.equals("EG"))
    {
      language="en";
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      blnHamletsExcluded=true;
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
      redundantWords= new String[]{"Governorate"};

    }
    //Тунис
    if (strCountryCode.equals("TN"))
    {
      language="en";
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      blnHamletsExcluded=true;
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Арабские эмираты
    if (strCountryCode.equals("AE"))
    {
      language="en";
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Шри-Ланка
    if (strCountryCode.equals("LK"))
    {
      levelsForCity=new String[] {ADMIN_LEVEL_8,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Таиланд
    if (strCountryCode.equals("TH"))
    {
      language="en";
      levelsForCity=new String[] {NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Сингапур
    if (strCountryCode.equals("SG"))
    {
      //Временный хак, чтобы глаза не мозолило
      levelsForCity=new String[] {ADMIN_LEVEL_2};
      levelsForRegion=new String[] {ADMIN_LEVEL_2};
    }

    //Гонконг
    if (strCountryCode.equals("HK"))
    {
      //Для начала
      language="en";
      levelsForCity=new String[] {ADMIN_LEVEL_8};
      levelsForRegion=new String[] {ADMIN_LEVEL_6};
    }
    //Вьетнам
    if (strCountryCode.equals("VN"))
    {
      levelsForCity=new String[] {CITY_POLYGON,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }
    //===========================================================================================
    //Австралия/Океания
    //===========================================================================================
    //Австралия
    if (strCountryCode.equals("AU"))
    {
      levelsForCity=new String[] {CITY_POLYGON,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }

    //Новая зеландия
    if (strCountryCode.equals("NZ"))
    {
      levelsForCity=new String[] {CITY_POLYGON,NEAREST_CITY_POINT};
      levelsForRegion=new String[] {ADMIN_LEVEL_4};
    }
  }

}
