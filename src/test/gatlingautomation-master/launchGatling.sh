. ./setVars.sh

#Запуск гатлинга
nohup sh ./gatling/bin/gatling.sh -bm -rm local -s $GATLING_MAINFILE > ./gatling/output/$(date +%s)-g.out &

#Вывод гатлинга на экран
sh ./viewGatlingOutput.sh