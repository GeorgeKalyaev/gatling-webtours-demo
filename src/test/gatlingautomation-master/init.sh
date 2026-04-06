. ./setVars.sh

mkdir gatling
mkdir ./gatling/output
mkdir results
mkdir projectGit
echo Нужные папки созданы

echo Начинаем установку нужных пакетов

git --version 2>&1 >/dev/null
GIT_IS_AVAILABLE=$?
if [ $GIT_IS_AVAILABLE -eq 0 ]; then
  echo GIT установлен
else
  echo GIT не установлен, начинаем установку, Введите SUDO ПАРОЛЬ если потребуется
  sudo apt-get install git
fi

zip --version 2>&1 >/dev/null
ZIP_IS_AVAILABLE=$?
if [ $ZIP_IS_AVAILABLE -eq 0 ]; then
  echo ZIP установлен
else
  echo ZIP не установлен, начинаем установку, Введите SUDO ПАРОЛЬ если потребуется
  sudo apt-get install zip
fi

echo Клонирование Gatling проекта из гита начато
if [ "$USE_GIT_LOGPASS" = "true" ]; then
  git clone --branch $GIT_BRANCH https://${GIT_USER}:${GIT_PASS}@${GIT_URL} ./projectGit/
else
  git clone --branch $GIT_BRANCH https://${GIT_URL} ./projectGit/
fi

echo Настройка окончена
echo Поместите содержимое папки \"gatling-charts-highcharts-bundle-3.9.5\" в папку \"gatling\"