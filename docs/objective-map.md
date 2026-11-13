# Objective map

This page lists what the project practices, what you should know before starting, and what it intentionally leaves
out. It does not contain assessments or answers.

## Selected objective groups

### Java Foundations

- **Core language and methods:** typed values, branching, loops, methods, strings, and lists used in a small program.
- **Classes and objects:** records/classes, constructors, enums, validation, encapsulation, and readable domain models.
- **Interfaces and design:** a repository interface with a JDBC implementation, supplied through constructor injection.
- **Exceptions and resources:** checked `SQLException` handling, application-specific exceptions, and
  try-with-resources.
- **Testing:** public JUnit tests using arrange-act-assert and exception assertions.

### SQL & JDBC Foundations

- **Relational design:** entities, primary/foreign keys, one-to-many relationships, normalization, data types, and
  `NOT NULL`, `UNIQUE`, `CHECK`, and referential constraints.
- **CRUD and reporting:** parameterized inserts, selects, updates, deletes, filtering, deterministic sorting, joins,
  aggregates, `GROUP BY`, `HAVING`, `CASE`, and nullable values.
- **JDBC data access:** `Connection`, `PreparedStatement`, generated keys, `ResultSet` row mapping, repository
  substitution, and resource closure.
- **Transactions:** a multi-statement checkout that commits as one unit or rolls back on failure.
- **Database testing:** an isolated in-memory database for tests plus a repeatable file-backed reset for local use.

## Prerequisites

Learners are expected to have completed the Foundation Git/shell setup, Java Foundations, and the relational-design
and basic SQL portions of SQL & JDBC Foundations. The local workflow requires JDK 25 and Maven 3.9 or newer.

## Intentional exclusions

To keep one project small and cohesive, it does not assess HTTP/Javalin, recursion and search algorithms, deep
inheritance, Mockito, logging configuration, PostgreSQL administration, environment-based database credentials,
`psql`, CSV loading, `generate_series`, DML `RETURNING`, CTEs, window functions, indexes, query plans, or concurrent
transaction-isolation experiments.

PostgreSQL connections are adapted rather than practiced in full: the project uses an embedded local database in a
PostgreSQL-compatible mode, so you need no database server. The SQL you write is a portable subset, but the embedded
database is not PostgreSQL. The supplied identity columns and reset support are specific to the embedded database;
PostgreSQL-specific tools, diagnostics, extensions, and planner behavior are outside this project.
