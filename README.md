# 🛡️ BlastGuard Plugin

## 📝 Описание
BlastGuard - плагин для Minecraft, который добавляет защитные тотемы от взрывов мобов. Тотемы создают защитное поле, которое предотвращает урон от взрывов в заданном радиусе.

## ⚙️ Установка
1. 📥 Скачайте последнюю версию плагина (JAR файл)
2. 📁 Поместите JAR файл в папку `plugins` вашего сервера
3. 🔄 Перезапустите сервер

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
Файл: `config.yml`
