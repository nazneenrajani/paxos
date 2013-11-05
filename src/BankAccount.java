
public class BankAccount {
	public static int clientID;
	public static int ACNumber;
	public static double balance;
	
	public BankAccount(int clientID, int ACNumber){
		this.clientID=clientID;
		this.ACNumber=ACNumber;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		BankAccount.clientID = clientID;
	}

	public int getACNumber() {
		return ACNumber;
	}

	public void setACNumber(int aCNumber) {
		ACNumber = aCNumber;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		BankAccount.balance = balance;
	}
}
