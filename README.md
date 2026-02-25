# To-do List Manager
MVP on Spring Framework

## Profiles (dev / prod)

В проекте добавлены профильные конфиги:
- `dev` (подробнее логирование, порт 8081)
- `prod` (production-настройки, порт 8080)

Активировать профиль можно так:

```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"
```
