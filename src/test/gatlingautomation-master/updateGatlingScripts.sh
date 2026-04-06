. ./setVars.sh


echo Начало апдейта проекта с ветки $GIT_BRANCH
cd ./projectGit
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "$GIT_BRANCH" ]; then
  git checkout $GIT_BRANCH
fi

if [ "$USE_GIT_LOGPASS" = "true" ]; then
  git pull https://${GIT_USER}:${GIT_PASS}@${GIT_URL} $GIT_BRANCH
else
  git pull https://${GIT_URL} $GIT_BRANCH
fi


cd ..
echo Проект обновлен c гита

echo Начало удаления пользовательских ресурсов гатлинга
rm -rf ./gatling/user-files/resources/*
echo Пользовательские ресурсы гатлинга удалены

echo Начало удаления пользовательских скриптов гатлинга
rm -rf ./gatling/user-files/simulations/*
echo Пользовательские скрипты гатлинга удалены

echo Начало копирования новых ресурсов
cp -r ${PROJECTGIT_RESOURCES_PATH}/* ./gatling/user-files/resources/
#rm ./gatling/user-files/resources/gatling.conf
#rm ./gatling/user-files/resources/logback-test.xml
echo Ресурсы скопированы

echo Начало копирования новых скриптов
cp -r ${PROJECTGIT_SCRIPTS_PATH}/* ./gatling/user-files/simulations/
echo Скрипты скопированы
