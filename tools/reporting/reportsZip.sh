echo Введите полный путь в папку gatling:
read path
echo Введите полный путь в папку results:
read path_results
echo Начало сбора результата последнего запуска
cd ${path_results}
echo Введите имя папки с логом:
read var_name
echo Папка с логом: ${var_name}

cd $var_name
var_files_count=$(ls -1 | wc -l)
cd ../../..

mkdir ${path_results}${var_name}_full
mkdir ${path_results}${var_name}_noGroupTemp
mkdir ${path_results}${var_name}_full/with_groups
mkdir ${path_results}${var_name}_full/without_groups

if [ $var_files_count -eq 1 ]
then
  echo Генерация HTML отчета с группами начата
  sh ./gatling-charts-highcharts-bundle-3.9.5/bin/gatling.sh -ro $var_name
  echo Генерация HTML отчета с группами окончена
else
  echo HTML отчет с группами уже был ранее создан
fi
cp -r ${path_results}${var_name}/* ${path_results}${var_name}_full/with_groups/
echo Генерация HTML отчета без групп начата
cat ${path_results}${var_name}_full/with_groups/simulation.log | grep -v "^GROUP" | sed 's/REQUEST\s[^/]*/REQUEST\t\t/g' | sed 's/-[0-9]*//g' > ${path_results}${var_name}_noGroupTemp/simulation_without_groups.log
sh ${path}/bin/gatling.sh -ro ${var_name}_noGroupTemp
mv ${path_results}${var_name}_noGroupTemp/* ${path_results}${var_name}_full/without_groups
rmdir ${path_results}${var_name}_noGroupTemp
echo Генерация HTML отчета без групп окончена

cd ${path_results}

zip -r ${var_name}_full.zip ${var_name}_full/*
cd ../..
mv ${path_results}${var_name}_full.zip ${path_results}
echo Результат досутпен по пути \"${path_results}${var_name}_full.zip\"
