# Objective map

This project was reconciled against the public `main` branch of
[`JesterCharles/pep-technology-content`](https://github.com/JesterCharles/pep-technology-content)
at commit [`784b3fbce1c8ec538def9d364371ebb2ff89a80b`](https://github.com/JesterCharles/pep-technology-content/commit/784b3fbce1c8ec538def9d364371ebb2ff89a80b).
The catalog declarations at that exact commit remain the canonical source. This file only summarizes the public
objective groups chosen for this project; it does not reproduce assessments or answer material.

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

The taught PostgreSQL connection objective is adapted, not claimed in full: this repository uses embedded H2
`2.3.232` in PostgreSQL compatibility mode so learners need no database server. The selected SQL is a portable subset,
but H2 is not PostgreSQL. Identity-column syntax and the support reset command (`DROP ALL OBJECTS`) are H2-specific;
PostgreSQL-specific tools, diagnostics, extensions, and planner behavior are outside this project.
