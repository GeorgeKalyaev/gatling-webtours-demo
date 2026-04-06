import os

# Укажите путь к папке, в которой находятся логи Gatling (все .log в ней будут склеены по порядку обхода).
# В инструкции для двух генераторов: сначала with_groups или without_groups с двумя файлами (simulation.log, simulation1.log и т.д.).
input_folder = 'C:/path/to/your/test1/test1/without_groups'

# Имя файла для объединенного лога
output_file = 'merged_simulation.log'

# Открываем файл для записи объединенного лога
with open(output_file, 'w', encoding='utf-8') as output_log:
    # Перебираем лог-файлы в указанной папке
    for root, dirs, files in os.walk(input_folder):
        for file in files:
            if file.endswith('.log'):
                log_file_path = os.path.join(root, file)
                print(f'Обрабатываем файл: {log_file_path}')
                with open(log_file_path, 'r', encoding='utf-8') as input_log:
                    # Читаем и записываем содержимое текущего лога построчно
                    output_log.writelines(input_log.readlines())
                print(f'Файл {log_file_path} успешно добавлен в объединенный лог.')

print(f'Объединенный лог сохранен в файле: {output_file}')
