program RoutingTest;

{$APPTYPE CONSOLE}

uses
  SysUtils,
  uRoutingTest in 'uRoutingTest.pas',
  uMPParser in 'uMPParser.pas',
  uMain in 'uMain.pas',
  uVB6runtime in 'uVB6runtime.pas',
  zADODB in 'zADODB.pas';

begin
  try
    { TODO -oUser -cConsole Main : Insert code here }
    Main();

  except
    on E: Exception do
      Writeln(E.ClassName, ': ', E.Message);
  end;
end.
