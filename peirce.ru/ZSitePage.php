<?php
#============================================
#Класс формирования страницы -
# Для z-Site
#(c) Zkir 2008
#============================================
require_once("settings.php");

# Класс TZSitePag, который содержит страницу, и к которому применяется шаблон
class TZSitePage
 {
 	var $title="Заголовок страницы";
	var $header="Заголовок";
	var $content="";

	#Вывод на страницу,(нет, мы не используем echo ).
	function WriteHtml($textline)
	{
        $this->content=$this->content.$textline;
	}

	#Вывод страницы в соответсвии с шаблоном
	function Output($UseWide="0")
	{
        global $g_SelfUrl;
        global $g_SkinName;
        
        include("skins/".$g_SkinName."/skin.php");
		
		//if ($UseWide=="1")
		//{
		//   include("skins/simple/skin.php");
		//}
		//else
		//{
		//   include("skins/".$g_SkinName."/skin.php");
		//}
	}
 }
?>