import java.util.*;

public class Replica extends Process {
	ProcessId[] leaders;
	ReplicaState state;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();

	public Replica(Env env, ProcessId me, ProcessId[] leaders){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
		env.addProc(me, this);
		this.state = new ReplicaState();
	}

	void propose(Command c){
		if (!decisions.containsValue(c)) {
			for (int s = 1;; s++) {
				if (!proposals.containsKey(s) && !decisions.containsKey(s)) {
					proposals.put(s, c);
					for (ProcessId ldr: leaders) {
						sendMessage(ldr, new ProposeMessage(me, s, c));
					}
					break;
				}
			}
		}
	}

	void perform(Command c){
		for (int s = 1; s < slot_num; s++) {
			if (c.equals(decisions.get(s))) {
				slot_num++;
				return;
			}
		}
		System.out.println("" + me + ": perform " + c);
		//performOperation(c);
		slot_num++;
	}

	private void performOperation(Command c) {
		String[] operation = c.op.toString().split(",");
		switch(operation[2]){
		case "D":
			deposit(new BankAccount(Integer.parseInt(operation[0]),Integer.parseInt(operation[1])),Double.parseDouble(operation[3]));
			break;
		case "W":
			withdraw(new BankAccount(Integer.parseInt(operation[0]),Integer.parseInt(operation[1])),Double.parseDouble(operation[3]));
			break;
		case "I":
			c.readOnly=true;
			inquiry(new BankAccount(Integer.parseInt(operation[0]),Integer.parseInt(operation[1])));
			break;
		case "T":
			transfer(new BankAccount(Integer.parseInt(operation[0]),Integer.parseInt(operation[1])),Double.parseDouble(operation[3]), new BankAccount(Integer.parseInt(operation[4]),Integer.parseInt(operation[5])));
			break;
		default:
			//TODO
		}
		
	}

	private void transfer(BankAccount bankAccount, double amount,
			BankAccount bankAccount2) {
		state.update(bankAccount.getACNumber(), bankAccount, amount, false);
		state.update(bankAccount2.getACNumber(), bankAccount2, amount, true);
		
	}

	private void inquiry(BankAccount bankAccount) {
		// TODO Auto-generated method stub
		
	}

	private void withdraw(BankAccount bankAccount, double amount) {
		state.update(bankAccount.getACNumber(), bankAccount, amount, false);
		
	}

	private void deposit(BankAccount bankAccount, double amount) {
		state.update(bankAccount.getACNumber(), bankAccount, amount, true);
		
	}

	public void body(){
		System.out.println("Here I am: " + me);
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof RequestMessage) {
				RequestMessage m = (RequestMessage) msg;
				propose(m.command);
			}

			else if (msg instanceof DecisionMessage) {
				DecisionMessage m = (DecisionMessage) msg;
				decisions.put(m.slot_number, m.command);
				for (;;) {
					Command c = decisions.get(slot_num);
					if (c == null) {
						break;
					}
					Command c2 = proposals.get(slot_num);
					if (c2 != null && !c2.equals(c)) {
						propose(c2);
					}
					perform(c);
				}
			}
			else {
				System.err.println("Replica: unknown msg type");
			}
		}
	}
}
