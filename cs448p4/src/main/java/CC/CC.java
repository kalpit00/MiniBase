package CC;
import java.util.*;

public class CC
{

	static int finish;
	static int timeStamp;

	static int [] dataBase;

	static ArrayList<LogCommand> logCommands;
	static LinkedList<Transaction> transactionList;

	static ArrayList<Lock> lockArrayList;

	static TransactionGraph transactionGraph;

	public static class Lock
	{
		String type;
		ArrayList<Transaction> transactions;

		Lock() {
			this.type = "None";
			this.transactions = new ArrayList<Transaction>();
		}

		boolean isSharedLock(Transaction transaction) {
			if (this.transactions.contains(transaction))
				return true;
			else if (!this.type.equalsIgnoreCase("Exclusive")) {
				this.type = "Shared";
				this.transactions.add(transaction);
				return true;
			}
			else
				return false;
		}

		boolean isExclusiveLock(Transaction transaction) {
			if (this.type.equals("Exclusive")
					&& this.transactions.contains(transaction)) {
				return true;
			}
			else if (this.type.equals("Shared") &&
					this.transactions.size() == 1 && this.transactions.contains(transaction)) {
				// current is shared
				// all others are shared
				this.type = "Exclusive";
				return true;
			}
			else if (this.type.equals("None")) {
				this.type = "Exclusive";
				this.transactions.add(transaction);
				return true;
			}
			else
				return false;
		}
	}

	public static class Transaction
	{
		private String[] commandNames;
		private int priority;
		int lastPos = -1;
		boolean check;
		boolean isAborted;
		boolean isFinished;

		List<Command> commands;

		public Transaction(String cmdStr, int prior) {
			this.commandNames = cmdStr.split(";");
			this.priority = prior;
			this.commands = buildCommandList(this.commandNames);
			this.check = false;
			this.isAborted = false;
			this.isFinished = false;
		}

		private List<Command> buildCommandList(String[] commandNames) {
			List<Command> result = new ArrayList<Command>();
			for (String s: commandNames) {
				char cmd = s.charAt(0);
				String res = s.substring(1);
				int goalIndex = 0;
				int goalValue = 0;

				if (res.length() != 0) {
					res = res.replaceAll("[(]","");
					res = res.replaceAll("[)]", "");

					String [] indices = res.split(",");
					if (indices.length >= 1) {
						goalIndex = Integer.parseInt(indices[0]);
						if (indices.length > 1) {
							goalValue = Integer.parseInt(indices[1]);
						}
					}
				}
				Command c = new Command(cmd, goalIndex, goalValue);
				result.add(c);
			}
			return result;
		}

		int getPriority() {
			return priority;
		}

		public boolean isEqual(Transaction b) {
			return (this.priority == b.getPriority());
		}

		public boolean operatingCommands(Command command) {
			boolean flag = false;
			switch (command.getCommandName()) {
				case 'R':
					flag = CC.read(this, command.getGoalIndex());
					break;
				case 'W':
					flag = CC.write(this, command.getGoalIndex()
							, command.getGoalValue());
					break;
				case 'C':
					flag = CC.commit(this);
					break;
			}
			return flag;
		}
	}

	public static class Command
	{
		private char commandName;
		private int goalIndex;
		private int goalValue;

		Command(char commandName, int goalIndex, int goalValue) {
			this.commandName = commandName;
			this.goalIndex = goalIndex;
			this.goalValue = goalValue;
		}

		char getCommandName() {
			return this.commandName;
		}

		int getGoalIndex() {
			return this.goalIndex;
		}

		int getGoalValue() {
			return this.goalValue;
		}

	}

	// For part 2 record
	public static class LogCommand {
		private char operationName;
		private int timeStamp;
		private int transactionID;
		private int recordID;
		private int oldValue;
		private int newValue;
		private int prevTimeStamp;

		LogCommand(char operationName, int timeStamp, int transactionID,
				   int recordID, int oldValue, int newValue, int prevTimeStamp) {
			this.operationName = operationName;
			this.timeStamp = timeStamp;
			this.transactionID = transactionID;
			this.recordID = recordID;
			this.oldValue = oldValue;
			this.newValue = newValue;
			this.prevTimeStamp = prevTimeStamp;

		}

		public char getOperationName() {
			return operationName;
		}

		public int getTimeStamp() {
			return this.timeStamp;
		}

		public int getTransactionID() {
			return this.transactionID;
		}

		public int getNewValue() {
			return this.newValue;
		}

		public int getOldValue() {
			return this.oldValue;
		}

