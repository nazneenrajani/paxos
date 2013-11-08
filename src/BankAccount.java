
public class BankAccount {
	public int clientID;
	public int ACNumber;
	public double balance=0.0;
	
	public BankAccount(int clientID, int ACNumber){
		this.clientID=clientID;
		this.ACNumber=ACNumber;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientId) {
		clientID = clientId;
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

	public void setBalance(double bal) {
		balance = bal;
	}
}
