unit uMain;

interface
uses uMPParser,Classes,sysutils;
procedure Main;

type TRule=Class
  function CheckCondition(MpSection:TMpSection):boolean;virtual;abstract;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);virtual;abstract;
end;

type TSourceFile=Class
  FileName:string;
  Rules:TList;
  constructor Create();
End;

type TSourceFileList=Class
  private
    mSourceFiles:TList;
    function GetCount:integer;
    function Get(Index: Integer):TSourceFile;
  public
    constructor Create(); overload;
    constructor Create(strFileName:string);overload;
    property Count:integer read GetCount;
    property Items[Index: Integer]: TSourceFile read Get; default;

End;

//Заданные наперед правила
type TRuleNoPOI=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleSkipCommentSections=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleUpliftCities6=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleSetRegionMap=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

implementation

uses Xml.XMLIntf,XMLDoc,ActiveX;

constructor TSourceFile.Create();
begin
  FileName:='';
  Rules:=TList.Create;
end;


constructor TSourceFileList.Create();
var aSourceFile:TSourceFile;
    aRule:TRule;
begin

   mSourceFiles:=TList.Create;

   //Имитируем загрузку xml
   aSourceFile:=TSourceFile.Create;
   aSourceFile.FileName:='d:\OSM\Overview_map.ru\RussiaBkg.mp';
   aRule:=TRuleSkipCommentSections.Create;
   aSourceFile.Rules.Add(aRule);
   mSourceFiles.Add(aSourceFile);

   aSourceFile:=TSourceFile.Create;
   aSourceFile.FileName:='d:\OSM\Overview_map.ru\ru.cities.mp';
   aRule:=TRuleSkipCommentSections.Create;
   aSourceFile.Rules.Add(aRule);
   mSourceFiles.Add(aSourceFile);

   aSourceFile:=TSourceFile.Create;
   aSourceFile.FileName:='d:\OSM\Overview_map.ru\ru.roads.mp';

   aRule:=TRuleSkipCommentSections.Create;
   aSourceFile.Rules.Add(aRule);


   aRule:=TRuleNoPOI.Create;
   aSourceFile.Rules.Add(aRule);

   mSourceFiles.Add(aSourceFile);

end;
constructor TSourceFileList.Create(strFileName:string);
var
  xml:IXMLDocument;
  RuleNode:IXMLNode;
  i,j:integer;
  aSourceFile:TSourceFile;
  aRule:TRule;

begin
  mSourceFiles:=TList.Create;

  xml:=LoadXMLDocument(strFileName);
  xml.Active:=True;

  //Перебираем исходные файлы
  for i := 0 to xml.DocumentElement.ChildNodes.Count-1 do
  if (xml.DocumentElement.ChildNodes[i].NodeName='source_file') then
  begin
   aSourceFile:=TSourceFile.Create;
   aSourceFile.FileName:=xml.DocumentElement.ChildNodes[i].Attributes['name'] ;
   //Смотрим правила
   for j := 0 to xml.DocumentElement.ChildNodes[i].ChildNodes.Count-1 do
   begin
     RuleNode:=xml.DocumentElement.ChildNodes[i].ChildNodes[j];
     if RuleNode.NodeName='rule' then
       if RuleNode.Attributes['predefined']<>'' then
         if RuleNode.Attributes['predefined']='skip_comment_sections' then
         begin
           aRule:=TRuleSkipCommentSections.Create;
           aSourceFile.Rules.Add(aRule);

         end
         else  if RuleNode.Attributes['predefined']='skip_poi' then
         begin
           aRule:=TRuleNoPOI.Create;
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='uplift_cities_6' then
         begin
           aRule:=TRuleUpliftCities6.Create;
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='set_region_map' then
         begin
           aRule:=TRuleSetRegionMap.Create;
           aSourceFile.Rules.Add(aRule);
         end
         else
           raise Exception.Create('Unknown predefined rule: '+RuleNode.Attributes['predefined']);


   end;




   //Добавляем исходный файл с правилами в список для обработки.
   mSourceFiles.Add(aSourceFile);
  end;

end;
function TSourceFileList.GetCount:integer;
begin
  result:=mSourceFiles.Count;
end;

function TSourceFileList.Get(Index: Integer):TSourceFile;
begin
  result:=mSourceFiles[Index];
end;

//Предопределенные правила

function TRuleNoPOI.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=(MpSection.SectionType=ST_POI);
end;

procedure TRuleNoPOI.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  blnSkipSection:=true;
end;

function TRuleSkipCommentSections.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=(MpSection.SectionType=ST_COMMENT);
end;

procedure TRuleSkipCommentSections.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  blnSkipSection:=true;
end;

