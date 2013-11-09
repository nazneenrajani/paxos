import java.util.*;

public class Replica extends Process {
	ProcessId[] leaders;
	ReplicaState replicaState;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();
	//TODO handle decisions in leader for read-only
	
	public Replica(Env env, ProcessId me, ProcessId[] leaders){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
		env.addProc(me, this);
		this.replicaState = new ReplicaState();
		replicaState.id=me;
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
		performOperation(c);
		slot_num++;
	}

	private void performOperation(Command c) {
		String[] operation = c.op.toString().split(",");
		switch(operation[2]){
		case "D":
			System.out.println("Performing deposit");
			deposit(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),Double.parseDouble(operation[3]));
			System.out.println("Done with deposit");
			break;
		case "W":
			withdraw(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),Double.parseDouble(operation[3]));
			break;
		case "I":
			c.readOnly=true;
			inquiry(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]));
			break;
		case "T":
			transfer(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),Double.parseDouble(operation[3]), Integer.parseInt(operation[4]),Integer.parseInt(operation[5]));
			break;
		default:
			//TODO
		}
		
	}

	private void transfer(int clientid1, int acnumber1, double amount,
			int clientid2, int acnumber2) {
		replicaState.update(acnumber1, clientid1, amount, false, replicaState.state);
		replicaState.update(acnumber2, clientid2, amount, true,replicaState.state);
		
	}

	private void inquiry(int clientid, int acnumber) {
		// TODO Auto-generated method stub
		
	}

	private void withdraw(int clientid, int acnumber, double amount) {
		replicaState.update(acnumber, clientid, amount, false, replicaState.state);
		
	}

	private void deposit(int clientid, int acnumber, double amount) {
		replicaState.update(acnumber, clientid, amount, true, replicaState.state);
		System.out.println("In deposit");
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
