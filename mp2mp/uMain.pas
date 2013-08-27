unit uMain;

interface
uses uMPParser,Classes,sysutils, RegularExpressions;
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

type TRuleUpliftCities=Class(TRule)
  strEndLevel:string;
  constructor Create(EndLevel:string);
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleUpliftTowns=Class(TRule)
  strEndLevel:string;
  constructor Create(EndLevel:string);
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleUpliftNumberedRoads6=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;


type TRuleDecreaseNonEuTrunk=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleUpliftEuRoads=Class(TRule)
  strEndLevel:string;
  constructor Create(EndLevel:string);
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleUpliftMainRoads=Class(TRule)
  strEndLevel:string;
  constructor Create(EndLevel:string);
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleRemoveLabels=Class(TRule)
  function CheckCondition(MpSection:TMpSection):boolean;override;
  procedure Apply(MpSection:TMpSection;var blnSkipSection:boolean);override;
end;

type TRuleSetAttribute=Class(TRule)
  strAttribute:string;
  strValue:string;
  constructor Create(aAttribute,aValue:string);
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
   aSourceFile.FileName:=StringReplace(aSourceFile.FileName ,'%OSM_DATA%','D:\OSM\osm_data\',[rfReplaceAll, rfIgnoreCase]);
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
         else  if RuleNode.Attributes['predefined']='uplift_cities' then
         begin
           aRule:=TRuleUpliftCities.Create(RuleNode.Attributes['EndLevel']);
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='uplift_towns' then
         begin
           aRule:=TRuleUpliftTowns.Create(RuleNode.Attributes['EndLevel']);
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='uplift_numbered_roads_6' then
         begin
           aRule:=TRuleUpliftNumberedRoads6.Create;
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='decrease_noneu_trunk' then
         begin
           aRule:=TRuleDecreaseNonEuTrunk.Create();
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='uplift_eu_roads' then
         begin
           aRule:=TRuleUpliftEuRoads.Create(RuleNode.Attributes['EndLevel']);
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='uplift_main_roads' then
         begin
           aRule:=TRuleUpliftMainRoads.Create(RuleNode.Attributes['EndLevel']);
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='set_end_level' then
         begin
           aRule:=TRuleSetAttribute.Create('EndLevel',RuleNode.Attributes['EndLevel']);
           aSourceFile.Rules.Add(aRule);
         end
         else  if RuleNode.Attributes['predefined']='remove_labels' then
         begin
           aRule:=TRuleRemoveLabels.Create;
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

//UpliftCities
constructor TRuleUpliftCities.Create(EndLevel:string);
begin
  strEndLevel:=EndLevel;
end;
function TRuleUpliftCities.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POI) then
    if (MpSection.mpType = '0x0100') or (MpSection.mpType = '0x0200') or
       (MpSection.mpType = '0x0300') or (MpSection.mpType = '0x0400') or
       (MpSection.mpType = '0x0500') or (MpSection.mpType = '0x0600') or
       (MpSection.mpType = '0x0700') or (MpSection.mpType = '0x0800') or
       (MpSection.mpType = '0x1400')  then
      result:=true;

  if (MpSection.SectionType=ST_POLYLINE) then
    if (MpSection.mpType = '0x1e') or (MpSection.mpType = '0x1c') then
      result:=true;
end;


procedure TRuleUpliftCities.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('EndLevel',strEndLevel);
end;

//TRuleUpliftTowns
constructor TRuleUpliftTowns.Create(EndLevel:string);
begin
  strEndLevel:=EndLevel;
end;

function TRuleUpliftTowns.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POI) then
    if (MpSection.mpType = '0x0900') or (MpSection.mpType = '0x0a00') or
       (MpSection.mpType = '0x0b00') or (MpSection.mpType = '0x0c00') or
       (MpSection.mpType = '0x0d00') or (MpSection.mpType = '0x0e00')   then
      result:=true;


end;

procedure TRuleUpliftTowns.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('EndLevel',strEndLevel);
end;

