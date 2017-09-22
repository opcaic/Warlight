set JAVA_HOME=c:\Program Files\Java\jdk1.8.0_65\
set PATH=c:\Program Files\Java\jdk1.8.0_65\bin

del results\replays\%1-*.replay
del results\replays\*-%1-*.replay
del results\fights\%1-*.csv"
del results\fights\*-%1.csv"

REM false;false;null;null -> visualize, forceHumanVisualization, continual:Boolean, continualFrameTimeMillis:Integer

java -cp "..\Conquest\bin\;..\Conquest-Tournament\bin;..\Conquest-Tournament\lib\*" conquest.tournament.ConquestFightConsole -s 426587 -o "GAME;PLR1;PLR2;x;x;false;false;null;null;-1;true;10000;5;100;CONTINUAL_1_1_A60_D70" -g 5 -r true -e "%1" -f "batch-fight.properties"