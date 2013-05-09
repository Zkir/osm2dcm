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
  GeocoderConfig(String strCountryCode)
  {
    //Cхема адресации зависит от страны.

    levelsForCity = new String[] {};
    levelsForRegion=new String[] {};

    //Страно-специфичные правила
    if (strCountryCode.equals("EE"))
      levelsForCity=new String[] {"9"};

    if (strCountryCode.equals("AT"))
    {
      levelsForCity=new String[] {"8","6"};
      levelsForRegion=new String[] {"6"};
    }

    if (strCountryCode.equals("CZ"))
    {
      levelsForCity=new String[] {"8","7"};
      levelsForRegion=new String[] {"6"};
    }

    if (strCountryCode.equals("ES"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }

    //Португалия
    if (strCountryCode.equals("PT"))
    {
      levelsForCity=new String[] {"8","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"6"};
    }

    if (strCountryCode.equals("NL"))
    {
      levelsForCity=new String[] {"10","8"};
      levelsForRegion=new String[] {"4"};
    }

    if (strCountryCode.equals("SE"))
    {
      levelsForCity=new String[] {"7"};
      levelsForRegion=new String[] {"4"};
    }

    if (strCountryCode.equals("GR"))
    {
      levelsForCity=new String[] {"10","8"};
      levelsForRegion=new String[] {"6"};
    }

    if (strCountryCode.equals("CY"))
    {
      levelsForCity=new String[] {"8","7"};
      levelsForRegion=new String[] {"6"};
    }

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

    if (strCountryCode.equals("SI"))
      levelsForCity=new String[] {"8"};

    if (strCountryCode.equals("HU"))
    {
      levelsForCity=new String[] {"8", "7"};
      levelsForRegion=new String[] {"6"};
    }

    //Румыния
    if (strCountryCode.equals("RO"))
    {
      levelsForCity=new String[] {"6","4","NEAREST_CITY_POINT"};
      levelsForRegion=new String[] {"5","4"};
    }

    if (strCountryCode.equals("LT"))
      levelsForCity=new String[] {"8"};

    if (strCountryCode.equals("IT"))
    {
      levelsForCity=new String[] {"8"};
      levelsForRegion=new String[] {"6"};
    }

    if (strCountryCode.equals("GB"))
      levelsForCity=new String[] {"8"};

    //Болгария
    if (strCountryCode.equals("BG"))
      levelsForCity=new String[] {"10","8"};

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
      levelsForCity=new String[] {"8","7"};

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

  }

}
