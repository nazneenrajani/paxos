import java.util.HashMap;


public class ReplicaState {
	HashMap<Integer,BankAccount> state;
	
	public ReplicaState(){
		state = new HashMap<Integer,BankAccount>();
	}
	public void update(Integer ACNumber, BankAccount ba, double amount, boolean add){
		for(int key:state.keySet()){
			System.out.println(key+"     "+state.get(key).getBalance()+"   "+state.get(key).getClientID());
		}
		if(!state.containsKey(ACNumber))
			state.put(ACNumber, ba);
		else{
			BankAccount temp = state.get(ACNumber);
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
}
