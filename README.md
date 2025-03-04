# MiniBase: A Relational Database Management System

This repository contains a series of projects that collectively implement MiniBase, a small but functional relational database management system. Each project builds upon the previous ones to create a complete database system with various components.

## Project Overview

### [Project 1: Database Storage and Retrieval](./cs448p1)
- Implements different methods for storing and retrieving data
- Compares file-based, main-memory, and MapDB approaches
- Analyzes performance differences between storage backends

### [Project 2: Buffer Manager and Disk Manager](./cs448p2)
- Implements a Buffer Manager for caching disk pages in memory
- Provides efficient page pinning, unpinning, and replacement
- Implements FIFO buffer replacement strategy
- Manages disk space allocation and deallocation

### [Project 3: Relational Algebra Operators](./cs448p3)
- Implements fundamental relational algebra operators
- Includes Selection, Projection, and Join operations
- Uses an iterator-based execution model
- Supports query execution plans

### [Project 4: Concurrency Control Layer](./cs448p4)
- Implements Two-Phase Locking (2PL) Protocol
- Provides shared locks for read operations and exclusive locks for write operations
- Includes deadlock detection and resolution
- Ensures transaction serializability

### [Project 5: Big Data Processing with Apache Spark](./p5-skeleton)
- Uses Apache Hadoop and Spark for processing large datasets
- Implements SQL-like queries using Spark's DataFrame API
- Demonstrates distributed data processing techniques
- Processes movie ratings dataset

### [Transaction Serializability Checker](./TransactionSerializabilityChecker)
- Standalone tool for checking conflict serializability
- Constructs and analyzes precedence graphs
- Detects cycles in transaction schedules
- Determines if schedules are conflict serializable

## Tech Stack

- **Languages**: Java
- **Build Tools**: Maven
- **Testing**: JUnit
- **Big Data**: Apache Spark, Hadoop
- **Storage**: MapDB, Custom buffer management
- **Concurrency**: Two-Phase Locking

## Getting Started

Each project directory contains its own README with specific instructions for building and running that component. Generally, you can build each project using Maven:

```bash
cd <project-directory>
mvn clean package
```

## Project Dependencies

The projects build upon each other in the following order:
1. Project 1: Standalone
2. Project 2: Builds foundation for disk and buffer management
3. Project 3: Uses Project 2 components for storage
4. Project 4: Adds concurrency control to previous components
5. Project 5: Integrates with big data technologies

The Transaction Serializability Checker is a standalone tool that complements Project 4.
