# CS448 Project 5: Big Data Processing with Apache Spark

This project demonstrates the use of Apache Hadoop and Spark for processing and querying large datasets in a distributed environment.

## Overview

The project focuses on using Apache Spark to process and query a movie ratings dataset. It implements SQL-like queries using Spark's DataFrame API and Spark SQL, showcasing how to efficiently process large datasets in a distributed manner.

## Tech Stack

- Java
- Apache Spark
- Apache Hadoop Distributed File System (HDFS)
- Maven for dependency management

## Project Structure

- `src/main/java/cs448/`: Source code
  - `App5.java`: Main application entry point
  - `Project5.java`: Implementation of Spark queries
  - `CS448Constants.java`: Constants used throughout the project
  - `CS448Utils.java`: Utility functions
  - `Movie.java`: Movie data model
  - `Rating.java`: Rating data model
  - `User.java`: User data model

## Features

### Spark SQL Queries

The project implements several SQL-like queries using Spark:
- Finding movies with ratings above a threshold for specific user occupations
- Additional queries can be implemented in the `Project5.java` file

### Data Models

- **Movie**: Represents movie data with attributes like ID, title, and genres
- **User**: Represents user data with attributes like ID, age, gender, and occupation
- **Rating**: Represents rating data with attributes like user ID, movie ID, and rating value

### Spark Application Framework

- **SparkSession**: Creates and manages Spark sessions
- **JavaRDD**: Represents distributed collections of data
- **Dataset<Row>**: Represents structured data with a schema

## Usage

To run the Spark application:

```bash
java -cp target/cs448p5-1.0-SNAPSHOT.jar cs448.App5 -i <input_path> -o <output_path> [options]
```

Options:
- `-i <path>`: Input path (required)
- `-o <path>`: Output path
- `-u <file>`: Users data file (default: users.dat)
- `-m <file>`: Movies data file (default: movies.dat)
- `-r <file>`: Ratings data file (default: ratings.dat)
- `-warmup`: Run warmup exercise
- `-q1`: Run query 1
- `-q1r <rating>`: Rating threshold for query 1
- `-q1o <occupation>`: Occupation for query 1

## Example Query

The project includes an example query that finds movie titles with ratings above a specified threshold for users with a specific occupation:

```sql
SELECT DISTINCT t.title 
FROM (
  SELECT title, rating, occupation 
  FROM movies m 
  JOIN ratings r ON m.movieid = r.movieid 
  JOIN users u ON u.userid = r.userid
) t 
WHERE t.rating >= [rating_threshold] 
AND t.occupation = [occupation_id]
```

## Building the Project

```bash
mvn clean package
```

This will compile the code and create a JAR file in the `target` directory.