function TRuleUpliftCities6.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POI) then
    if (MpSection.mpType = '0x0100') or (MpSection.mpType = '0x0200') or
       (MpSection.mpType = '0x0300') or (MpSection.mpType = '0x0400') or
       (MpSection.mpType = '0x1400')  then
      result:=true;
end;

procedure TRuleUpliftCities6.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
         MpSection.SetAttributeValue('EndLevel','6');

end;

function TRuleSetRegionMap.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_IMG_ID) then

      result:=true;
end;


procedure TRuleSetRegionMap.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('RegionMap','1');

end;

(*
procedure ApplyFilteringRules(i:integer;MpSection:TMpSection;var blnSkipSection:boolean );
var
  l:integer;
  strLabel:string;
begin
     if i=0 then
       if MpSection.mpType='0x77' then
         MpSection.SetAttributeValue('StreetDesc','');




     if (MpSection.SectionType=ST_POI) then
       if (MpSection.mpType = '0x0300') and ((MpSection.GetAttributeValue('Population')='')or
                                             (strToInt(MpSection.GetAttributeValue('Population'))=0)) then
         MpSection.SetAttributeValue('Type','0x0700');



     if  ((MpSection.GetAttributeValue('Capital')='yes')and (MpSection.GetAttributeValue('AdminLevel')='')) or
         ((MpSection.GetAttributeValue('Capital')='yes')and (MpSection.GetAttributeValue('AdminLevel')='2'))  then
       MpSection.SetAttributeValue('EndLevel','6');

     if (i=2) then
     begin
     if (MpSection.SectionType<>ST_POLYLINE) then
       blnSkipSection:=true
      else
        begin
          //strLabel:=MpSection.GetAttributeValue('Label');
          //if not(((Pos('~',strLabel)=1) and (Pos ('E',strLabel)<>0))) then
          //  blnSkipSection:=true;

        end ;
     end;



    { if (i>0)and (MpSection.GetAttributeValue('EndLevel')<>'') then
       begin
         l:=StrToInt(MpSection.GetAttributeValue('EndLevel'));
         l:=l-4;
         MpSection.SetAttributeValue('EndLevel',inttostr(l));

       end;}
end;
*)

procedure Process(strTgtFileName, strConfigFileName:string);

var MpParser:TMpParser;
    MpSection:TMpSection;
    SourceMpFiles:TSourceFileList;
    i,j:integer;
    NSections:integer;
    blnSkipSection:boolean;
begin
  Writeln('mp2mp, (c) Zkir 2012, CC-BY-SA 2.0 ');
  Writeln('Target file: ',strTgtFileName);
  AssignFile(uMPParser.tgtFile,strTgtFileName);
  Rewrite(uMPParser.tgtFile);

  SourceMpFiles:=TSourceFileList.Create (strConfigFileName);

  Writeln('Processing source files:');
  for i := 0 to SourceMpFiles.Count-1 do
  begin
   Writeln(SourceMpFiles[i].FileName);
   MpParser:=TMpParser.Create(SourceMpFiles[i].FileName);
   NSections:=0;

   while not MpParser.EOF do
   begin
     MpSection:=MpParser.ReadNextSection;
     blnSkipSection:=False;
     //Препроцессинг.


     //Заголовок только из первого файла.
     //Это общее правило, по необходимости
     if (i>0)and (MpSection.SectionType=ST_IMG_ID) then
       blnSkipSection:=true
     else
     begin
       //Применяем частные правила.
       // Правила применются строго по порядку
       for j := 0 to SourceMpFiles[i].Rules.Count-1  do
         if TRule(SourceMpFiles[i].Rules[j]).CheckCondition(MpSection) then
           TRule(SourceMpFiles[i].Rules[j]).Apply(MpSection,blnSkipSection);
      end;

     if not(blnSkipSection) then
     begin
       MpSection.WriteSection();
       NSections:=NSections+1;
     end;
     MpSection.Free;
   end;
   MpParser.Free;
   Writeln(' '+IntToStr(NSections)+' section(s) written');
  end;
  CloseFile(uMPParser.tgtFile);
  SourceMpFiles.Free;
  Writeln('Done!');
end;

procedure ParseCommandLine(var strTargetFileName, strConfigFileName:string);
begin
  strTargetFileName:=ParamStr(1);
  strConfigFileName:=ParamStr(2);
end;

procedure Main();
//const
//  strTgtFileName='d:\OSM\Overview_map.ru\RU-OVRV.mp';
var
   strTargetFileName, strConfigFileName:string;
begin
  ParseCommandLine(strTargetFileName, strConfigFileName);
  Process(strTargetFileName, strConfigFileName);
end;

initialization
  CoInitialize(nil);
end.
