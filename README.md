# Store Web Services

API REST de uma loja virtual desenvolvida com **Spring Boot** e **JPA / Hibernate**, seguindo o workshop do curso *Java COMPLETO* do Prof. Dr. Nelio Alves ([devsuperior.com.br](https://devsuperior.com.br)).

O projeto cobre o ciclo completo de uma aplicação back-end: modelo de domínio, arquitetura em camadas, banco de dados de teste (H2) populado automaticamente, CRUD de usuários e tratamento de exceções.

---

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem |
| Spring Boot 4.1.1 | Framework base |
| Spring Web MVC | Camada REST |
| Spring Data JPA / Hibernate | Persistência e ORM |
| H2 Database | Banco em memória (perfil `test`) |
| PostgreSQL (driver) | Dependência incluída no `pom.xml` (não utilizada no perfil atual) |
| Maven (Maven Wrapper) | Build e gerenciamento de dependências |
| Postman | Testes manuais dos endpoints |

---

## Arquitetura

O projeto é organizado em camadas lógicas, onde cada uma depende apenas da camada abaixo:

```
Application (cliente HTTP)
        │
        ▼
Resource Layer   → controladores REST (@RestController)
        │
        ▼
Service Layer    → regras de negócio e tratamento de exceções (@Service)
        │
        ▼
Data Access      → repositórios Spring Data (JpaRepository)

        Entities → modelo de domínio, usado por todas as camadas
```

### Estrutura de pacotes

```
src/main/java/com/ellen/store_web_services
├── StoreWebServicesApplication.java
├── config/
│   └── TestConfig.java              # Popula o banco no perfil "test"
├── entities/
│   ├── User.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Product.java
│   ├── Category.java
│   ├── enums/OrderStatus.java
│   └── pk/OrderItemPK.java          # Chave primária composta de OrderItem
├── repositories/                    # Interfaces JpaRepository
├── services/
│   └── exceptions/                  # ResourceNotFoundException, DatabaseException
└── resources/                       # Controladores REST
    └── exceptions/                  # StandardError, ResourceExceptionHandler
```

---

## Modelo de domínio

- **User**: cliente da loja (`id`, `name`, `email`, `phone`, `password`). Um usuário possui vários pedidos.
- **Order**: pedido (`id`, `moment`, `orderStatus`). Pertence a um usuário, possui vários itens e, opcionalmente, um pagamento. Expõe o valor `total`.
- **OrderItem**: item de pedido (`quantity`, `price`). Associação muitos-para-muitos entre `Order` e `Product` **com atributos extras**, usando a chave composta `OrderItemPK`. Expõe o valor `subTotal`.
- **Payment**: pagamento (`id`, `moment`). Associação um-para-um com `Order` (compartilhando a mesma chave via `@MapsId`).
- **Product**: produto (`id`, `name`, `description`, `price`, `imgUrl`). Pertence a várias categorias.
- **Category**: categoria (`id`, `name`). Associação muitos-para-muitos com `Product` através da tabela `tb_product_category`.
- **OrderStatus** (enum): `WAITING_PAYMENT(1)`, `PAID(2)`, `SHIPPED(3)`, `DELIVERED(4)`, `CANCELED(5)`.

### Principais conceitos de JPA aplicados

- `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = IDENTITY)`
- `@ManyToOne` / `@OneToMany` (usuário ↔ pedidos)
- `@ManyToMany` com `@JoinTable` (produto ↔ categoria)
- `@EmbeddedId` / `@Embeddable` para chave primária composta (`OrderItem`)
- `@OneToOne` com `@MapsId` e `cascade = ALL` (pedido ↔ pagamento)
- `@JsonIgnore` para evitar loops de serialização em associações bidirecionais
- Datas no padrão **ISO 8601** (`Instant`, formato `yyyy-MM-dd'T'HH:mm:ss'Z'`)

---

## Como executar

### Pré-requisitos

- JDK 17 ou superior
- (Opcional) Maven instalado — o projeto já inclui o Maven Wrapper

### Passos

```bash
# 1. Clonar o repositório
git clone https://github.com/Nellefb/workshop-springboot-jpa.git
cd workshop-springboot-jpa

# 2. Executar a aplicação
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

A aplicação sobe em `http://localhost:8080`.

### Perfil de teste e banco H2

O arquivo `application.properties` ativa o perfil `test`:

```properties
spring.profiles.active=test
spring.jpa.open-in-view=true
```

No perfil `test` (`application-test.properties`), a aplicação usa o **H2 em memória** e a classe `TestConfig` popula o banco automaticamente a cada inicialização com 2 usuários, 3 categorias, 5 produtos, 3 pedidos, 4 itens de pedido e 1 pagamento.

Console do H2 disponível em: **http://localhost:8080/h2-console**

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:testdb` |
| User | `sa` |
| Password | *(vazio)* |

> Como o banco é em memória, os dados são recriados a cada execução.

---

## Endpoints

| Recurso | Método | Endpoint | Descrição |
|---|---|---|---|
| Users | `GET` | `/users` | Lista todos os usuários |
| Users | `GET` | `/users/{id}` | Busca usuário por id |
| Users | `POST` | `/users` | Insere um novo usuário (retorna `201 Created`) |
| Users | `PUT` | `/users/{id}` | Atualiza nome, e-mail e telefone |
| Users | `DELETE` | `/users/{id}` | Remove um usuário (retorna `204 No Content`) |
| Orders | `GET` | `/orders` | Lista todos os pedidos |
| Orders | `GET` | `/orders/{id}` | Busca pedido por id (com itens, pagamento e total) |
| Products | `GET` | `/products` | Lista todos os produtos |
| Products | `GET` | `/products/{id}` | Busca produto por id |
| Categories | `GET` | `/categories` | Lista todas as categorias |
| Categories | `GET` | `/categories/{id}` | Busca categoria por id |

### Exemplos

**Inserir usuário** — `POST /users`

```json
{
  "name": "Bob Brown",
  "email": "bob@gmail.com",
  "phone": "977557755",
  "password": "123456"
}
```

**Atualizar usuário** — `PUT /users/3`

```json
{
  "name": "Bob Brown",
  "email": "bob@gmail.com",
  "phone": "977557755"
}
```

> O `PUT` atualiza apenas `name`, `email` e `phone`; a senha não é alterada.

**Buscar pedido** — `GET /orders/1`

Retorna o pedido com cliente, status, itens (cada um com produto, quantidade, preço e subtotal), pagamento e total.

---

## Tratamento de exceções

As exceções são tratadas globalmente por `ResourceExceptionHandler` (`@ControllerAdvice`), que devolve um JSON padronizado (`StandardError`):

| Situação | Exceção | Status HTTP |
|---|---|---|
| Id inexistente em consulta, atualização ou exclusão | `ResourceNotFoundException` | `404 Not Found` |
| Exclusão de registro com dependências (integridade referencial) | `DatabaseException` | `400 Bad Request` |

Exemplo de resposta de erro (`GET /users/99`):

```json
{
  "timestamp": "2026-10-06T21:40:02Z",
  "status": 404,
  "error": "Resource not found",
  "message": "Resource not found. Id: 99",
  "path": "/users/99"
}
```

---

## Progresso do workshop

- [x] Projeto Spring Boot criado
- [x] Entidade e resource `User`
- [x] H2, perfil de teste e JPA
- [x] Repositórios, injeção de dependência e carga inicial do banco
- [x] Camada de serviço e registro de componentes
- [x] `Order`, `Instant` e ISO 8601
- [x] Enum `OrderStatus`
- [x] `Category` e `Product`
- [x] Associação muitos-para-muitos com `@JoinTable`
- [x] `OrderItem` (muitos-para-muitos com atributos extras)
- [x] `Payment` (um-para-um)
- [x] Métodos `subTotal` e `total`
- [x] CRUD de `User` (insert, delete, update)
- [x] Tratamento de exceções (findById, delete, update)
- [ ] Deploy no Heroku com PostgreSQL *(etapa opcional, não realizada)*

---

## Autora

**Ellen** — [@Nellefb](https://github.com/Nellefb)

Projeto desenvolvido para fins de estudo, com base no curso *Java COMPLETO* de Nelio Alves ([devsuperior.com.br](https://devsuperior.com.br)). Repositório de referência do curso: [acenelio/workshop-springboot4-jpa](https://github.com/acenelio/workshop-springboot4-jpa).
