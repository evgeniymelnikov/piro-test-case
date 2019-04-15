# Piro Test Case
Реализация с учётом недопустимости использования какой-либо БД.
В качестве хранилище использован CopyOnWriteArrayList (с синхронизациями на случай изменения состояния элементов, 
хранящихся в хранилище).

Имеется CRUD API и API для пагинации. Логика функционирования пагинации и репозитории сделаны по подобию спринговских 
(к примеру, пагинация функционирует на основе абстракции, напоминающей Spring Specification 
с её методом toPredicate(...)).

Настроен swagger для проверки API + интеграционные тесты.

Текст задания:
----------


Приложение представляет собой REST API, которое позволяет работать с виджетами. Виджеты имеют позицию(x и y), ширину, высоту, z-index(чем выше значение, тем выше лежит виджет), дату последней модификации и уникальный идентификатор. С помощью REST API запросов мы можем добавлять, удалять и менять виджеты. Также мы можем получить все виджеты, либо какой-то один виджет.

Список запросов:

— Создание виджета. При создании виджета мы должны указать его координаты, ширину и высоту. Если мы не указываем z-index, то виджет перемещается на передний план. Если указываем существующий z-index, то новый виджет сдвигает все виджеты с таким же или большим индексом в большую сторону. 
На выходе мы должны получить полное представление виджета, вместе со сгенерированным уникальным идентификатором. 

— Получение виджета. Указав идентификатор виджета, мы можем получить его полное представление. 

— Изменение виджета. Мы должны иметь возможность изменить все данные виджета, кроме его идентификатора. Важно чтобы все изменения над виджетом происходили синхронно. То есть, если мы меняем местоположение виджета с координат 50;50 на 100;100, мы не должны случайно получить координаты 50;100, если отправили запрос на чтение виджета. 

— Получение списка виджетов. По умолчанию мы выдаем все виджеты отсортированные по индексу, от меньшего к большему. 

— Удаление виджета. Мы можем удалить виджет по его идентификатору.

Требования:
1. Реализация должна быть основана Spring. 
2. Данные должны храниться только в оперативной памяти. Запрещается использовать любые хранилища и базы данных. 
3. Покрытие кода unit тестами должно быть не меньше 30%. 
4. В качестве сборщика нужно использовать Maven. 
5. Исходники предоставить в git репозитории.

