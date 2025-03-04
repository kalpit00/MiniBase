# CS448 Project 2: Buffer Manager and Disk Manager

This project implements a Buffer Manager and Disk Manager for the MiniBase database system, focusing on efficient page management between disk and memory.

## Overview

The Buffer Manager is responsible for managing the buffer pool, which is a collection of memory pages (frames) used to cache disk pages. It handles page pinning, unpinning, allocation, and deallocation, as well as implementing buffer replacement policies.

## Tech Stack

- Java
- Maven for dependency management
- JUnit for testing

## Project Structure

- `src/main/java/bufmgr/`: Buffer Manager implementation
  - `BufMgr.java`: Core Buffer Manager implementation
  - `FrameDescriptor.java`: Metadata for buffer frames
  - Various exception classes for error handling
- `src/main/java/diskmgr/`: Disk Manager implementation
- `src/test/java/tests/`: Test cases
  - `BMTest.java`: Buffer Manager test suite

## Features

### Buffer Manager

- **Page Pinning**: Loads pages from disk into memory frames
- **Page Unpinning**: Releases pages from memory when no longer needed
- **Dirty Page Management**: Tracks modified pages that need to be written back to disk
- **Buffer Replacement Policies**: Implements FIFO (First-In-First-Out) replacement strategy
- **Page Allocation**: Allocates new pages on disk
- **Page Deallocation**: Deallocates pages from disk

### Frame Descriptor

Each frame in the buffer pool has an associated descriptor that tracks:
- Page number
- Pin count (number of clients using the page)
- Dirty bit (whether the page has been modified)

## Usage

To run the Buffer Manager tests:

```bash
java -cp target/cs448p2-1.0-SNAPSHOT.jar tests.BMTest
```

The test suite includes various test cases for pinning, unpinning, allocation, and deallocation operations.

## Buffer Replacement Policy

The current implementation uses the FIFO (First-In-First-Out) replacement policy, where the oldest unpinned page is selected for replacement when a new page needs to be loaded into the buffer pool.

## Building the Project

```bash
mvn clean package
```

This will compile the code and run the tests, creating a JAR file in the `target` directory.
