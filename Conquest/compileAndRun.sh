sh compile.sh

# max number of rounds | bot command timeout | bot 1 init | bot 2 init | visualization | replay file
java -cp bin conquest.Conquest 100 5000 "internal:conquest.bot.BotStarter" "process:java -cp bin conquest.bot.BotStarter" true replay.log
