import java.util.HashMap;


public class ReplicaState {
	HashMap<Integer,BankAccount> state;
	ProcessId id;
	int command_number=0;
	
	public ReplicaState(){
		state = new HashMap<Integer,BankAccount>();
	}
	public void update(int ACNumber, int clientID, double amount, boolean add, HashMap<Integer,BankAccount> state){
		if(!state.containsKey(ACNumber)){
			BankAccount newAC = new BankAccount(clientID,ACNumber);
			state.put(ACNumber, newAC);
			operation(newAC,ACNumber,amount,add);
		}
		else{
			BankAccount temp = state.get(ACNumber);
			operation(temp,ACNumber,amount,add);
		}
		/*
		for(int key:state.keySet()){
			System.out.println(id+": "+key+"     "+state.get(key).getBalance()+"   "+state.get(key).getClientID());
		}
		*/
	}
	public void operation(BankAccount temp, int ACNumber,double amount, boolean add){
		if(add==true){
			temp.setBalance(state.get(ACNumber).getBalance()+amount);
			state.put(ACNumber, temp);
			//System.out.println("updated state");
		}
		else{
			temp.setBalance(state.get(ACNumber).getBalance()-amount);
			state.put(ACNumber, temp);
		}
	}
}
