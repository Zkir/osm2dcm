program mp2mp;

{$APPTYPE CONSOLE}

{$R *.res}

uses
  System.SysUtils,
  uMain in 'uMain.pas',
  uMPParser in '..\RoutingTest\uMPParser.pas',
  uVB6runtime in '..\RoutingTest\uVB6runtime.pas';

begin
  try
    Main;
  except
    on E: Exception do
      Writeln(E.ClassName, ': ', E.Message);
  end;
end.
