#read -p "Вы уверены что хотите удалить все логи и резалты? yes|no " yn
#case $yn in
#yes)
  echo Начало удаления всех логов и резалтов
       rm ./gatling/*.log
       rm ./gatling/output/*
       rm -r ./gatling/results/*
       echo Все логи и резалты удалены
#esac
