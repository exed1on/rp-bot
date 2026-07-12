# Backend Engineer Interview Prep — Aristocrat & Storware (Java / Spring, Mid-Level)

> Target: **mid-level Java/Spring backend** roles at **Aristocrat** (gaming technology) and **Storware** (data backup & recovery, Warsaw/Poland).
> Coverage is balanced: company-specifics + Java core, JVM/GC, concurrency, collections, Spring/Spring Boot, REST, JPA/Hibernate, SQL, databases, system design, and behavioral.
>
> **How to use this:** Skim Part 1 (company specifics) first so you know what each company emphasizes. Then drill Parts 2–8 — these are the questions that actually get asked. Aim to be able to *say the answer out loud* in 30–60 seconds, not just recognize it. Use the checklist at the end to track readiness.
>
> **Sourcing note:** Company sections are built from real candidate reports (GeeksforGeeks interview experiences), live job postings (BuiltIn), and the companies' own careers pages. Where a fact could not be verified from a primary source it's labeled *[inference]*. General Q&A is compiled from Baeldung/GeeksforGeeks/InterviewBit/DigitalOcean/Jenkov/system-design-primer. Full source list at the bottom.

---

## Table of Contents

1. [Company-Specific Prep](#1-company-specific-prep)
   - [Aristocrat](#aristocrat)
   - [Storware](#storware)
2. [Java Core & OOP](#2-java-core--oop)
3. [JVM Internals & Garbage Collection](#3-jvm-internals--garbage-collection)
4. [Concurrency & Multithreading](#4-concurrency--multithreading)
5. [Collections Framework](#5-collections-framework)
6. [Spring, Spring Boot, REST & Microservices](#6-spring-spring-boot-rest--microservices)
7. [SQL, Databases, Caching & Messaging](#7-sql-databases-caching--messaging)
8. [System Design (Mid-Level)](#8-system-design-mid-level)
9. [Behavioral & HR](#9-behavioral--hr)
10. [Salary Expectations (Warsaw)](#10-salary-expectations-warsaw)
11. [2-Week Study Plan & Checklist](#11-2-week-study-plan--checklist)
12. [Sources](#12-sources)

---

## 1. Company-Specific Prep

### Aristocrat

**What they do:** Gaming technology — physical slot machines / EGMs (Electronic Gaming Machines), and Real Money Gaming (iLottery, iGaming, Online Sports Betting) plus social/mobile (Product Madness, Pixel United). They run a **Remote Gaming Platform (RGP)** that integrates third-party games.

**Interview process** (varies 4–6 stages by year/location/role):
1. Recruiter / profile screen
2. Online assessment — MCQs (polymorphism, STL/language concepts) + coding; sometimes via **BrainBench**. Reported as **time-pressured** ("maintain speed").
3. 2–3 technical rounds — **live coding** + deep language/OOP concepts + object-oriented design problems
4. Managerial / project-lead round
5. HR

> Note: most documented reports are from Aristocrat's India entity and skew C/C++ EGM roles & junior candidates, but the DSA/OOP/design bar applies broadly. Difficulty ≈ **3/5** (solid, not FAANG-bar). DSA described as "medium and hard," 3–4 questions, always discuss **time & space complexity**.

**Tech stack (from real job postings):** Two backend stacks exist depending on division.
- **Java stack** (most relevant to you): server-side **core Java 8+**, **Spring framework**, **MongoDB** (NoSQL), **Elasticsearch** + Kibana + Grafana, **REST and WebSockets**, Maven, GitLab, **Docker & Kubernetes**, **microservices**. Domain: integrating external games into the **Remote Gaming Platform (RGP)**.
- **C#/.NET stack:** C# .NET / .NET 6, ASP.NET MVC, Web API, Entity Framework, **MS-SQL Server** (schemas, stored procedures, triggers, indexes), **DDD, CQRS, Mediator pattern**, Angular front-end.

**Real reported coding/DSA questions:**
- Sort an array of 0s and 1s in **one scan** (Dutch national flag idea / two pointers).
- **Detect a loop in a linked list** (Floyd's cycle detection).
- **Reverse a linked list** — be ready for both *iterative and recursive*.
- Count the number of **leaves in a binary tree**.
- **Shuffle a deck of cards** programmatically (Fisher–Yates).
- **Count possible decodings** of a digit sequence (DP).
- **Array rotation** (reversal algorithm).
- Find **max j − i such that arr[j] > arr[i]**.
- **Count set bits** in a number.
- **Reverse the words** in a string ("hello mr X" → "X mr hello").
- **Find a repeating and missing number** (asked for the *optimal* solution).

**Real reported OOP/concept questions** (C++-flavored, but know the Java equivalents):
- Design patterns; implement a **Singleton**; discuss 3–4 patterns.
- **"Design a calculator — modular and object-oriented."**
- Implement a **generic Queue** class / implement a Set.
- vtable/vptr & virtual functions (Java equivalent: dynamic dispatch / method overriding), smart pointers (Java: GC + references), the diamond problem (Java: interfaces + default methods).

**Behavioral (reported):** "Why work for our company?", "How do you deal with criticism?", "How did you handle a disagreement with your manager?", "Describe an ideal work environment." They **probe genuine interest in gaming** — have a real reason ready.

**How to prep for Aristocrat:**
- Grind **medium LeetCode**: arrays, two-pointers, linked lists, trees, bit manipulation, DP basics. Always state complexity.
- Be fluent in **OO design** (patterns, "design a calculator / parking lot" style) and **Java fundamentals + Spring** (see Parts 2 & 6).
- Know **REST + WebSockets**, **MongoDB basics**, **microservices/Docker/K8s** at a conversational level.
- Have a crisp, sincere "why gaming / why Aristocrat" story.

---

### Storware

**What they do:** Polish (Warsaw) product company making **Storware Backup & Recovery** (formerly **vProtect**) — backup/restore for **VMs, containers, cloud, and Microsoft 365** across hypervisors (**VMware, oVirt/RHV, KVM, Proxmox, Nutanix AHV**, etc.).

**Interview process** (from their PL careers page — 5 named stages):
1. **Rozmowa telefoniczna** — phone screen
2. **Rozmowa kwalifikacyjna** — qualifying interview
3. **Sprawdzenie kompetencji** — competence/skills check (the technical task/interview happens here)
4. **Ustalenie warunków** — agreeing terms / offer
5. **Zaproszenie do zespołu** — join the team

> Culture (from careers page): **flat structure, product-oriented teams of 5–7**, **Scrum/Agile**, JIRA. Remote work, mentoring, skills-based raises. **Apply via HR@storware.eu** (they noted no open roles at the time of research — send CV directly). Polish CVs need the standard GDPR consent clause.

**Tech stack:**
- **Confirmed from careers page (tooling/infra):** TeamCity, SonarQube, **Kubernetes, Docker**, Bitbucket, JIRA, Confluence, Slack, CentOS, **AWS**, and virtualization platforms **oVirt, KVM, VMware, Proxmox**.
- *[inference, strongly supported by the product]:* The backend is **Java/Spring with a REST API** and an **agentless architecture** that talks to hypervisor APIs. Likely PostgreSQL/Mongo for metadata. The careers page does **not** explicitly list Java/Spring/DB versions, so confirm in the screen.

**Domain topics to understand** (no public interview questions found — these come from the product; expect questions in the competence check):
- **Snapshot-based backup** and **changed-block tracking (CBT) / incremental & differential backups**.
- **Agentless architecture** — interacting with hypervisor/cloud APIs instead of in-guest agents (trade-offs vs agent-based).
- **Deduplication & compression**, backup **repositories/storage backends**, retention policies (GFS — grandfather-father-son).
- **Restore workflows**, consistency (crash-consistent vs application-consistent backups), RPO/RTO.
- Talking to external APIs reliably: retries, idempotency, rate limits, large data transfers, streaming.

**How to prep for Storware:**
- Solid **Java + Spring Boot + REST + JPA** fundamentals (Parts 2 & 6) — this is the core.
- Be comfortable with **Docker/Kubernetes**, **AWS basics**, Linux/CentOS.
- Read up on **virtualization & backup concepts** above — showing domain curiosity matters at a focused product company.
- Be ready to discuss a feature end-to-end: API design, persistence, error handling, testing (they value SonarQube/quality).
- Since it's a small company, expect a **practical/pragmatic** interview and possibly a take-home or live coding task in the "competence check."

---

## 2. Java Core & OOP

**`==` vs `equals()`** — `==` compares references (for primitives, values); `equals()` compares logical content and is overridable. For Strings: two literals share the pool so `==` is true, but `new String("a") == "a"` is false. Always use `equals()` for content.

**`hashCode()`/`equals()` contract** — Equal objects *must* return the same `hashCode()`; unequal objects *may* collide. Override both together, because hash collections use `hashCode()` to pick the bucket and `equals()` to confirm within it — overriding only `equals()` breaks `HashMap`/`HashSet` lookups.

**String immutability & pool** — Strings are immutable for: pool sharing (memory), thread-safety, safe `HashMap` keys (cached hash stays valid), and security. The **string pool** caches unique literals; `intern()` returns the canonical pooled reference. `new String("x")` always allocates a new heap object.

**`final` vs `finally` vs `finalize`** — `final`: constant variable / non-overridable method / non-extendable class. `finally`: always-run cleanup block (skipped only on `System.exit()`/JVM crash). `finalize()`: deprecated GC hook — avoid; use try-with-resources/`Cleaner`.

**Checked vs unchecked exceptions** — Checked extend `Exception` (compiler-enforced, must catch/declare: `IOException`, `SQLException`) for recoverable conditions. Unchecked extend `RuntimeException` (`NPE`, `ArrayIndexOutOfBounds`) — usually bugs.

**Autoboxing & Integer cache** — Auto-conversion between primitives and wrappers. `Integer.valueOf()` caches `-128..127`, so `Integer a=127,b=127; a==b` is **true** but `200==200` (boxed) is **false** — always `.equals()` wrapper values. Unboxing a `null` wrapper throws NPE.

**Generics** — **Type erasure**: generic info removed at compile time (no `new T()`, no `instanceof List<String>`, no generic arrays). **Bounded types**: `<T extends Comparable<T>>`. **PECS**: Producer `extends`, Consumer `super` (`? extends` to read, `? super` to write).

**OOP principles** — **Encapsulation** (hide state behind accessors), **Inheritance** (is-a reuse), **Polymorphism** (overloading = compile-time, overriding = runtime/dynamic dispatch), **Abstraction** (expose behavior, hide implementation).

**Abstract class vs interface** — Abstract class: constructors, instance state, mix of concrete/abstract methods, single inheritance ("is-a"). Interface: contract, multiple inheritance of type, since Java 8 has `default`/`static` methods (Java 9 `private`), no instance state ("can-do"). Use abstract class for shared state/code among related classes; interface for capabilities across unrelated classes.

**`static`** — Belongs to the class, shared across instances, accessible without an instance. Static methods can't be overridden (they're hidden) and can't use `this`. **Static block** runs once at class load. Static nested class doesn't hold an outer reference; inner class does.

**Access modifiers** — `private` < default(package-private) < `protected` (package + subclasses) < `public`.

**Pass-by-value** — Java is **always** pass-by-value. For objects, the *reference value* is copied: you can mutate the object and the caller sees it, but reassigning the parameter doesn't affect the caller's reference.

---

## 3. JVM Internals & Garbage Collection

**JVM vs JRE vs JDK** — JDK ⊃ JRE ⊃ JVM. JDK = JRE + dev tools (javac, jar, debugger). JRE = JVM + core libraries. JVM = execution engine running bytecode. "Write once, run anywhere": bytecode is portable, only the JVM is platform-specific.

**Class loading** — **Loading** (read `.class`, create `Class` object) → **Linking** (Verify → Prepare statics with defaults → Resolve references) → **Initialization** (set static values, run static blocks). **ClassLoaders**: Bootstrap → Extension/Platform → Application/System. **Parent-delegation model** ("parent-first") prevents core classes from being overridden and avoids duplicates.

**Memory areas** — **Heap** (shared; all objects, GC'd), **Stack** (per-thread; method frames, locals, references), **Method Area/Metaspace** (class metadata, statics, runtime constant pool), **PC Register** (per-thread current instruction), **Native Method Stack**. **PermGen → Metaspace** (Java 8): Metaspace lives in **native memory** and grows dynamically (tune with `-XX:MaxMetaspaceSize`).

**Stack vs heap** — Stack: thread-local, small, fast, auto-freed per method, holds locals/references. Heap: shared, larger, slower, `new`-allocated, GC-reclaimed.

**Garbage collection** — Reclaims unreachable objects (reachability from **GC roots**). `System.gc()` is only a *request*. Eligible when: reference nulled/reassigned, out of scope, or "island of isolation."

**Generational heap** — **Young gen** (Eden + two Survivor spaces S0/S1) collected frequently (**Minor GC**); survivors get **promoted** to **Old/Tenured gen** (**Major GC**). **Full GC** = whole heap + metaspace (longest pauses). Based on "most objects die young." **Mark-Sweep-Compact**: mark reachable, sweep garbage, compact to remove fragmentation.

**Collectors** — **Serial** (single-thread, small apps), **Parallel** (throughput, default Java 8), **CMS** (low-pause, concurrent, deprecated/removed), **G1** (region-based, default since Java 9, predictable pauses on large heaps), **ZGC**/**Shenandoah** (ultra-low-pause, very large heaps). Trend: Serial → Parallel → CMS → G1 → ZGC/Shenandoah.

**Tuning flags** — `-Xms`/`-Xmx` (initial/max heap), `-Xss` (thread stack), `-Xmn` (young size), `-XX:MaxMetaspaceSize`, `-XX:MaxGCPauseMillis`, `-XX:+HeapDumpOnOutOfMemoryError`, `-XX:+PrintGCDetails`. Tip: set `-Xms == -Xmx` to avoid resizing. Tools: jstat, jmap, VisualVM.

**Errors** — `OutOfMemoryError: Java heap space` (leak / undersized heap), `: Metaspace` (too many classes/classloader leak), `: GC overhead limit exceeded` (~98% time in GC, <2% reclaimed). **StackOverflowError** = stack exhausted (deep recursion, tune `-Xss`) vs **OutOfMemoryError** = heap/metaspace exhausted. Both are `Error`s — don't catch.

**JIT** — Interpreter runs bytecode line-by-line (fast start, slow repeats); **JIT** compiles hot methods to native code and caches them. HotSpot has **C1** (fast compile, light optimization) and **C2** (slow compile, aggressive optimization); **tiered compilation** uses both.

---

## 4. Concurrency & Multithreading

**Thread lifecycle** — NEW → RUNNABLE → BLOCKED (waiting for monitor lock) → WAITING/TIMED_WAITING → TERMINATED. `start()` spawns a new thread (JVM calls `run()`); calling `run()` directly runs on the current thread. Calling `start()` twice → `IllegalThreadStateException`. **Daemon threads** don't keep the JVM alive.

**Runnable vs Callable** — `Runnable.run()` returns nothing, no checked exceptions. `Callable.call()` returns a result and can throw checked exceptions. `Future` = handle on an async result (`get()` blocks, `isDone()`, `cancel()`). Prefer implementing `Runnable` over extending `Thread`.

**`synchronized`** — Mutual exclusion + visibility. Synchronized **method** locks the whole object (or `Class` for static); synchronized **block** locks a chosen object on just the critical section (preferred). Object lock vs class lock are different locks (can run concurrently).

**`volatile`** — Guarantees **visibility** (reads/writes go to main memory) and prevents reordering (happens-before edge). **Not atomic** for compound ops — `count++` is read-modify-write, so use `synchronized`/`AtomicInteger` for that.

**wait/notify/notifyAll** — On `Object` (every object has a monitor); must be called inside a `synchronized` block (else `IllegalMonitorStateException`). `wait()` releases the lock; `sleep()` (static) does not. `notify()` wakes one, `notifyAll()` wakes all. **Always call `wait()` in a loop** (guard against spurious wakeups).

**Deadlock** — Circular wait over nested locks. Coffman conditions: mutual exclusion, hold-and-wait, no preemption, circular wait. **Avoid** with consistent global lock ordering, `tryLock` with timeout, fewer/shorter locks. **Detect** via thread dump (`jstack`). Related: **livelock** (active but no progress), **starvation** (never gets the lock).

**ExecutorService & thread pools** — Reusable worker threads pulling tasks from a queue (avoids per-task thread cost). Factories: `newFixedThreadPool`, `newCachedThreadPool`, `newSingleThreadExecutor`, `newScheduledThreadPool`, `newWorkStealingPool`. `execute()` (Runnable, void) vs `submit()` (Runnable/Callable, returns `Future`). Backed by a `BlockingQueue` (producer-consumer).

**ConcurrentHashMap** — Thread-safe with **fine-grained locking**: pre-Java 8 used segments; Java 8+ uses **CAS** on empty bins + `synchronized` on the bin head under contention. No null keys/values. Iterators are **weakly consistent** (no `ConcurrentModificationException`).

**CompletableFuture** — `Future` + `CompletionStage`: non-blocking chaining. `supplyAsync`/`runAsync`, `thenApply` (map value→value), `thenCompose` (flat-map when fn returns a future), `thenCombine` (combine two), `allOf`/`anyOf`, `exceptionally`/`handle`.

**Atomics & CAS** — `AtomicInteger`/`AtomicLong`/`AtomicReference`: lock-free thread-safe ops. **CAS** (compare-and-swap) updates only if the current value equals the expected — basis of optimistic concurrency. Faster than `synchronized` under low/moderate contention (no OS blocking). **ABA problem** → `AtomicStampedReference`.

**Locks** — `ReentrantLock` vs `synchronized`: both reentrant, but `ReentrantLock` adds `tryLock()` with timeout, `lockInterruptibly()`, fairness, multiple `Condition`s — but you must `unlock()` in `finally`. `ReadWriteLock`: many readers OR one writer (read-heavy throughput).

**Java Memory Model & happens-before** — JMM defines visibility/ordering across threads. **happens-before** edges: program order; unlock→subsequent lock; volatile write→subsequent read; `Thread.start()`→thread actions; thread actions→`join()`. Without a happens-before edge, updates may never be seen.

**ThreadLocal** — Per-thread independent copy of a variable (per-thread context, `SimpleDateFormat`). **Pitfall:** memory leaks in thread pools — always `remove()` after use.

---

## 5. Collections Framework

**List vs Set vs Map** — List: ordered, allows duplicates, index access. Set: unique elements. Map: key→value, unique keys. (Map does *not* extend `Collection`.)

**ArrayList vs LinkedList** — ArrayList: dynamic array, O(1) random access, O(n) middle insert (shifting), better cache locality — the default. LinkedList: doubly-linked, O(1) insert/delete at ends/known nodes, O(n) random access, implements `Deque`.

**Vector vs ArrayList** — Vector is synchronized (legacy, slower, grows 100%); ArrayList is unsynchronized (faster, grows ~50%). Prefer ArrayList / concurrent collections.

**HashMap internals** — Array of buckets (`Node[]`, default capacity 16). Index = `(n-1) & hash` (hash is perturbed). Collisions chain in a linked list; since Java 8, a bucket **treeifies** to a red-black tree when chain > **8** *and* table capacity ≥ **64** (else it resizes), reverting below 6. **Load factor 0.75** triggers resize (capacity doubles, rehash) when `size > capacity × loadFactor`. Allows one null key + null values. Not thread-safe.

**HashMap vs Hashtable vs ConcurrentHashMap** — HashMap: unsynchronized, fast, null key allowed. Hashtable: fully synchronized (locks whole map), no nulls, legacy. ConcurrentHashMap: fine-grained locking, thread-safe, no nulls, weakly-consistent iterators.

**HashSet** — Backed by a HashMap (elements stored as keys with a shared `PRESENT` dummy value). Uniqueness via `hashCode()`/`equals()`.

**TreeMap / TreeSet** — Red-black tree, sorted (natural or `Comparator`), O(log n), navigation methods (`first`, `ceiling`, `floor`). No null key. **LinkedHashMap** keeps insertion order (or access order with `accessOrder=true` → override `removeEldestEntry()` for an **LRU cache**).

**Fail-fast vs fail-safe** — Fail-fast (ArrayList, HashMap) throw `ConcurrentModificationException` on structural modification during iteration (via `modCount`); safe removal needs `iterator.remove()`. Fail-safe (CopyOnWriteArrayList, ConcurrentHashMap) iterate over a snapshot / weakly consistent — no exception.

**Comparable vs Comparator** — `Comparable.compareTo()`: single natural ordering inside the class. `Comparator.compare()`: external, multiple custom orderings without editing the class.

**Thread-safe collections** — `Collections.synchronizedList/Map/Set` (synchronize manually while iterating) or concurrent classes: `ConcurrentHashMap`, `CopyOnWriteArrayList` (copies on write, lock-free reads — read-heavy), `ConcurrentSkipListMap`, blocking queues.

---

## 6. Spring, Spring Boot, REST & Microservices

### Spring Core
- **IoC / DI** — Container creates and wires objects (decoupling, testability). Injection types: **constructor** (preferred — immutability, mandatory deps), setter (optional), field (`@Autowired`, discouraged for testing).
- **Bean scopes** — `singleton` (default, one per container), `prototype` (new each request), web: `request`/`session`/`application`/`websocket`.
- **Bean lifecycle** — instantiate → inject deps → `BeanPostProcessor` (before) → init (`@PostConstruct`/`afterPropertiesSet`) → `BeanPostProcessor` (after) → in use → destroy (`@PreDestroy`).
- **`@Autowired`** — resolves by type, then qualifier/name; `@Qualifier` disambiguates; `required=false` optional.
- **`@Component`/`@Service`/`@Repository`** — stereotypes; `@Repository` adds persistence exception translation. **`@Configuration` + `@Bean`** for explicit/third-party bean registration.
- **ApplicationContext vs BeanFactory** — ApplicationContext adds eager singletons, events, i18n, AOP, resource loading (use in production).
- **AOP** — modularizes cross-cutting concerns (logging, security, transactions): aspect, advice (before/after/around), join point, pointcut. Spring uses **JDK dynamic proxies** (interface) or **CGLIB** (subclass). Watch the **self-invocation** limitation.

### Spring Boot
- **Auto-configuration** — `@EnableAutoConfiguration` conditionally configures beans from classpath + properties (`@Conditional`).
- **Starters** — curated, version-managed dependency bundles (`spring-boot-starter-web`, `-data-jpa`, `-security`).
- **`@SpringBootApplication`** = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- **Profiles** — `@Profile("dev")`, `spring.profiles.active`, `application-{profile}.yml`.
- **Actuator** — `/health`, `/metrics`, `/info`, `/beans` operational endpoints.
- **Embedded server** — Tomcat/Jetty/Undertow → runnable JAR (`java -jar`), `server.port`.
- **vs Spring MVC** — convention over configuration: auto-config, starters, embedded server, no XML.

### REST
- **Principles** — resource-oriented, **stateless**, client-server, cacheable, uniform interface, layered.
- **Idempotency** — GET, PUT, DELETE, HEAD, OPTIONS idempotent; **POST and PATCH are not**. POST = create, PUT = full replace (idempotent), PATCH = partial update.
- **Status codes** — 2xx (200 OK, 201 Created, 204 No Content), 3xx (301, 304), 4xx (400, 401, 403, 404), 5xx (500, 503).
- **Annotations** — `@RestController` = `@Controller` + `@ResponseBody`. `@PathVariable` (path segment, required) vs `@RequestParam` (query param, optional). `@RequestBody`/`@ResponseBody` (de)serialize JSON. Global errors via `@ControllerAdvice` + `@ExceptionHandler`.
- **Versioning** — URI (`/v1/`), query param, header, or content negotiation. **HATEOAS** = responses embed links. URI design: plural nouns, lowercase, hyphens, no verbs.

### JPA / Hibernate
- **Lazy vs eager** — `@OneToMany`/`@ManyToMany` default LAZY; `@ManyToOne`/`@OneToOne` default EAGER. Prefer lazy.
- **N+1 problem** — 1 query for parents + N for associations. Fix: `JOIN FETCH`, `@EntityGraph`, batch fetching (`@BatchSize`).
- **Caches** — first-level (session, default on); second-level (shared across sessions, optional: EhCache/Hazelcast/Redis).
- **Entity states** — Transient → Managed (tracked, auto-flush) → Detached → Removed.
- **`@Transactional`** — runs in a transaction (AOP proxy); rolls back on **unchecked** exceptions by default. **Propagation**: REQUIRED (default), REQUIRES_NEW, NESTED, etc. **Isolation**: READ_UNCOMMITTED → SERIALIZABLE.
- `find()` (hits DB) vs `getReference()` (lazy proxy). Cascade types: PERSIST/MERGE/REMOVE/ALL.

### Security
- **AuthN vs AuthZ** — authentication = who you are; authorization = what you can access.
- **Spring Security** — servlet filter chain: `SecurityFilterChain`, `AuthenticationManager`, `UserDetailsService`, `SecurityContext`.
- **JWT** — header.payload.signature (Base64URL); stateless auth via `Authorization: Bearer`.
- **CSRF** — enabled by default; typically disabled for stateless JWT APIs (no session cookie).

### Microservices
- **Monolith vs microservices** — monolith: one deployable, simple, strong consistency early; microservices: small autonomous services, own DB each, independent scaling/deploy — at the cost of distributed complexity. *Recommendation: start with a modular monolith, extract services when boundaries are clear.*
- **Service discovery** — Eureka/Consul/K8s DNS (dynamic registration/lookup).
- **API gateway** — single entry point: routing, auth, rate limiting, aggregation (Spring Cloud Gateway).
- **Circuit breaker** — Closed → Open → Half-Open; **Resilience4j** (Hystrix legacy); pair with retries/timeouts/bulkheads/fallbacks.
- **Config server** — centralized externalized config (Spring Cloud Config + Git).
- **Communication** — sync REST vs async messaging (RabbitMQ/Kafka) for decoupling.
- **Saga pattern** — distributed consistency without 2PC: chain of local transactions + compensating transactions; choreography (events) vs orchestration (coordinator).
- **Idempotency** — handle at-least-once delivery with idempotency keys/dedup.
- **Observability** — centralized logs, metrics (Prometheus/Micrometer), distributed tracing (correlation IDs, Zipkin/Sleuth/OpenTelemetry).

---

## 7. SQL, Databases, Caching & Messaging

### SQL
- **Joins** — INNER (matches only), LEFT/RIGHT (all of one side + matches, NULLs otherwise), FULL OUTER (all rows both sides), CROSS (Cartesian).
- **Keys** — PRIMARY KEY = UNIQUE + NOT NULL (one per table); UNIQUE allows NULLs/multiple; FOREIGN KEY enforces referential integrity.
- **Indexes** — B-tree keeps keys sorted/balanced → O(log n) lookups & ranges. Clustered (rows in key order, one per table) vs non-clustered (separate pointer structure). **Don't index** small tables, low-cardinality columns, or write-heavy tables (every write updates indexes).
- **Normalization** — 1NF (atomic values), 2NF (no partial dependency on a composite key), 3NF (no transitive dependency). **Denormalization** adds redundancy to speed reads at write-consistency cost.
- **ACID** — Atomicity, Consistency, Isolation, Durability.
- **Isolation levels & anomalies** — READ UNCOMMITTED (dirty reads) → READ COMMITTED (no dirty, allows non-repeatable) → REPEATABLE READ (allows phantoms) → SERIALIZABLE (full). Higher = more locking, less throughput.
- **WHERE vs HAVING** — WHERE filters rows before grouping; HAVING filters groups after aggregation.
- **Window functions** — compute across related rows without collapsing them: `ROW_NUMBER`/`RANK`/`DENSE_RANK` + `PARTITION BY`/`ORDER BY` (running totals, rankings).
- **DELETE vs TRUNCATE vs DROP** — DELETE (selective, logged, rollback, triggers); TRUNCATE (all rows, fast, resets identity, no WHERE); DROP (removes the table).
- **Optimization** — `EXPLAIN`/`EXPLAIN ANALYZE`: look for full scans, missing indexes, bad join order; fix with composite indexes, avoid `SELECT *`, rewrite subqueries as joins, refresh statistics.
- **Classic coding Qs:**
  - Second-highest salary: `SELECT MAX(salary) FROM emp WHERE salary < (SELECT MAX(salary) FROM emp);` or `ORDER BY salary DESC LIMIT 1 OFFSET 1`.
  - Find duplicates: `SELECT name, COUNT(*) FROM t GROUP BY name HAVING COUNT(*) > 1;`

### NoSQL & distributed data
- **SQL vs NoSQL** — SQL for structured data, relations, ACID, joins; NoSQL for flexible schema, high throughput, horizontal scale, simple access patterns.
- **CAP theorem** — pick 2 of Consistency/Availability/Partition-tolerance; since partitions are unavoidable, the real trade-off is C vs A. CP (HBase, Mongo default) vs AP (Cassandra, DynamoDB).
- **Eventual consistency** — replicas converge if writes stop; fine for feeds, not balances.
- **Sharding/partitioning** — split data by shard key (horizontal = rows, vertical = columns); watch hot shards, cross-shard joins, rebalancing.
- **Replication** — master-slave (read scaling, failover) vs master-master (write availability + conflict resolution).
- **Consistent hashing** — keys + nodes on a ring so adding/removing a node moves only a fraction of keys; virtual nodes balance load.

### Caching
- **Strategies** — cache-aside/lazy (app loads on miss), read-through, write-through (sync to DB, fresh but slower writes), write-behind (async flush, fast but risk of loss), refresh-ahead.
- **Invalidation** — TTL, explicit eviction on write, write-through. (Hard problem.)
- **Eviction** — **LRU** (most common), LFU, FIFO, TTL.
- **Redis** — in-memory key-value with rich structures (hashes, sets, sorted sets); caching, sessions, rate limiting, leaderboards, pub/sub; single-threaded atomic commands; RDB/AOF persistence.

### Messaging
- **Kafka vs RabbitMQ** — RabbitMQ: broker with exchanges/queues, push, deletes after consumption, rich routing (thousands msg/s). Kafka: distributed commit log (topics/partitions), pull, **retains messages** (replay), millions msg/s — for high-throughput streaming.
- **Kafka concepts** — topic → partitions (ordered, append-only) → offsets; **consumer groups** share partitions for parallelism. Ordering guaranteed **only within a partition** (use a partition key).
- **Delivery** — at-most-once (may lose), at-least-once (may duplicate — make consumers **idempotent**), exactly-once (idempotent producer + transactions; costly).

---

## 8. System Design (Mid-Level)

**Approach a design question:** (1) clarify functional + non-functional requirements & scope, (2) back-of-envelope estimation (QPS, storage, bandwidth), (3) define the API, (4) high-level diagram (clients → LB → services → DB/cache), (5) deep-dive data model & bottlenecks, (6) scale (cache, shard, replicate, CDN), (7) discuss trade-offs.

**Scaling** — Vertical (bigger machine, ceiling + SPOF) vs horizontal (more machines, needs stateless servers + shared session store + load balancing).

**Load balancer** — distributes requests (round-robin, least-connections, IP-hash); L4 (transport, fast) vs L7 (application, content-aware); active-active/active-passive failover.

**CDN** — edge-cached static content near users; push (you upload) vs pull (fetch on first request + TTL).

**Estimation** — rough latencies: memory ~100ns, SSD ~1ms, disk seek ~10ms, network RTT ~100ms; e.g. 100 req/s × 86,400 ≈ 8.6M req/day.

**Common designs:**
- **URL shortener** — high read:write; Base62 over an auto-increment counter (collision-free) or hashing; scalable DB + Redis cache for hot URLs; 301/302 redirect; expiry cleanup.
- **Rate limiter** — token bucket (allows bursts — most common), leaky bucket (steady output), fixed window (simple, boundary bursts), sliding window (accurate). Distributed: Redis atomic `INCR` + TTL.
- **Notification system** — events → message queue → channel workers (push/SMS/email) via providers; templates, user prefs, retries with idempotency keys, dead-letter queues.

**REST API design** — stateless, nouns for resources, verbs as HTTP methods, correct status codes, versioning, pagination/filtering.

---

## 9. Behavioral & HR

Use **STAR** (Situation, Task, Action, Result). Prepare 4–6 stories you can flex to different prompts.

Commonly asked (Aristocrat-reported and standard):
- "Why do you want to work here?" / "What's your dream company?"
- "How do you deal with criticism?"
- "Tell me about a disagreement with your manager — how did you handle it?"
- "Describe your ideal work environment."
- "Tell me about a challenging bug / project and how you solved it."
- "A time you had a tight deadline / had to learn something fast."
- "A time you disagreed with a technical decision."
- Logistics: notice period, salary expectations, why leaving current role.

**Company hooks to prepare:**
- **Aristocrat:** a genuine interest in **gaming** (they probe this). Mention real-time/regulated-gaming systems, the scale of slot/RMG platforms.
- **Storware:** interest in **backup/data-protection & virtualization**, working in small product-oriented Scrum teams, code-quality culture (SonarQube).

---

## 10. Salary Expectations (Warsaw)

Mid-level Java/Spring backend, **employment contract (UoP), gross (brutto) monthly** — reference ranges (verify against live JustJoin.it / NoFluffJobs / theProtocol.it):

| Level | UoP gross/month | B2B/month (net + VAT, comparison) |
|---|---|---|
| Junior | 8,000 – 12,000 PLN | 9,000 – 14,000 PLN |
| **Mid / Regular** | **14,000 – 19,000 PLN** | 16,000 – 24,000 PLN |
| Senior | 19,000 – 28,000 PLN | 25,000 – 38,000 PLN |

**What to announce:** anchor near the top of your range — e.g. **"around 17,000 PLN gross on UoP"** (or "16–19k depending on full scope/benefits"). Floor ~14,000 PLN gross.

Notes:
- Always say **"gross/brutto"** explicitly in Poland.
- **UoP vs B2B** differ a lot — B2B looks ~30–40% higher but you lose paid leave/sick pay and cover ZUS/taxes yourself. Don't compare the two columns directly.
- Aristocrat (larger/international) tends to pay at/above mid-market; Storware (smaller product company) likely lower-to-mid with strong learning/product upside.

---

## 11. 2-Week Study Plan & Checklist

**Week 1 — fundamentals + coding**
- Days 1–2: Java core & OOP (Part 2) + 10 easy/medium LeetCode (arrays, strings, two-pointers).
- Days 3–4: Collections (Part 5) + JVM/GC (Part 3) + linked-list & tree problems (reverse list, detect loop, leaf count).
- Day 5: Concurrency (Part 4) + bit manipulation / array rotation problems.
- Day 6: SQL (Part 7) — write the classic queries by hand.
- Day 7: Review + mock the Aristocrat-style DSA list above.

**Week 2 — frameworks + design + company**
- Days 8–9: Spring/Spring Boot/REST/JPA (Part 6) — be able to explain DI, bean lifecycle, `@Transactional`, N+1.
- Day 10: Microservices + messaging + caching (Parts 6–7).
- Day 11: System design (Part 8) — practice URL shortener + rate limiter out loud.
- Day 12: **Aristocrat** deep-dige — OO design ("design a calculator"), gaming domain, WebSockets/MongoDB; "why gaming" story.
- Day 13: **Storware** deep-dive — backup/virtualization concepts, Docker/K8s/AWS, REST design; "why backup" story.
- Day 14: Behavioral (STAR stories) + salary framing + full mock interview.

**Readiness checklist:**
- [ ] Can explain `equals`/`hashCode` contract and HashMap internals (treeify, load factor)
- [ ] Can explain JVM memory areas + GC generations + pick a collector
- [ ] Can explain `volatile` vs `synchronized` vs atomics, and write a thread-safe counter
- [ ] Can reverse a linked list iteratively *and* recursively, detect a cycle
- [ ] Can explain Spring DI, bean lifecycle, `@Transactional`, and fix an N+1
- [ ] Can design REST endpoints with correct verbs/status codes/idempotency
- [ ] Can write 2nd-highest-salary & find-duplicates SQL from memory
- [ ] Can explain CAP, sharding, caching strategies, Kafka vs RabbitMQ
- [ ] Can design a URL shortener and a rate limiter out loud
- [ ] Have a sincere "why this company" story for each
- [ ] Have a salary number ready (gross UoP) with a floor

---

## 12. Sources

**Company:**
- Aristocrat — GeeksforGeeks interview experiences: [Aristocrat Leisure Recruitment](https://www.geeksforgeeks.org/interview-experiences/aristocrat-leisure-recruitment-process/), [Aristocrat Technologies (Experienced)](https://www.geeksforgeeks.org/aristocrat-technologies-interview-experience-experienced/), [Aristocrat Gaming Set 1](https://www.geeksforgeeks.org/interview-experiences/aristocrat-gaming-interview-experience-set-1-off-campus/)
- Aristocrat job postings (BuiltIn): [Back End Java Developer](https://builtin.com/job/back-end-java-developer/4181248), [Back-End Developer (.NET)](https://builtin.com/job/back-end-developer/3001806)
- Storware careers: [EN](https://storware.eu/company/careers/), [PL (recruitment process)](https://storware.eu/pl/dolaczdonas/dolaczdonas), [storware.eu](https://storware.eu/)

**General Java/Spring/DB/system design:**
- Java core/OOP: [InterviewBit](https://www.interviewbit.com/java-interview-questions/), [DigitalOcean](https://www.digitalocean.com/community/tutorials/core-java-interview-questions-and-answers), [GeeksforGeeks equals/hashCode](https://www.geeksforgeeks.org/java/equals-hashcode-methods-java/)
- JVM/GC: [GeeksforGeeks JVM architecture](https://www.geeksforgeeks.org/java/jvm-works-jvm-architecture/), [GeeksforGeeks GC](https://www.geeksforgeeks.org/java/garbage-collection-java/), [GeeksforGeeks GC collectors](https://www.geeksforgeeks.org/java/types-of-jvm-garbage-collectors-in-java-with-implementation-details/), [DigitalOcean JVM memory model](https://www.digitalocean.com/community/tutorials/java-jvm-memory-model-memory-management-in-java)
- Concurrency: [DigitalOcean/JournalDev](https://www.digitalocean.com/community/tutorials/java-multithreading-concurrency-interview-questions-answers), [InterviewBit](https://www.interviewbit.com/multithreading-interview-questions/), [Jenkov JMM](https://jenkov.com/tutorials/java-concurrency/java-memory-model.html), [Jenkov CAS](https://jenkov.com/tutorials/java-concurrency/compare-and-swap.html)
- Collections: [GeeksforGeeks collections Qs](https://www.geeksforgeeks.org/java/java-collections-interview-questions/), [GeeksforGeeks HashMap internals](https://www.geeksforgeeks.org/java/internal-working-of-hashmap-java/), [InterviewBit](https://www.interviewbit.com/java-collections-interview-questions/)
- Spring/REST/microservices: [GeeksforGeeks Spring](https://www.geeksforgeeks.org/java/spring-interview-questions/), [GeeksforGeeks Spring Boot](https://www.geeksforgeeks.org/springboot/spring-boot-interview-questions-and-answers/), [InterviewBit REST](https://www.interviewbit.com/rest-api-interview-questions/), [InterviewBit JPA](https://www.interviewbit.com/jpa-interview-questions/), [InterviewBit microservices](https://www.interviewbit.com/microservices-interview-questions/)
- SQL/system design: [system-design-primer](https://github.com/donnemartin/system-design-primer), [GeeksforGeeks SQL](https://www.geeksforgeeks.org/sql/sql-interview-questions/), [GeeksforGeeks CAP](https://www.geeksforgeeks.org/system-design/cap-theorem-in-system-design/), [GeeksforGeeks rate limiting](https://www.geeksforgeeks.org/system-design/rate-limiting-algorithms-system-design/), [AWS RabbitMQ vs Kafka](https://aws.amazon.com/compare/the-difference-between-rabbitmq-and-kafka/), [Martin Fowler microservices](https://martinfowler.com/articles/microservices.html)

> **Caveat:** Live web search was intermittently unavailable during research; some Glassdoor/AmbitionBox/JustJoin.it/NoFluffJobs pages were not directly fetchable. Company sections rely on the accessible primary sources above. Before interviewing, re-check Glassdoor "Aristocrat/Storware Interview Questions", Levels.fyi, and current job ads for the latest specifics.
