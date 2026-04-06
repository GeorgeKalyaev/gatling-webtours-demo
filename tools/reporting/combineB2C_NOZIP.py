import xlwt
import pandas as pd
import os
import zipfile
from collections import defaultdict


#Для второго скрипта
requests_mob = defaultdict(int)
requests_web = defaultdict(int)
requests_status = defaultdict(int)
requests_events = defaultdict(int)
sum_web = 0
sum_mob = 0
sum_status = 0
sum_events = 0

# Название результирующего файла
output_file=input("Введите имя результирующего файла: ")
output_file_path = output_file+'.xls'

# Список кодов ошибок
error_codes_to_track = ["found 400","found 401","found 402","found 403","found 404","found 405","found 406","found 407","found 408","found 409","found 410","found 411","found 412","found 413","found 414","found 415","found 416","found 417","found 418","found 419","found 421","found 422","found 423","found 424","found 425","found 426","found 428","found 429","found 431","found 449","found 451","found 499","found 500","found 501","found 502","found 503","found 504","found 505","found 506","found 507","found 508","found 509","found 510","found 511","found 520","found 521","found 522","found 523","found 524","found 525","found 526","Premature close", "after 60000 ms"]

# Название архива zip
name_zip=input("Введите имя папки: ")


nogroups=name_zip+'\\without_groups\\simulation_without_groups.log'
groups = name_zip+'\\with_groups\\simulation.log'
zip_archive_name = name_zip +'.zip'

# # Путь к папке, где будет распакован архив
extracted_folder = os.path.join(os.getcwd(), name_zip)

# # Создаем папку для распаковки архива, если она не существует
# if not os.path.exists(extracted_folder):
#     os.makedirs(extracted_folder)

# # Распаковываем архив zip
# with zipfile.ZipFile(zip_archive_name, 'r') as zip_ref:
#     zip_ref.extractall(extracted_folder)

# Пути к файлам
log_file_path = os.path.join(extracted_folder, groups)
second_script_file_path = os.path.join(extracted_folder, nogroups)


# Создаем словарь для хранения данных об ошибках
error_data = defaultdict(lambda: defaultdict(int))

# Открываем файл и читаем его построчно
with open(log_file_path, 'r', encoding='utf-8', errors='ignore') as file:
    for line in file:
        if any(error_code in line for error_code in error_codes_to_track):
            # Разбиваем строку на слова
            words = line.split()
            if len(words) >= 4:
                # Получаем код ошибки и увеличиваем счетчик для данной комбинации
                for error_code in error_codes_to_track:
                    if error_code in line:
                        key = words[1], words[2]
                        error_data[error_code][key] += 1

with open(second_script_file_path, "r", encoding="utf-8") as file:
    for line in file:
        if line.startswith("REQUEST"):
            fields = line.strip().split("\t")
            if len(fields) >= 5 and fields[5] == "OK":
                request = fields[2]
                if request == "/v2/orders/ID/completion":
                    timestamp = int(fields[3]) // 1000 // 60
                    requests_mob[timestamp] += 1
                if request == "/api/v3/checkout/orders/ID/completion":
                    timestamp = int(fields[3]) // 1000 // 60
                    requests_web[timestamp] += 1
                if request == "/v2/shipments/ID/cancellations":
                    timestamp = int(fields[3]) // 1000 // 60
                    requests_status[timestamp] += 1
                if request == "/api/v3/user/shipment_cancellation_reasons":
                    timestamp = int(fields[3]) // 1000 // 60
                    requests_events[timestamp] += 1

# Создаем новую книгу Excel для объединенных результатов
combined_workbook = xlwt.Workbook(encoding='utf-8')
time_style = xlwt.easyxf(num_format_str='YYYY-MM-DD HH:MM:SS')

# Создаем первый лист для данных из первого скрипта
worksheet1 = combined_workbook.add_sheet('Errors')

# Заголовки столбцов
worksheet1.write(0, 0, 'Error Code')
worksheet1.write(0, 1, 'Group')
worksheet1.write(0, 2, 'Endpoint')
worksheet1.write(0, 3, 'Count')

# Заполняем таблицу данными из первого скрипта
row = 1
for error_code in error_codes_to_track:
    data = error_data[error_code]
    sorted_data = sorted(data.items(), key=lambda x: x[1], reverse=True)
    for key, count in sorted_data:
        worksheet1.write(row, 0, error_code)
        worksheet1.write(row, 1, key[0])
        worksheet1.write(row, 2, key[1])
        worksheet1.write(row, 3, count)
        row += 1

# Создаем второй лист для данных из второго скрипта
worksheet2 = combined_workbook.add_sheet('Requests per min')

# Заголовки столбцов
worksheet2.write(0, 0, 'Время')
worksheet2.write(0, 1, 'Приложение /v2/orders/ID/completion')
worksheet2.write(0, 2, 'Веб /api/v3/checkout/orders/ID/completion')
worksheet2.write(0, 3, 'Приложение отмена /v2/shipments/ID/cancellations')
worksheet2.write(0, 4, 'Веб отмена /api/v3/user/shipment_cancellation_reasons')

# Заполняем таблицу данными из второго скрипта
row = 1
for key, value in requests_mob.items():
    sum_web = sum_web + requests_web[key]
    sum_mob = sum_mob + requests_mob[key]
    sum_status = sum_status + requests_status[key]
    sum_events = sum_events + requests_events[key]
    worksheet2.write(row, 0, pd.to_datetime(key, unit="m"), time_style)
    worksheet2.write(row, 1, requests_mob[key])
    worksheet2.write(row, 2, requests_web[key])
    worksheet2.write(row, 3, requests_status[key])
    worksheet2.write(row, 4, requests_events[key])
    row += 1

# Сохраняем объединенные результаты в файл
combined_workbook.save(output_file_path)

print(f'Объединенные результаты сохранены в {output_file_path}')
