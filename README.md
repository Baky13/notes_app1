# Notes App

Сервис заметок с JWT-авторизацией на Spring Boot 3.4

## Стек технологий

- Java 21
- Spring Boot 3.4
- Gradle (Kotlin DSL)
- PostgreSQL
- Flyway (миграции)
- Spring Security + JWT
- Spring Data JPA
- Lombok
- Swagger/OpenAPI

## Запуск приложения

### 1. Настройка базы данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE notes_db;
```

### 2. Настройка конфигурации

Отредактируйте `src/main/resources/application.yaml` при необходимости:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notes_db
    username: postgres
    password: postgres
```

### 3. Сборка и запуск

```bash
# Установка JAVA_HOME (пример для Windows)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun

# Или запуск JAR файла
java -jar build/libs/notes-app-0.0.1-SNAPSHOT.jar
```

## Эндпоинты API

### Управление заметками

- `POST /api/notes` - Создать заметку
- `GET /api/notes` - Получить все заметки (с пагинацией)
- `GET /api/notes/{id}` - Получить заметку по ID
- `PUT /api/notes/{id}` - Обновить заметку
- `DELETE /api/notes/{id}` - Удалить заметку
- `GET /api/notes/search?query={text}` - Поиск заметок

### Параметры пагинации

```bash
GET /api/notes?page=0&size=10&sort=createdAt,desc
```

### Документация API

Swagger UI доступен по адресу: http://localhost:8080/swagger-ui.html

## Структура проекта

```
src/main/java/com/example/notesapp/
├── entity/          # Сущности JPA
├── repository/      # Репозитории Spring Data
├── service/         # Слой бизнес-логики
├── controller/      # REST контроллеры
├── dto/            # Объекты передачи данных
├── exception/      # Обработка исключений
├── security/       # Конфигурация безопасности
└── config/         # Конфигурации Spring

src/main/resources/
├── db/migration/   # Flyway миграции
└── application.yaml # Конфигурация приложения
```

## Валидация

- Заголовок заметки: обязателен, до 200 символов
- Содержимое заметки: до 10000 символов
- Имя пользователя: уникальное, до 50 символов
- Email: уникальный, валидный формат

## Тестирование

```bash
# Запуск тестов
./gradlew test

# Запуск с покрытием
./gradlew test jacocoTestReport
```

## Особенности реализации

- Используются индексы для оптимизации запросов
- Реализован полнотекстовый поиск
- Конструкторная инъекция зависимостей
- Валидация на уровне DTO
- Обработка ошибок через @ControllerAdvice
- Автоматические миграции через Flyway