		public int getRecordID() {
			return this.recordID;
		}

		public int getPrevTimeStamp() {
			return this.prevTimeStamp;
		}
	}

	public static class TransactionGraph
	{
		private int numTrans;
		private ArrayList<List<Integer>> adjNodes;
		private ArrayList<Integer> names;

		TransactionGraph(int numTrans) {
			this.numTrans = numTrans;
			adjNodes = new ArrayList<>(numTrans);
			names = new ArrayList<>(numTrans);

			for (int i = 0; i < this.numTrans; i++) {
				adjNodes.add(new LinkedList<>());
				names.add(i);
			}
		}
		// Add edge here
		void addEdge(int source, int dest) {
//			System.out.println("Source: " + source + " Dest: " + dest);
			if (!adjNodes.get(source).contains(dest)) {
				adjNodes.get(source).add(dest);
			}
		}
		// Find the first cycle in the graph by DFS
		// Return a list for nodes in the cycle
		ArrayList<Integer> findCycle() {
			// Using DFS to find the cycle.
			ArrayList<Integer> resList = new ArrayList<>();
			// Initialize dfsStack
			Stack<Integer> dfsStack = new Stack<>();
			dfsStack.push(0);
			ArrayList<Integer> visited = new ArrayList<>();

			// Check the dfs Stack
			while (!dfsStack.isEmpty()) {
				int parent = dfsStack.pop();
				visited.add(parent);
				// Check whether the current node can go deeper
				for (Integer i: adjNodes.get(parent)) {
					if (!visited.contains(i)) {
						dfsStack.push(i);
					}
					else {
						int start = visited.indexOf(i);
						for (int j = start; j < visited.size(); j++) {
							resList.add(j);
						}
						return resList;
					}
				}
			}
			// If there's no cycle, return an empty list
			return resList;
		}
	}

	public static void printLogs() {
		System.out.println("Log:");
		for (int i = 0; i < logCommands.size(); i++) {
			String out = "	";
			out = out + logCommands.get(i).getOperationName() + ":";
			out = out + logCommands.get(i).getTimeStamp() + ",";
			out = out + "T" + logCommands.get(i).getTransactionID() + ",";
			if (logCommands.get(i).getRecordID() > -2)
				out = out + logCommands.get(i).getRecordID() + ",";
			if (logCommands.get(i).getOldValue() > -2)
				out = out + logCommands.get(i).getOldValue() + ",";
			if (logCommands.get(i).getNewValue() > -2)
				out = out + logCommands.get(i).getNewValue() + ",";
			out = out + logCommands.get(i).getPrevTimeStamp();

			System.out.println(out);
		}
	}


	/**
	 * Notes:
	 *  - Execute all given transactions, using locking.
	 *  - Each element in the transactions List represents all operations performed by one transaction, in order.
	 *  - No operation in a transaction can be executed out of order, but operations may be interleaved with other
	 *    transactions if allowed by the locking.
	 *  - The index of the transaction in the list is equivalent to the transaction ID.
	 *  - Print the log to the console at the end of the method.
	 *  - Return the new db state after executing the transactions.
	 * @param db the initial status of the db
	 * @param transactions the schedule, which basically is a {@link List} of transactions.
	 * @return the final status of the db
	 */
	public static int[] executeSchedule(int[] db, List<String> transactions)
	{
		dataBase = db;
		finish = 0;
		timeStamp = -1;

		lockArrayList = new ArrayList<Lock>();
		for (int i = 0; i < dataBase.length; i++) {
			List<Integer> temp = new ArrayList<Integer>();
			lockArrayList.add(new Lock());
		}
		transactionList = new LinkedList<Transaction>();

		logCommands = new ArrayList<LogCommand>();

		// Create the transaction List
		for (int i = 0; i < transactions.size(); i++) {
			Transaction transaction = new Transaction(transactions.get(i), (i + 1));
			transactionList.add(transaction);
		}

		// Create the transaction precedence Nodes of graph
		transactionGraph = new TransactionGraph(transactions.size());

		int count = 0;
		while (!allDone()) {
			Transaction transaction = transactionList.get(count);

			if (!transaction.isFinished) {
				Command command = transaction.commands.get(0);
				if (transaction.operatingCommands(command)) {
					transaction.commands.remove(command);
				}
				if (transaction.commands.isEmpty()) {
					transaction.isFinished = true;
				}
			}
			count++;
			if (count == transactionList.size()) {
				count = 0;
			}
		}

		printLogs();

		return dataBase;
	}

