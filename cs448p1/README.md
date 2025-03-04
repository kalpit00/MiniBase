# CS448 Project 1: Database Storage and Retrieval

This project implements different methods for storing and retrieving data from a database, comparing the performance of various storage backends.

## Overview

This project demonstrates three different approaches to database storage and retrieval:
1. **File-based approach**: Direct file scanning and retrieval
2. **Main-memory approach**: Loading all data into memory using HashMap
3. **MapDB approach**: Using MapDB as a persistent storage engine

## Tech Stack

- Java 7+
- Maven for dependency management
- MapDB (v3.0.7) for persistent storage

## Project Structure

- `src/main/java/cs448/App.java`: Main application entry point
- `src/main/java/cs448/project1.java`: Core implementation of the storage and retrieval methods
- `data_*.tsv`: Sample data files in TSV format

## Features

- **File Select**: Scans a file and retrieves specific columns for a given key (O(n) complexity)
- **Main-memory Select**: Loads data into memory for fast retrieval (O(1) complexity)
- **MapDB Select**: Uses MapDB for persistent storage with fast retrieval (O(1) complexity)

## Usage

The application can be run with the following command:

```bash
java -cp target/cs448p1-1.0-SNAPSHOT.jar cs448.App <file_path> <backend> <id>
```

Where:
- `<file_path>`: Path to the data file
- `<backend>`: Storage backend (0 for file, 1 for main-memory, 2 for MapDB)
- `<id>`: ID to search for in the database

## Performance

The project includes methods to determine the fastest loading and selection methods:
- `fastestLoad()`: Returns the fastest loading method (1 for Main-memory, 2 for MapDB)
- `fastestSelect()`: Returns the fastest selection method (0 for File, 1 for Main-memory, 2 for MapDB)

## Building the Project

```bash
mvn clean package
```

This will create a JAR file in the `target` directory that can be executed as described in the Usage section.
