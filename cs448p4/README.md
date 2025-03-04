# CS448 Project 4: Concurrency Control Layer

This project implements a concurrency control layer for a database system, focusing on the Two-Phase Locking (2PL) protocol to ensure transaction serializability.

## Overview

The concurrency control layer manages concurrent access to database resources by multiple transactions. It uses the Two-Phase Locking (2PL) protocol to ensure that transactions are executed in a serializable manner, preventing anomalies such as lost updates, dirty reads, and non-repeatable reads.

## Tech Stack

- Java
- Maven for dependency management
- JUnit for testing

## Project Structure

- `src/main/java/CC/`: Concurrency control implementation
  - `CC.java`: Core implementation of the concurrency control layer
  - `Project.java`: Project-specific utilities
- `src/test/java/tests/`: Test cases
  - `CCTest.java`: Concurrency control test suite
- `ExpectedLogs.txt`: Expected output for test cases

## Features

### Two-Phase Locking (2PL) Protocol

- **Locking Mechanism**: Implements shared locks for read operations and exclusive locks for write operations
- **Two Phases**:
  1. **Growing Phase**: Transactions acquire locks but do not release any locks
  2. **Shrinking Phase**: Transactions release locks but do not acquire any new locks
- **Deadlock Detection**: Uses a wait-for graph to detect deadlocks among transactions
- **Deadlock Resolution**: Aborts transactions to resolve deadlocks when detected

### Transaction Management

- **Transaction Operations**: Supports read and write operations on data items
- **Transaction States**: Tracks transaction states (active, committed, aborted)
- **Logging**: Generates a log of transaction operations in a serializable schedule

### Deadlock Detection

- **Wait-For Graph**: Constructs a graph where nodes are transactions and edges represent waiting relationships
- **Cycle Detection**: Uses depth-first search to detect cycles in the wait-for graph
- **Deadlock Resolution**: Aborts transactions involved in deadlocks

## Usage

To run the concurrency control tests:

```bash
java -cp target/cs448p4-1.0-SNAPSHOT.jar tests.CCTest
```

The test suite includes various test cases for different transaction schedules, including:
- Simple schedules with 2 transactions
- Complex schedules with multiple transactions
- Schedules that cause deadlocks
- Schedules that test the 2PL protocol's correctness

## Example Schedule

A simple example schedule might look like:
```
T1: W(1,5); R(2); W(2,3); R(1); C
T2: R(1); W(1,2); C
```

Where:
- W(x,y) means "write value y to data item x"
- R(x) means "read data item x"
- C means "commit transaction"

## Building the Project

```bash
mvn clean package
```

This will compile the code and create a JAR file in the `target` directory.
