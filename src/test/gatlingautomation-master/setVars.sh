#Название ветки GIT проекта с кодом скриптов для гатлинга
GIT_BRANCH=master
#Файл входа для гатлинга с симуляцией по формату "пакет.имя файла без разрешения"
GATLING_MAINFILE=NewScripts.Debug

# true — логин/пароль в URL при clone/pull (удобно для приватного GitLab/GitHub).
# false — без вшитых учёток (публичный репозиторий или ввод вручную при запросе).
USE_GIT_LOGPASS=false



# Данные для коннекта к Git (HTTPS clone/pull).
# GIT_URL — хост и путь без префикса https://
#   Пример GitLab: gitlab.com/my-group/my-gatling-project.git
#   Пример GitHub: github.com/GeorgeKalyaev/gatling-webtours-demo.git
#   Свой сервер GitLab: gitlab.company.local/group/repo.git
GIT_URL=github.com/GeorgeKalyaev/gatling-webtours-demo.git
# Логин (для публичного репозитория часто не нужен — можно любой заглушкой при USE_GIT_LOGPASS)
GIT_USER=user
# Пароль или токен. Символ @ в пароле заменить на %40
GIT_PASS=password

# Пути внутри клонированного репозитория до resources и scala (без "/" на конце).
# Для этого репозитория (корень = корень клона):
PROJECTGIT_RESOURCES_PATH=./projectGit/src/test/resources
PROJECTGIT_SCRIPTS_PATH=./projectGit/src/test/scala
