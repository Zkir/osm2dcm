unit uVB6runtime;

interface
uses classes;
function vb6_Left(s:string; l:integer):string;
function vb6_Right(s:string; l:integer):string;
function Split (str: String; strSeparator:String ):TStringList;

implementation
uses SysUtils;

function vb6_Left(s:string; l:integer):string;
begin
  result:=copy(s,1,l);
end;

function vb6_Right(s:string; l:integer):string;
begin
  result:=copy(s,length(s)-l+1,l);
end;

//–азделение строки на части по разделителю
function Split (str: String; strSeparator:String ):TStringList;
  var t:TStringList;
begin
  t:=TStringList.create; //создаЄм класс
  t.text:=stringReplace(str,strSeparator,#13#10,[rfReplaceAll]);//мы замен€ем все разделители на символы конца строки
  result:= t;
end;


end.
