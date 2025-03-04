# CS448 Project 3: Relational Algebra Operators

This project implements fundamental relational algebra operators for the MiniBase database system, providing the building blocks for SQL query execution.

## Overview

The project focuses on implementing essential relational algebra operators that form the foundation of SQL query processing:
- Selection (σ)
- Projection (π)
- Join operations (including simple join and hash join)

These operators are implemented as iterators that can be combined to form complex query execution plans.

## Tech Stack

- Java
- Maven for dependency management
- JUnit for testing

## Project Structure

- `src/main/java/relop/`: Relational operator implementations
  - `Iterator.java`: Base abstract class for all operators
  - `FileScan.java`: Scans a heap file sequentially
  - `Selection.java`: Filters tuples based on predicates
  - `Projection.java`: Projects specific attributes from tuples
  - `SimpleJoin.java`: Implements nested-loop join
  - `HashJoin.java`: Implements hash-based join
  - `IndexScan.java`: Scans using an index
  - `KeyScan.java`: Scans by specific key values
  - `Predicate.java`: Represents selection predicates
  - `Schema.java`: Defines the schema for relations
  - `Tuple.java`: Represents a tuple in a relation
- `src/test/java/tests/`: Test cases
  - `QEPTest.java`: Query execution plan tests
  - `ROTest.java`: Relational operator tests

## Features

### Iterator Framework

All relational operators extend the `Iterator` abstract class, which provides:
- `explain()`: Explains the execution plan
- `restart()`: Restarts the iterator
- `close()`: Releases resources
- `hasNext()`: Checks for more tuples
- `getNext()`: Gets the next tuple
- `execute()`: Executes the iterator and prints results

### Relational Operators

- **FileScan**: Scans all tuples in a heap file
- **Selection**: Filters tuples based on predicates
- **Projection**: Projects specific attributes from tuples
- **SimpleJoin**: Implements nested-loop join algorithm
- **HashJoin**: Implements hash-based join for better performance
- **IndexScan**: Scans using an index structure
- **KeyScan**: Scans by specific key values

## Usage

To run the query execution plan tests:

```bash
java -cp target/cs448p3-1.0-SNAPSHOT.jar tests.QEPTest <data_directory>
```

Where `<data_directory>` is the path to the directory containing the test data files (Employee.txt and Department.txt).

## Example Queries

The test suite includes several example queries:
- Simple selection and projection operations
- Join operations between Employee and Department tables
- Complex queries combining multiple operators

## Building the Project

```bash
mvn clean package
```

This will compile the code and create a JAR file in the `target` directory.
