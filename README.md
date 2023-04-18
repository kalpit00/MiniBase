# MiniBase, a small relational DBMS.

So far implemented a Buffer Manager and a Disk Manager, a Concurrency Control Layer and Basic SQL Query + Relational Algebra Operator Functionalities

Also implemented a Hadoop DFS using Spark to create an example sparkApp which can process a sample query taken from a BIG DATA dataset

PLEASE DOWNLOAD THE ZIP FILES

Download Zip p2, p3, p4 and p5, unzip and run as follows :

p2 has the buffer and disk managers. Run BMTest with the desired buffer replacement strategy to pin, unpin pages and use buffers for faster access

p3 has Relational Algebra operator functionalities : Selection, Projection and Join. Run QEPTest by passing in your directory path in argv[0] (or enter desired file paths in main method accordingly) to load files. I have used an Employee and Department sample data file for testing

p4 has a Concurrency Control Layer, which uses the popular Two-Phase Locking (2PL) Protocol to allow multiple transactions to occur concurrently. It takes in a schedule of operations and prints a log of a serializable schedule. It also adds Shared Locks for Read operations and Exclusive Locks for Write operations. Lastly, it uses DFS to check for DeadLock Detection and constructs a wait-for-graph by detecting cycles in the graph. In the case of deadlocks, it Aborts the operation appropriately

p5 uses the Apache Hadoop Distributed File System and Spark Java framework to read from an input dataset and process basic SQL queries. It creates a SparkApp for every query and uses Hadoop's MapReduce algorithm to improve the efficieny of processing a very large file system


ALSO ADDED During P4 : A TransactionSerializabilityChecker which takes in a schedule and checks if it is Serializable or not.
