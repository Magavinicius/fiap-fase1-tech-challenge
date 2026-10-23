# Tech Challenge - FIAP (Fase 1)

API REST de cadastro e gerenciamento de usuários, feita em **Java 21 + Spring Boot** com **MySQL**,
organizada em Clean Architecture (`domain`, `application`, `infrastructure`).

Funcionalidades principais (`/api/v1/user`):

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/api/v1/user/` | Cria um usuário |
| GET | `/api/v1/user/` | Lista usuários (paginado) |
| GET | `/api/v1/user/{id}` | Busca usuário por ID |
| GET | `/api/v1/user/find-by-name` | Busca usuários por nome |
| PUT | `/api/v1/user/{id}` | Atualiza um usuário |
| PATCH | `/api/v1/user/change-password/{id}` | Troca a senha |
| DELETE | `/api/v1/user/{id}` | Remove um usuário |
| POST | `/api/v1/user/auth-login` | Valida login e senha |

## Pré-requisitos

- Docker e Docker Compose
- (opcional, para rodar sem Docker) JDK 21 — o Maven vem junto via `./mvnw`

## Configuração do ambiente

Assim que baixar o repositório, crie o arquivo `local.env` a partir do arquivo de exemplo
`local.env.sample` — basta copiar/renomear:

```bash
cp local.env.sample local.env
```

Depois preencha as informações com as credenciais necessárias:

| Variável | Descrição |
| --- | --- |
| `MYSQL_ROOT_PASSWORD` | Senha do usuário **root** do banco |
| `MYSQL_DATABASE` | Nome do banco de dados |
| `SPRING_DATASOURCE_USERNAME` | Usuário da aplicação no banco |
| `SPRING_DATASOURCE_PASSWORD` | Senha desse usuário |

> O `local.env` não é versionado (está no `.gitignore`), pois contém credenciais.

## Subindo a aplicação

```bash
docker compose --env-file local.env up --build
```

Isso sobe dois containers:

- `mysql-db` — MySQL 8.4 na porta `3307`
- `spring-app` — a API na porta `8080` (só inicia depois que o banco está saudável)

As tabelas são criadas automaticamente pelo Hibernate (`ddl-auto=update`).

Para parar:

```bash
docker compose down
```

Para parar e apagar também os dados do banco:

```bash
docker compose down -v
```

## Acessando

- API: http://localhost:8080/api/v1/user
- Swagger UI: http://localhost:8080/swagger-ui.html

## Rodando localmente sem Docker

Suba apenas o banco e rode a aplicação pelo Maven Wrapper:

```bash
docker compose --env-file local.env up db
./mvnw spring-boot:run
```

> Nesse caso, ajuste `spring.datasource.*` em `src/main/resources/application.properties`
> para apontar para `localhost` com as credenciais do seu `local.env`.

## Testes

```bash
./mvnw test
```

## Postman

A pasta `postman/` contém as collections e o environment para testar os endpoints
(inclusive cenários de erro).
