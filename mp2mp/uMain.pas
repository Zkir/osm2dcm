unit uMain;

interface
uses uMPParser,Classes,sysutils;
procedure Main;

implementation

procedure ApplyFilteringRules(i:integer;MpSection:TMpSection;var blnSkipSection:boolean );
var
  l:integer;
  strLabel:string;
begin
     if i=0 then
       if MpSection.mpType='0x77' then
         MpSection.SetAttributeValue('StreetDesc','');


     if (MpSection.SectionType=ST_POLYLINE) and (MpSection.GetAttributeValue('Type')= '0x01') then
       MpSection.SetAttributeValue('EndLevel','6');

     if (MpSection.SectionType=ST_POLYLINE) and (MpSection.GetAttributeValue('Type')= '0x02') then
       MpSection.SetAttributeValue('EndLevel','6');




     if (MpSection.SectionType=ST_POI) then
       if (MpSection.mpType = '0x0300') and ((MpSection.GetAttributeValue('Population')='')or
                                             (strToInt(MpSection.GetAttributeValue('Population'))=0)) then
         MpSection.SetAttributeValue('Type','0x0700');


     if (MpSection.SectionType=ST_POI) then
       if (MpSection.mpType = '0x0100') or (MpSection.mpType = '0x0200') or
           (MpSection.mpType = '0x0300') or (MpSection.mpType = '0x0400') OR
           (MpSection.mpType ='0x1400')  then
         MpSection.SetAttributeValue('EndLevel','6');

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

procedure Main;

//const strTgtFileName='d:\OSM\Overview_map.ru\RU-OVRV.mp';
const strTgtFileName='d:\OSM\Overview_map.ru\World-OVRV.mp';

var MpParser:TMpParser;
    MpSection:TMpSection;
    F:TextFile;
    SourceMpFiles:TStringList;
    i:integer;

    blnSkipSection:boolean;
begin
   Writeln('mp2mp, (c) Zkir 2012, CC-BY-SA 2.0 ');
   AssignFile(uMPParser.tgtFile,strTgtFileName);
   Rewrite(uMPParser.tgtFile);

   SourceMpFiles:=TStringList.Create;
   //SourceMpFiles.Add('d:\OSM\Overview_map.ru\RussiaBkg.mp');
   SourceMpFiles.Add('d:\OSM\Overview_map.ru\WorldBkg.mp');
   SourceMpFiles.Add('d:\OSM\Overview_map.ru\_world.ovrv.cities.mp');
   //SourceMpFiles.Add('d:\OSM\Overview_map.ru\Cities.mp');
   SourceMpFiles.Add('d:\OSM\Overview_map.ru\_world.ovrv.roads.mp');

  for i := 0 to SourceMpFiles.Count-1 do
  begin
   Writeln(SourceMpFiles[i]);
   MpParser:=TMpParser.Create(SourceMpFiles[i]);


   while not MpParser.EOF do
   begin
     MpSection:=MpParser.ReadNextSection;
     blnSkipSection:=False;
     //Препроцессинг.


     //Заголовок только из первого файла.
     //Это общее правило, по необходимости
     if (i>0)and (MpSection.SectionType=ST_IMG_ID) then
       blnSkipSection:=true;

     if (MpSection.SectionType=ST_COMMENT) then
       blnSkipSection:=true;


     //Частные правила
     ApplyFilteringRules(i, MpSection,blnSkipSection );


     if not(blnSkipSection) then

       MpSection.WriteSection();
     MpSection.Free;
   end;
   MpParser.Free;
  end;
  CloseFile(uMPParser.tgtFile);
  SourceMpFiles.Free;
  Writeln('Done!');
end;
end.
