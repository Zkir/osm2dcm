<?php
    // Задаем текущий язык проекта
    echo putenv("LANG=zz_ZZ") . "<BR/>\n";

    // Задаем текущую локаль (кодировку)
      echo setlocale(LC_MESSAGES, "zz_ZZ.UTF-8") . "<BR/>\n";
    //echo setlocale(LC_ALL, 'ru_RU.UTF-8') . "<BR/>\n";

    // Указываем имя домена
    $domain = 'default.1';

    // Задаем каталог домена, где содержатся переводы
    echo bindtextdomain ($domain, "./locale") . "<BR/>\n";

    // Выбираем домен для работы

    echo textdomain ($domain) . "<BR/>\n";

    // Если необходимо, принудительно указываем кодировку
    // (эта строка не обязательна, она нужна,
    // если вы хотите выводить текст в отличной
    // от текущей локали кодировке).
    echo bind_textdomain_codeset($domain, 'UTF-8'). "<BR/>\n";;
    
    
    echo _('Контроль качества'); 
?> 