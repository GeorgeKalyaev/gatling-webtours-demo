echo Начало сбора результата последнего запуска
cd ./gatling/results/
var_name=$(ls | tail -n 1)
echo последний запуск: ${var_name}

cd $var_name
var_files_count=$(ls -1 | wc -l)
cd ../../..

mkdir ./gatling/results/${var_name}_full
mkdir ./gatling/results/${var_name}_noGroupTemp
mkdir ./gatling/results/${var_name}_full/with_groups
mkdir ./gatling/results/${var_name}_full/without_groups
mkdir ./gatling/results/${var_name}_full/SCRIPTS

if [ $var_files_count -eq 1 ]
then
  echo Генерация HTML отчета с группами начата
  sh ./gatling/bin/gatling.sh -ro $var_name
  echo Генерация HTML отчета с группами окончена
else
  echo HTML отчет с группами уже был ранее создан
fi
cp -r ./gatling/results/${var_name}/* ./gatling/results/${var_name}_full/with_groups/
echo Генерация HTML отчета без групп начата
cat ./gatling/results/${var_name}_full/with_groups/simulation.log | grep -v "^GROUP" | sed 's/REQUEST\s[^/]*/REQUEST\t\t/g' | sed 's/-[0-9]*//g' > ./gatling/results/${var_name}_noGroupTemp/simulation_without_groups.log
sh ./gatling/bin/gatling.sh -ro ${var_name}_noGroupTemp
mv ./gatling/results/${var_name}_noGroupTemp/* ./gatling/results/${var_name}_full/without_groups
rmdir ./gatling/results/${var_name}_noGroupTemp
echo Генерация HTML отчета без групп окончена

echo Начат сбор скриптов для артифактов
cp -r ./gatling/user-files/* ./gatling/results/${var_name}_full/SCRIPTS/
echo Сбор скриптов для артифактов окончен

echo Начата подкотовка итогового архива
cd ./gatling/results/

zip -r ${var_name}_full.zip ${var_name}_full/*
cd ../..
mv ./gatling/results/${var_name}_full.zip ./results/
echo Результат досутпен по пути \"/results/${var_name}_full.zip\"