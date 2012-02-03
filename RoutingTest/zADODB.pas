unit zADODB;

interface
uses ComObj, ADOInt,ActiveX;

//Определение типа Recordset
type Recordset=_Recordset;

implementation

initialization
  CoInitializeEx(nil, COINIT_MULTITHREADED);

finalization
  CoUninitialize();
end.
