program  kolich;

var
  k, i: integer;
  a: array[1..10] of integer;

begin
  randomize;

  for i:=1 to 10 do a[i] := random(100);
  for i:=1 to 10 do write (a[i], ' ');

  k := 0;
  for i:=1 to 10 do
    if a[i] > 50 then
      k := k + 1;

  write('k=', k)

end.