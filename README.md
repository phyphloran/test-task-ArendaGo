# Task Catalog API

REST-сервис для управления задачами.

## Описание
Проект реализует backend с архитектурой:
- Controller
- Service
- Repository

Доступ к данным выполнен через Spring JDBC `JdbcClient` и native SQL.
Сервисный слой использует реактивные типы Reactor (`Mono`).

## Стек
- Kotlin 1.9
- Java 21
- Spring Boot 3.5
- Spring Web MVC
- Project Reactor
- Spring JDBC (`JdbcClient`)
- PostgreSQL 16
- Flyway
- Swagger / OpenAPI (`springdoc`)
- Maven
- JUnit 5 + Mockito + Reactor Test

## Структура проекта
`src/main/kotlin/test_task_ArendaGo`
- `controller` — HTTP-эндпоинты, валидация входных параметров
- `service` — бизнес-логика и orchestration
- `repository` — SQL-запросы, пагинация, фильтрация
- `model` — доменные модели
- `dto` — запросы/ответы API
- `mapper` — преобразование entity <-> DTO
- `exception` — централизованная обработка ошибок

`src/main/resources`
- `application.yaml` — конфигурация приложения
- `db/migration` — миграции Flyway

## API
Базовый префикс: `/api/v1/tasks`

- `POST /api/v1/tasks` — создать задачу
- `GET /api/v1/tasks?page=0&size=10&status=NEW` — список задач с пагинацией и фильтром по статусу
- `GET /api/v1/tasks/{id}` — получить задачу по id
- `PATCH /api/v1/tasks/{id}/status` — обновить только статус
- `DELETE /api/v1/tasks/{id}` — удалить задачу

Статусы задач:
- `NEW`
- `IN_PROGRESS`
- `DONE`
- `CANCELLED`

## Валидация
- `title` обязателен
- длина `title`: от 3 до 100
- `page >= 0`
- `1 <= size <= 100`

## Обработка ошибок
Используется централизованный `@RestControllerAdvice`.

Примеры:
- задача не найдена — `404 Not Found`
- ошибки валидации — `400 Bad Request`
- непредвиденная ошибка — `500 Internal Server Error`

## Переменные окружения
Используются и приложением, и Docker Compose:
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_PORT`
- `APP_PORT`
- `DB_DRIVER`
- `DB_URL`

Файл с примером значений: [.env](C:/Users/phyphloran/Desktop/test-task-ArendaGo/.env)

## Запуск через Docker Compose (рекомендуемый способ)
Поднимает PostgreSQL и backend-сервис.

1. Перейти в корень проекта.
2. При необходимости отредактировать `.env`.
3. Запустить:

```bash
docker compose up --build -d
```

4. Проверить статус контейнеров:

```bash
docker compose ps
```

Ожидаемый результат:
- `task-catalog-postgres` в статусе `healthy`
- `task-catalog-app` в статусе `Up`

5. Проверить документацию API:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

Остановка сервисов:

```bash
docker compose down
```

Остановка с удалением volume БД:

```bash
docker compose down -v
```

Просмотр логов:

```bash
docker compose logs -f app
docker compose logs -f postgres
```

## Локальный запуск без Docker
Требования:
- доступный PostgreSQL
- корректно заданные env-переменные

Запуск:

```bash
./mvnw spring-boot:run
```

Для Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Миграции
Flyway применяет миграции автоматически при старте приложения.

Текущая миграция:
- [V1__create_tasks.sql](C:/Users/phyphloran/Desktop/test-task-ArendaGo/src/main/resources/db/migration/V1__create_tasks.sql)

## Тесты
Запуск всех тестов:

```bash
./mvnw test
```

Для Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Примечание: интеграционные тесты репозитория используют H2 только в test scope.

## SQL-логи
В `application.yaml` включено логирование SQL для Spring JDBC:
- `org.springframework.jdbc.core: DEBUG`
- `org.springframework.jdbc.core.StatementCreatorUtils: TRACE`
