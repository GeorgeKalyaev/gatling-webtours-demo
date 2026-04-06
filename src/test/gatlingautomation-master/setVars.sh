#Название ветки GIT проекта с кодом скриптов для гатлинга
GIT_BRANCH=master
#Файл входа для гатлинга с симуляцией по формату "пакет.имя файла без разрешения"
GATLING_MAINFILE=Scripts.Debug

#Маркер автоматической подстановки логпасса при коннекте к гиту, если установлен на false то вручную придется их вбивать по мере выполнения скриптов init и updateGatlingScripts.
USE_GIT_LOGPASS=true



#Данные для коннекта к гиту
#HTTP ссылка на гит без HTTPS://
GIT_URL=gitlab.appline.ru/loadtesting/GatlingAutomation.git
#Логин гита
GIT_USER=user
#Пароль гита. Если в пароле присутствует символ @ то его нужно заменить на %40
GIT_PASS=password

#Настройка путей ресурсов и пакета со скриптами в локальном проекте гита (На конце не должно быть символа "/")
PROJECTGIT_RESOURCES_PATH=./projectGit/dev/scripts/Scripts/src/test/resources
PROJECTGIT_SCRIPTS_PATH=./projectGit/dev/scripts/Scripts/src/test/scala
