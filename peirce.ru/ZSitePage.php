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
	
	#Поля для рсс
	var $cnl_link;
	var $item_title;
	var $item_link;
	var $item_guid;
    var $item_pubDate;

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
	#Вывод страницы в форма rss
	function OutputAsRss()
	{	
		
	  echo('<?xml version="1.0" encoding="utf-8"?>
          <rss version="2.0">
          <channel>
            <title>'.$this->title.'</title>
            <link>'.$this->cnl_link.'</link>
            <item>
              <guid>'.$this->item_guid.'/0</guid>
              <title>'.$this->item_title.'</title>
              <link>'.$this->item_link.'</link>
              <author>Ch.S. Peirce</author>
              <pubDate>'.$this->item_pubDate.'</pubDate>
              <description><![CDATA['.($this->content).']]></description>
            </item>
          </channel>
       </rss>');
    }  
 }
?>