//uplift_numbered_roads_6
function TRuleUpliftNumberedRoads6.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POLYLINE) then
    if (MpSection.GetAttributeValue('RouteParam')<>'') and
       (MpSection.GetAttributeValue('Label')<>'')   then
      result:=true;


end;

procedure TRuleUpliftNumberedRoads6.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
         MpSection.SetAttributeValue('EndLevel','6');
end;

//TRuleUpliftEuRoads
constructor TRuleUpliftEuRoads.Create(EndLevel:string);
begin
  strEndLevel:=EndLevel;
end;
function TRuleUpliftEuRoads.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POLYLINE) then
    if (MpSection.GetAttributeValue('RouteParam')<>'') and
       TRegEx.IsMatch(MpSection.GetAttributeValue('Label'),'E[ -]?[0-9]{1,3}')   then
      result:=true;


end;


procedure TRuleUpliftEuRoads.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('EndLevel',strEndLevel);
end;

//TRuleDecreaseNonEuTrunk
function TRuleDecreaseNonEuTrunk.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POLYLINE) then
    if (MpSection.GetAttributeValue('RouteParam')<>'') and
       (MpSection.GetAttributeValue('Type')='0x1') and
       not TRegEx.IsMatch(MpSection.GetAttributeValue('Label'),'E[ -]?[0-9]{1,3}')   then
      result:=true;
end;


procedure TRuleDecreaseNonEuTrunk.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('Type','0x2');
end;

//TRuleUpliftMainRoads
constructor TRuleUpliftMainRoads.Create(EndLevel:string);
begin
  strEndLevel:=EndLevel;
end;
function TRuleUpliftMainRoads.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=false;
  if (MpSection.SectionType=ST_POLYLINE) then
    if (MpSection.GetAttributeValue('Type')='0x1') or
       (MpSection.GetAttributeValue('Type')='0x2')    then
      result:=true;


end;

procedure TRuleUpliftMainRoads.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('EndLevel',strEndLevel);
end;

//TRuleSetAttribute
constructor TRuleSetAttribute.Create(aAttribute:string;aValue:string);
begin
  strAttribute:=aAttribute;
  strValue:=aValue;
end;

function TRuleSetAttribute.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=true;
end;

procedure TRuleSetAttribute.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue(strAttribute,strValue);
end;

//TRuleRemoveLables
function TRuleRemoveLabels.CheckCondition(MpSection:TMpSection):boolean;
begin
  result:=true;
end;

procedure TRuleRemoveLabels.Apply(MpSection:TMpSection;var blnSkipSection:boolean);
begin
  MpSection.SetAttributeValue('Label','');
end;

//SetRegionMap
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
    intFirstSourceWithRoutingGraph:integer;
begin
  Writeln('mp2mp, (c) Zkir 2012, CC-BY-SA 2.0 ');
  Writeln('Target file: ',strTgtFileName);
  AssignFile(uMPParser.tgtFile,strTgtFileName);
  Rewrite(uMPParser.tgtFile);

  SourceMpFiles:=TSourceFileList.Create (strConfigFileName);

  Writeln('Processing source files:');
  intFirstSourceWithRoutingGraph:=-1;
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
       //Нужно проверить, что дорожный граф присутствует только один раз.
       //Будем судить по наличию RouteParam
       if MpSection.GetAttributeValue('RouteParam')<>'' then
         begin
           if intFirstSourceWithRoutingGraph=-1 then
             intFirstSourceWithRoutingGraph:=i;

           if intFirstSourceWithRoutingGraph<>i then
             raise Exception.Create('Routing graph is present more than once. It is not allowed');

         end;

       MpSection.WriteSection();
       NSections:=NSections+1;
     end;
     MpSection.Free;
   end;
   MpParser.Free;
   Writeln(' '+IntToStr(NSections)+' section(s) written');
  end;
  Writeln(uMPParser.tgtFile,'; ### That''s all, folks!');
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
  try
   ParseCommandLine(strTargetFileName, strConfigFileName);
   Process(strTargetFileName, strConfigFileName);
  except
    on E : Exception do
      begin
        writeln(E.Message);
        halt(1); 
      end;
  end;
end;

initialization
  CoInitialize(nil);
end.
