# Transaction Serializability Checker

A tool for checking whether a given transaction schedule is conflict serializable by constructing and analyzing a precedence graph.

## Overview

This project provides a utility to determine if a given schedule of database operations is conflict serializable. It works by:
1. Parsing a schedule of read and write operations
2. Identifying conflicts between operations
3. Constructing a precedence graph
4. Checking for cycles in the graph (which indicate non-serializability)

## Tech Stack

- Java
- No external dependencies

## Project Structure

- `src/App.java`: Main application entry point
- `src/Schedule.java`: Core implementation of the serializability checker
  - `Operation` class: Represents individual read/write operations
  - `OperationConflict` class: Represents conflicts between operations

## Features

### Schedule Parsing

- Parses schedules in the format `r1x`, `w2y`, etc., where:
  - First character is the operation type (`r` for read, `w` for write)
  - Second character is the transaction number
  - Third character is the data item being accessed

### Conflict Detection

- Identifies conflicts between operations based on the following rules:
  - Read-Write conflict: One transaction reads a data item, another writes to the same item
  - Write-Read conflict: One transaction writes to a data item, another reads the same item
  - Write-Write conflict: Two transactions write to the same data item

### Precedence Graph Construction

- Constructs a directed graph where:
  - Nodes represent transactions
  - Edges represent conflicts (direction from earlier to later operation)

### Cycle Detection

- Uses depth-first search to detect cycles in the precedence graph
- A cycle indicates that the schedule is not conflict serializable

## Usage

To run the serializability checker:

```bash
java -jar serializability.jar "r1x" "r2z" "r1z" "r3y" "r3y" "w1x" "w3y" "r2y" "w2z" "w2y"
```

Or use the test mode:

```bash
java -jar serializability.jar test
```

## Example Output

For a given schedule, the output includes:
1. The parsed schedule
2. The precedence graph representation
3. A determination of whether the schedule is conflict serializable
4. If serializable, a possible serial execution order

## Building the Project

To compile the project:

```bash
javac src/*.java -d out/
```

To create a JAR file:

```bash
jar cvf serializability.jar -C out/ .
```
