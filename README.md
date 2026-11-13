# DRPP Solver

Веб-приложение для исследования и решения **Directed Rural Postman Problem (DRPP)** — задачи сельского почтальона на ориентированном графе.

Требуется найти замкнутый маршрут минимальной стоимости, проходящий по всем обязательным дугам хотя бы один раз. Необязательные дуги могут использоваться для соединения участков маршрута; повторные проходы допускаются и учитываются в стоимости.

Приложение позволяет генерировать графы, применять редукции Eiselt и Christofides и решать задачу с помощью целочисленной оптимизационной модели.

## Технологии

- Java 25
- Spring Boot и Vaadin
- Maven Wrapper
- JOM и GLPK — построение и решение оптимизационной модели
- Graphviz — визуализация графов
- Docker — сборка и запуск приложения

## Запуск через Docker

### 1. Скачать проект

```bash
git clone https://github.com/Denis1168/DRPP.git
cd DRPP
```

### 2. Собрать образ

```bash
docker build -t drpp-web:local .
```

### 3. Запустить контейнер

```bash
docker run -d --name drpp-web -p 127.0.0.1:8080:8080 drpp-web:local
```

После запуска приложение доступно по адресу:

**[http://localhost:8080](http://localhost:8080)**

Порт опубликован только на локальном интерфейсе компьютера.

### Управление контейнером

Просмотр логов:

```bash
docker logs -f drpp-web
```

Остановка:

```bash
docker stop drpp-web
```

Повторный запуск ранее созданного контейнера:

```bash
docker start drpp-web
```

### Обновление после изменения кода

Пересоберите образ и пересоздайте контейнер:

```bash
docker build -t drpp-web:local .
docker stop drpp-web
docker rm drpp-web
docker run -d --name drpp-web -p 127.0.0.1:8080:8080 drpp-web:local
```

## Настройка GLPK

Путь или имя нативной библиотеки GLPK задаётся переменной окружения:

```text
GLPK_LIBRARY
```

В Docker она установлена автоматически:

```text
GLPK_LIBRARY=libglpk.so.40
```

При запуске непосредственно в Windows укажите путь к установленной DLL GLPK. Например, в PowerShell:

```powershell
$env:GLPK_LIBRARY = "C:\Libraries\GLPK\glpk_4_65.dll"
```

При запуске из IDE переменную следует задать в конфигурации запуска приложения.

Если переменная не задана, код использует резервный путь:

```text
src/main/resources/glpk-4.65/w64/glpk_4_65.dll
```

Дистрибутив GLPK по этому пути не входит в репозиторий. Для запуска вне Docker необходимы отдельная установка GLPK, Java 25 и настройка Graphviz.

## Структура проекта

```text
src/main/java/
├── app/                 # Веб-интерфейс и запуск Spring Boot
├── graph/               # Модель графа и генератор
│   └── algorithms/      # Алгоритмы работы с графом
├── reducers/            # Редукции Eiselt и Christofides
├── solver/              # Оптимизационная модель и результаты
└── visualization/       # Визуализация через Graphviz

src/main/resources/      # Настройки и ресурсы приложения
Dockerfile               # Сборка и запуск Docker-образа
.dockerignore            # Исключения из контекста Docker
pom.xml                  # Зависимости и настройки Maven
```