	public static boolean allDone() {
		for (int i = 0; i < transactionList.size(); i++) {
			if (!transactionList.get(i).isFinished) {
				return false;
			}
		}
		return true;
	}

	// Read with shared lock?
	public static boolean read(Transaction transaction, int readID) {

		if (lockArrayList.get(readID).isSharedLock(transaction)) {
			int element = dataBase[readID];
			timeStamp++;
			LogCommand command = new LogCommand('R', timeStamp, transaction.getPriority(),
					readID, element, -2, transaction.lastPos);

			logCommands.add(command);
			transaction.lastPos = timeStamp;
			return true;
		}
		else {
			// check for cycle and add to the dependency list
			ArrayList<Transaction> dependencies = lockArrayList.get(readID).transactions;
			for (Transaction trans: dependencies) {
				if (!transaction.isEqual(trans)) {
					transactionGraph.addEdge(transaction.getPriority() - 1,
							trans.getPriority() - 1);
				}
			}
			ArrayList<Integer> indices = transactionGraph.findCycle();
			if (!indices.isEmpty()) {
				abort(indices, dependencies);
			}
			return false;
		}
	}

	// Write without exclusive lock
	public static boolean write(Transaction transaction, int writeID, int writeValue) {
		//return true;
		if (lockArrayList.get(writeID).isExclusiveLock(transaction)) {
			timeStamp++;
			int oldValue = dataBase[writeID];
			dataBase[writeID] = writeValue;
			LogCommand command = new LogCommand('W', timeStamp, transaction.getPriority(),
					writeID, oldValue, writeValue, transaction.lastPos);
			logCommands.add(command);
			transaction.lastPos = timeStamp;

			return true;
		}
		else {
			// check for cycle and add to the dependency list
			ArrayList<Transaction> dependencies = lockArrayList.get(writeID).transactions;
			for (Transaction trans: dependencies) {
				if (!transaction.isEqual(trans)) {
					transactionGraph.addEdge(transaction.getPriority()-1,
							trans.getPriority()-1);
				}
			}

			ArrayList<Integer> indices = transactionGraph.findCycle();
			if (!indices.isEmpty()) {
				abort(indices, dependencies);
			}
			return false;
		}
	}

	// Commit and release all locks in this transaction
	public static boolean commit(Transaction transaction) {
		timeStamp++;
		LogCommand command = new LogCommand('C', timeStamp, transaction.getPriority(),
				-2, -2, -2, transaction.lastPos);
		logCommands.add(command);

		finish++;

		for (int i = 0; i < dataBase.length; i++) {
			lockArrayList.get(i).transactions.remove(transaction);
			if (lockArrayList.get(i).transactions.isEmpty()) {
				lockArrayList.get(i).type = "None";
			}
		}

		transaction.isFinished = true;
//		transactionList.remove(transaction);

		return true;
	}

	public static void abort(ArrayList<Integer> indices, ArrayList<Transaction> dependencies) {
		// Aborting:
		timeStamp++;
		// Find the lowest priority transaction
		int removeTrans = Collections.max(indices);
		// First of all, remove the edges from the graph
		for (List<Integer> list: transactionGraph.adjNodes) {
			if (list.contains(removeTrans)) {
				list.remove(list.indexOf(removeTrans));
			}
		}

		int removePriority = removeTrans + 1;
		// Set this transaction to be aborted
		for (int j = 0; j < transactionList.size(); j++) {
			if (transactionList.get(j).getPriority() == removePriority) {
				transactionList.get(j).isAborted = true;
				transactionList.get(j).isFinished = true;
			}
		}

		// Remove the transaction from each locks
		for (int j = 0; j < lockArrayList.size(); j++) {
			List<Transaction> transactions = lockArrayList.get(j).transactions;
			for (Transaction tr: transactions) {
				if (tr.getPriority() == removePriority) {
					transactions.remove(tr);
					break;
				}
			}
			if (transactions.isEmpty()) {
				lockArrayList.get(j).type = "None";
			}
		}

		// Create Abort Log
		int lastStamp = 0;
		for (LogCommand log: logCommands) {
			int oldValue = log.getOldValue();
			int recID = log.getRecordID();
			int transPriority = log.getTransactionID();
			char name = log.getOperationName();
			if (transPriority == removePriority) {
				dataBase[recID] = oldValue;
				lastStamp = log.getTimeStamp();
			}
		}
		LogCommand logCommand = new LogCommand('A', timeStamp, removePriority,
				-2, -2, -2, lastStamp);

		logCommands.add(logCommand);

	}
}
