# 🛡️ BlastGuard Plugin

## 📝 Описание
BlastGuard - плагин для Minecraft, который добавляет защитные тотемы от взрывов мобов. Тотемы создают защитное поле, которое предотвращает урон от взрывов в заданном радиусе.

## ⚙️ Установка
1. 📥 Скачайте последнюю версию плагина (JAR файл)
2. 📁 Поместите JAR файл в папку `plugins` вашего сервера
3. 🔄 Перезапустите сервер
4. ⚙️ Настройки плагина находятся в: `plugins/BlastGuard/config.yml`

## 🎮 Использование

### 📌 Команды
- `/totem give [player]` - выдать тотем себе или указанному игроку
  - Требуются права оператора (OP)
- `/totem place` - установить тотем
  - Требуется держать тотем "BLAST GUARD" в руке
  - Тотем устанавливается на блок, на который смотрит игрок

### 🏗️ Структура тотема
- Тотем состоит из двух блоков:
  - Нижний блок: Кварцевый блок
  - Верхний блок: Изумрудный блок

### ⚡ Функционал
- Тотем автоматически отменяет любые взрывы в радиусе действия
- При разрушении любого из блоков тотема, защита деактивируется
- Радиус действия настраивается в конфигурации

## ⚙️ Конфигурация
Файл: `plugins/BlastGuard/config.yml`


## 🔑 Права доступа
- `blastguard.use` - доступ к командам плагина (по умолчанию только для операторов)

## 📋 Примечания
- Тотем должен быть установлен на твердый блок
- Для установки требуется свободное пространство высотой в 2 блока
- Состояние тотемов сохраняется при перезагрузке сервера

## ⚠️ Устранение неполадок
Если тотем не работает:
1. Убедитесь, что у вас есть права оператора
2. Проверьте, что тотем называется именно "BLAST GUARD"
3. Убедитесь, что место для установки свободно
4. Проверьте логи сервера в `logs/latest.log`

## 🔧 Разработка
Для сборки плагина:
1. Клонируйте репозиторий
2. Убедитесь, что установлен Maven
3. Выполните команду: `mvn clean package`
4. Собранный плагин будет находиться в `target/BlastGuard-1.0-SNAPSHOT.jar`
