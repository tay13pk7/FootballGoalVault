# GoalVault

GoalVault is a learning project built with **Spring Boot** (a popular Java framework for building web applications and APIs). You don't need any prior Spring experience to understand this document — it starts from the basics and gets progressively more technical.

---

## Contents
- [What is this project?](#what-is-this-project)
- [The domain: what is GoalVault actually about?](#the-domain-what-is-goalvault-actually-about)
- [Key concepts for complete beginners](#key-concepts-for-complete-beginners)
- [What's inside the box (tech stack)](#whats-inside-the-box-tech-stack)
- [Prerequisites](#prerequisites)
- [Running the app](#running-the-app)
- [Profiles](#profiles)
- [H2 console](#h2-console)
- [Actuator endpoints](#actuator-endpoints)
- [Curl examples](#curl-examples)
- [Project layout, in plain English](#project-layout-in-plain-english)
- [Annotation reference](#annotation-reference)

---

## What is this project?

This is **not** a real production product — it is a teaching sandbox. The author built a small but realistic web application and deliberately packed it with examples of (almost) every important feature the Spring framework offers, each one commented so it can be looked up again later like a personal cheat sheet.

In short: it's a backend API server. It has no visual website/front-end — instead, you talk to it using HTTP requests (the same kind of requests your browser makes when it loads a web page), and it replies with data in **JSON** format (a simple, structured text format used almost everywhere on the web, e.g. `{"name": "Arsenal", "city": "London"}`).

## The domain: what is GoalVault actually about?

"Domain" just means *the subject matter the software is modeling* — i.e., what real-world things it keeps track of. GoalVault's domain is **football (soccer) league management**. It behaves like a simplified back-office system for a football league, keeping track of:

| Concept | What it represents |
|---|---|
| **Team** | A football club — name, home city, the year it was founded, and its home stadium. |
| **Player** | A person who plays for a team — name, shirt/jersey number, playing position (e.g. forward, defender), and goals scored. |
| **Coach** | The person who manages/trains a team. |
| **Match** | A game between two teams on a given date, with a final score and the stadium it was played at. |
| **Fan** | A supporter who can follow one or more teams. |
| **Match Review** | A fan's written review/rating of a specific match (like a product review, but for a football match). |

The API lets a client application (a website, a mobile app, or just you using `curl`) **create, read, update, and delete** (often abbreviated "CRUD") all of the above, plus some extra real-world-style features: searching/filtering players, paginating long lists, versioned API responses, caching, scheduled background jobs (e.g. an hourly league summary), and login-protected admin actions.

## Key concepts for complete beginners

If terms like "API", "backend", or "framework" are new to you, here's a 60-second primer:

- **API (Application Programming Interface)**: A set of URLs a program exposes so other programs (or you, via tools like `curl` or Postman) can ask it to do things — e.g. "give me team #1" or "create a new player." You don't need a browser UI to use it.
- **REST API**: A common style of API where each URL represents a "thing" (a team, a player) and the HTTP method (`GET`, `POST`, `PUT`, `DELETE`) says what you want to do with it. This project is a REST API.
- **JSON**: The text format used to send and receive data, e.g. `{"id": 1, "name": "Arsenal"}`.
- **Server**: A program that sits running in the background, listening for incoming requests, and replying to them. Running this project starts a server on your own computer.
- **Framework (Spring / Spring Boot)**: A large, pre-built toolkit that handles the repetitive, "plumbing" parts of building a server (handling web requests, talking to a database, security, etc.) so the developer can focus on the actual business logic. Spring Boot is the "batteries included, get started fast" flavor of the Spring framework.
- **Database**: Where the data (teams, players, matches...) is actually stored. This project uses **H2**, a tiny database that lives entirely in memory — meaning all data resets every time you restart the app. That's intentional, since this is for learning, not for permanent storage.
- **Maven**: The build tool used to compile the Java code and download its dependencies (the libraries it relies on). You don't install Maven yourself — this project ships with a wrapper script (`mvnw`) that downloads the correct version automatically.

## What's inside the box (tech stack)

| Piece | Role |
|---|---|
| **Java 17** | The programming language the project is written in. |
| **Spring Boot 3.3** | The framework providing web server, dependency injection, data access, security, etc. |
| **Embedded Tomcat** | The actual web server that runs inside the app — no separate installation needed. |
| **H2 (in-memory database)** | Stores all the data while the app is running; wiped on restart. |
| **Spring Data JPA** | Lets the code talk to the database using plain Java objects instead of hand-written SQL. |
| **Spring Security** | Protects certain endpoints (e.g. admin actions) behind a username/password. |
| **Lombok** | A code-generation helper that removes Java boilerplate (like getters/setters). |
| **Maven** | Builds the project and manages its dependencies. |

## Prerequisites

Before running the app, install:
1. **Java Development Kit (JDK) 17 or newer** — [Adoptium Temurin](https://adoptium.net/) is a good free option.
2. That's it. Maven itself is *not* required — use the included `mvnw` / `mvnw.cmd` wrapper scripts instead.

Check your Java version with:
```bash
java -version
```

---

## Running the app

```bash
# Dev profile (default). Seeds ~15 teams, 40 players, 150 matches, 300 reviews.
./mvnw spring-boot:run

# Explicit dev
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"

# Prod profile — minimal seed, H2 console disabled, WARN log root
./mvnw spring-boot:run "-Dspring-boot.run.profiles=prod"

# Run the test suite (all four test types)
./mvnw test
```

App URL: `http://localhost:8083`

## Profiles

| Profile | Purpose | Seeded by |
|---|---|---|
| `dev` (default) | Full learning dataset, H2 console open, verbose logs. | `DataSeeder` (`@Profile("dev")`) |
| `prod`          | Placeholder seed only, H2 console disabled, WARN root log. | `ProdDataSeeder` (`@Profile("prod")`) |

## H2 console

Visit `http://localhost:8083/h2-console`.

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:goalvault` |
| Username | `sa` |
| Password | *(empty)* |

## Actuator endpoints

Exposed under `/actuator/**`. Health/info are open; the rest require Basic Auth as an admin.

| Endpoint | What it's for when debugging a real app |
|---|---|
| `/actuator/health`   | Liveness / readiness. Wired to k8s probes and LB checks. |
| `/actuator/info`     | Human-readable identity: app name, description, version. "Which build am I hitting?" |
| `/actuator/metrics`  | Micrometer counters/gauges: JVM, HTTP, cache, DB pool. Starting point for perf work. |
| `/actuator/beans`    | Full Spring bean graph. Answers "is my custom bean actually being wired?" |
| `/actuator/mappings` | Registered request path → handler map. Answers "why is my URL 404-ing?" |

Curl examples:

```bash
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/info
curl -u admin:admin http://localhost:8083/actuator/metrics
curl -u admin:admin http://localhost:8083/actuator/beans
curl -u admin:admin http://localhost:8083/actuator/mappings
```

## Curl examples

### Meta

```bash
curl http://localhost:8083/api/v1/meta
```

### Teams — CRUD, versioning, cache demos

```bash
# List (paginated)
curl "http://localhost:8083/api/v1/teams?page=0&size=5&sort=name"

# Get v1 shape (default)
curl http://localhost:8083/api/v1/teams/1

# Header-based versioning: v2 adds playerCount + averageTeamRating
curl -H "X-API-Version: 2" http://localhost:8083/api/v1/teams/1

# Create
curl -X POST http://localhost:8083/api/v1/teams \
  -H "Content-Type: application/json" \
  -d '{"name":"NewFC","city":"Testville","foundedYear":2001,"stadiumName":"NewGround"}'

# Update  (evicts the cached team)
curl -X PUT http://localhost:8083/api/v1/teams/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Renamed","city":"London","foundedYear":1900,"stadiumName":"North Park Arena"}'

# Rename stadium (@CachePut demo — cache is refreshed with the returned value)
curl -X PATCH "http://localhost:8083/api/v1/teams/1/stadium?name=Newer%20Ground"

# Delete
curl -X DELETE http://localhost:8083/api/v1/teams/99
```

### Players — pagination, dynamic filtering, bulk, custom action

```bash
# Pagination + sort
curl "http://localhost:8083/api/v1/players?page=0&size=5&sort=goalsScored,desc"

# Dynamic filter (any subset of teamId / position / minGoals / q may be omitted)
curl "http://localhost:8083/api/v1/players?position=FORWARD&teamId=3&minGoals=10"

# Create
curl -X POST http://localhost:8083/api/v1/players \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","position":"FORWARD","jerseyNumber":9,"teamId":1,"goalsScored":0}'

# Bulk create (partial success — see per-item result array)
curl -X POST http://localhost:8083/api/v1/players/bulk \
  -H "Content-Type: application/json" \
  -d '[{"name":"Ok","position":"FORWARD","jerseyNumber":11,"teamId":1,"goalsScored":0},
       {"name":"","position":"WRONG","jerseyNumber":0,"teamId":9999,"goalsScored":-1}]'

# Custom action: score a goal
curl -X POST http://localhost:8083/api/v1/players/1/score
```

### Matches — idempotency + async notification

```bash
# Create (idempotency-key header optional; same key -> same body, no duplicate row)
curl -X POST http://localhost:8083/api/v1/matches \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 6f8a2c3d-e4b1-4a92-9f61-abc123456789" \
  -d '{"homeTeamId":1,"awayTeamId":2,"matchDate":"2025-09-01T15:00:00","homeScore":2,"awayScore":1,"stadium":"North Park Arena"}'

# List by team or stadium
curl "http://localhost:8083/api/v1/matches?teamId=1"
curl "http://localhost:8083/api/v1/matches?stadium=North%20Park%20Arena"

# Get / Update / Delete
curl http://localhost:8083/api/v1/matches/1
curl -X PUT http://localhost:8083/api/v1/matches/1 \
  -H "Content-Type: application/json" \
  -d '{"homeTeamId":1,"awayTeamId":2,"matchDate":"2025-09-01T15:00:00","homeScore":3,"awayScore":1,"stadium":"North Park Arena"}'
curl -X DELETE http://localhost:8083/api/v1/matches/1
```

### Coaches

```bash
curl "http://localhost:8083/api/v1/coaches?page=0&size=5"
curl http://localhost:8083/api/v1/coaches/1
```

### Admin (HTTP Basic auth)

Two seeded users:

| Username | Password  | Role  |
|----------|-----------|-------|
| `user`   | `password`| USER  |
| `admin`  | `admin`   | ADMIN |

```bash
# @PreAuthorize("hasRole('ADMIN')") — clear the "teams" cache
curl -u admin:admin -X DELETE http://localhost:8083/api/v1/admin/cache/teams

# @Secured("ROLE_ADMIN") — admin ping
curl -u admin:admin -X POST http://localhost:8083/api/v1/admin/ping

# Non-admin -> 403
curl -u user:password -X POST http://localhost:8083/api/v1/admin/ping
```

---

## Annotation reference

The single most-useful table in this README. Each annotation is used somewhere in the codebase — find it in the listed file and read the accompanying comment.

### Dependency Injection

| Annotation | Used in | One-liner |
|---|---|---|
| `@SpringBootApplication` | `BootApplication.java` | Meta-annotation = `@Configuration + @EnableAutoConfiguration + @ComponentScan`. |
| `@Component` | `DataSeeder.java`, `LifecycleDemoBean.java`, `LeagueScheduler.java`, `PrototypeGreeting.java` | Generic stereotype; makes a class a Spring bean. |
| `@Service` | `TeamServiceImpl.java`, `NotificationService.java`, `IdempotencyService.java` | Component variant for the business layer. |
| `@Repository` | `TeamRepository.java`, `PlayerRepository.java` | Component variant for persistence; translates JPA exceptions. |
| `@RestController` | `TeamController.java`, `AdminController.java`, `MetaController.java` | `@Controller + @ResponseBody`; every handler returns JSON. |
| `@Configuration` | `WebConfig.java`, `SecurityConfig.java`, `AppInfoConfig.java` | Class supplies `@Bean` definitions. |
| `@Bean` | `WebConfig.java`, `SecurityConfig.java`, `AppInfoConfig.java` | Registers the method's return value as a Spring bean. |
| `@Autowired` (constructor) | Everywhere via constructor injection — commented in `TeamServiceImpl.java` | Optional on a single-constructor class since Spring 4.3. Preferred DI style. |
| `@Autowired` (field) | `MatchServiceImpl.java` | Field injection example with a comment listing its downsides. |
| `@Autowired` (setter) | `CoachServiceImpl.java` | Setter injection for an optional dependency. |
| `@Value` | `AppInfoConfig.java` | Injects a single property with a default fallback. |
| `@Qualifier` | *(not needed here — only one bean per type)* | Selects a specific bean when multiple exist. |

### Web

| Annotation | Used in | One-liner |
|---|---|---|
| `@RequestMapping` | Every controller | Base path for the class, or generic path/method mapping. |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@PatchMapping` / `@DeleteMapping` | All controllers | HTTP-method-specific shortcuts for `@RequestMapping`. |
| `@PathVariable` | `TeamController.java` et al. | Binds a `{placeholder}` in the URL to a method parameter. |
| `@RequestParam` | `PlayerController.java` et al. | Binds a query-string parameter. |
| `@RequestBody` | `TeamController.java` et al. | Deserialises the JSON body into a DTO. |
| `@RequestHeader` | `TeamController.java` (`X-API-Version`), `MatchController.java` (`Idempotency-Key`) | Reads an HTTP header. |
| `@ResponseBody` | Implicit via `@RestController` | Serialises the return value as JSON. |
| `@PageableDefault` | `TeamController.java`, `PlayerController.java`, `MatchController.java` | Default page size / sort for Spring Data `Pageable`. |
| `@RestControllerAdvice` | `GlobalExceptionHandler.java` | Global JSON exception handler (RFC 7807 ProblemDetail). |
| `@ExceptionHandler` | `GlobalExceptionHandler.java` | Handler for a specific exception type. |

### Data / JPA

| Annotation | Used in | One-liner |
|---|---|---|
| `@Entity` / `@Table` | `Team.java`, `Player.java`, `Coach.java`, `Match.java`, `Fan.java`, `MatchReview.java` | Marks a persistent class + configures its table. |
| `@Id` / `@GeneratedValue` | Every entity | Primary key + generation strategy. |
| `@Column` / `@JoinColumn` | Entities | Column mapping / FK column mapping. |
| `@ManyToOne`, `@ManyToMany`, `@OneToMany` | `Player.java`, `Coach.java`, `Fan.java`, `Match.java` | Relationship mapping. |
| `@Transactional` | Service impls | Runs the method in a DB transaction (`readOnly=true` for reads). |
| `@Query` | `MatchReviewRepository.java`, `PlayerRepository.java` | JPQL query when derived method names aren't enough. |
| `@Param` | Same | Names a parameter for the `@Query`. |

### Validation

| Annotation | Used in | One-liner |
|---|---|---|
| `@Valid` | Controllers on `@RequestBody` params | Triggers Bean Validation on the DTO. |
| `@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@Max`, `@Positive`, `@PositiveOrZero` | `TeamRequest.java`, `PlayerRequest.java`, `MatchRequest.java` | Standard Jakarta Validation constraints. |
| `@ValidPosition` | `PlayerRequest.java` | Custom constraint defined in `validation/`. |
| `@Constraint`, `@Target`, `@Retention` | `ValidPosition.java` | Meta-annotations for defining a custom constraint. |

### AOP

| Annotation | Used in | One-liner |
|---|---|---|
| `@Aspect` | `LoggingAspect.java` | Marks a class as an AspectJ aspect. |
| `@Pointcut` | `LoggingAspect.java` | Reusable named expression that selects join points. |
| `@Around` | `LoggingAspect.java` | Wraps the target method; controls whether/when to `proceed()`. |
| `@Before` | `LoggingAspect.java` | Runs before the target method; can log/audit but not alter args. |
| `@AfterThrowing` | `LoggingAspect.java` | Runs only when the target method exits by throwing. |

### Caching

| Annotation | Used in | One-liner |
|---|---|---|
| `@EnableCaching` | `BootApplication.java` | Turns on the cache abstraction. |
| `@Cacheable` | `TeamServiceImpl.get` | Memoise the return value by key. |
| `@CachePut`  | `TeamServiceImpl.updateStadium` | Always run the method, then write the return value into the cache. |
| `@CacheEvict` | `TeamServiceImpl.update`, `.delete` | Remove entries so the next read re-populates from the DB. |

### Security

| Annotation | Used in | One-liner |
|---|---|---|
| `@EnableWebSecurity` | `SecurityConfig.java` | Bootstraps Spring Security's filter chain. |
| `@EnableMethodSecurity` | `SecurityConfig.java` | Turns on annotation-driven method security (`securedEnabled=true` also activates `@Secured`). |
| `@PreAuthorize` | `AdminController.clearTeamCache` | SpEL-based method authorization ("hasRole('ADMIN')"). |
| `@Secured` | `AdminController.adminPing` | Legacy role-string method authorization. |

### Testing

| Annotation | Used in | One-liner |
|---|---|---|
| `@SpringBootTest` | `GoalVaultIntegrationTest.java`, `BootApplicationTests.java` | Boots the entire ApplicationContext for integration tests. |
| `@AutoConfigureMockMvc` | `GoalVaultIntegrationTest.java`, `TeamControllerWebMvcTest.java` | Adds a wired `MockMvc` bean. |
| `@WebMvcTest` | `TeamControllerWebMvcTest.java` | Slice test: just the MVC layer for the named controller. |
| `@DataJpaTest` | `PlayerRepositoryTest.java` | Slice test: JPA + embedded H2 only. |
| `@MockBean` | `TeamControllerWebMvcTest.java` | Replaces a bean with a Mockito mock inside the Spring context. |
| `@ExtendWith(MockitoExtension.class)` | `TeamServiceImplUnitTest.java` | Pure JUnit + Mockito; no Spring context. |
| `@Mock`, `@InjectMocks` | `TeamServiceImplUnitTest.java` | Mockito wiring for a plain unit test. |
| `@Test` | Every test class | Marks a JUnit 5 test method. |

### Lifecycle / Scopes

| Annotation | Used in | One-liner |
|---|---|---|
| `@PostConstruct` | `LifecycleDemoBean.java` | Runs once after dependency injection. |
| `@PreDestroy`   | `LifecycleDemoBean.java` | Runs on graceful container shutdown. |
| `@Scope("prototype")` | `PrototypeGreeting.java` | Fresh bean instance per lookup; contrast with the default singleton scope. |

### Conditional / Profile

| Annotation | Used in | One-liner |
|---|---|---|
| `@Profile("dev")` / `@Profile("prod")` | `DataSeeder.java`, `ProdDataSeeder.java` | Bean is only registered when the given profile is active. |
| `@ConditionalOnProperty` | `WebConfig.rateLimitInterceptor` | Bean only created when `goalvault.rate-limit.enabled=true`. |
| `@ConditionalOnMissingBean` | `WebConfig.systemClock` | Bean only created if no other bean of that type exists. |
| `@ConfigurationProperties` | `AppMetadata.java`, `RateLimitProperties.java` | Binds a typed POJO to a chunk of external config. |
| `@EnableConfigurationProperties` | `WebConfig.java` | Activates one or more `@ConfigurationProperties` classes as beans. |

### Async / Scheduling

| Annotation | Used in | One-liner |
|---|---|---|
| `@EnableAsync` | `BootApplication.java` | Activates `@Async` proxy support. |
| `@Async` | `NotificationService.sendMatchNotification` | Method runs on a task executor instead of the caller thread. |
| `@EnableScheduling` | `BootApplication.java` | Activates `@Scheduled` task detection. |
| `@Scheduled(fixedRate)` | `LeagueScheduler.logLeagueSummary` | Fires every N ms measured from previous start. |
| `@Scheduled(cron)` | `LeagueScheduler.hourlyCleanupCheck` | Fires on a cron schedule ("0 0 * * * *" = hourly). |

### Lombok (already in use)

| Annotation | Used in | One-liner |
|---|---|---|
| `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` | Every JPA entity | Boilerplate reduction — generates accessors and constructors at compile time. |

---

## Project layout, in plain English

The Java source code lives under `src/main/java/com/spring/learn/boot/goalvault/`. Each folder groups files by what they *do*, a common pattern in Spring apps:

- **`controller/`** — the "front desk". These files define the actual URLs (e.g. `/api/v1/teams`) and what HTTP method they respond to. This is the only layer that talks directly to the outside world.
- **`service/`** — the "business logic". Decides *what should happen* when, e.g., a new player is created (validation, cache updates, notifications).
- **`repository/`** — the "data access" layer. Talks to the H2 database to save/load/search records, without needing to write raw SQL.
- **`entity/`** — plain Java classes that map 1:1 to database tables (Team, Player, Coach, Match, Fan, MatchReview).
- **`dto/`** — "Data Transfer Objects": the shapes of the JSON actually sent/received over the API — kept separate from `entity/` so the database structure and the public API can evolve independently.
- **`config/`**, **`security/`**, **`exception/`**, **`validation/`**, **`aop/`**, **`scheduler/`**, **`notification/`**, **`lifecycle/`** — supporting infrastructure (configuration, login/roles, error handling, input validation, logging, background jobs, async email-style notifications, startup/shutdown hooks).

A typical request flows: **browser/curl → controller → service → repository → H2 database**, and the response flows back the same way in reverse, converted into JSON along the way.

Full breakdown:

```
com.spring.learn.boot
└── goalvault
    ├── aop/           — LoggingAspect  (@Aspect, @Pointcut, @Around, @Before, @AfterThrowing)
    ├── config/        — Web, seed, ConfigurationProperties, ConditionalOn* examples
    ├── controller/    — REST endpoints (v1 URI-versioned; TeamController shows header versioning too)
    ├── dto/           — Request/response records, bulk result, TeamResponseV2
    ├── entity/        — JPA entities: Team, Player, Coach, Match, Fan, MatchReview
    ├── exception/     — Global RFC-7807 exception handling
    ├── lifecycle/     — @PostConstruct/@PreDestroy demo + @Scope("prototype") demo
    ├── notification/  — @Async NotificationService
    ├── repository/    — Spring Data JPA repositories + JPA Specifications
    ├── scheduler/     — @Scheduled fixedRate + cron examples
    ├── security/      — SecurityConfig, AdminController (@PreAuthorize + @Secured)
    ├── service/       — Service interfaces
    │   └── impl/      — Service implementations (Team/Player/Coach/Match + IdempotencyService)
    └── validation/    — Custom @ValidPosition constraint + validator
```
