import java.util.*;

public class Replica extends Process {
	ProcessId[] leaders;
	ReplicaState replicaState;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();
	ArrayList<Boolean> completed_requests = new ArrayList<Boolean>();
	Set<Command> incomplete_readOnly = new TreeSet<Command>();

	public Replica(Env env, ProcessId me, ProcessId[] leaders){
		this.env = env;
		this.me = me;
		this.leaders = leaders;
		env.addProc(me, this);
		this.replicaState = new ReplicaState();
		replicaState.id=me;
	}

	void propose(Command c){
		completed_requests.add(c.req_id,false);
		if(c.readOnly==true){
			for (ProcessId ldr: leaders) {
				sendMessage(ldr, new ReadOnlyProposeMessage(me, c));
			}
		}
		else{
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
		completed_requests.set(c.req_id,true);
		updateCommandNumber();
		performIncompleteReadOnly();
	}
	
	private void updateCommandNumber(){
		while(replicaState.command_number<completed_requests.size() && completed_requests.get(replicaState.command_number)==true)
			replicaState.command_number++;
	}

	private void performIncompleteReadOnly() {
		if(incomplete_readOnly.size()>0) 
			System.out.println("Pending ReadOnly requests at "+ me + " :" +incomplete_readOnly);
		
		Iterator<Command> it = incomplete_readOnly.iterator(); 
		while(it.hasNext()){
			Command cr = it.next();
			if(cr.req_id==replicaState.command_number){				
				performOperation(cr);
				completed_requests.set(cr.req_id,true);
				updateCommandNumber();
				it.remove();
			}
			else if(completed_requests.get(cr.req_id)==true){
				it.remove();
			}
		}
	}

		private void performOperation(Command c) {
			String[] operation = c.op.toString().split(",");
			switch(operation[2]){
			case "D":
				deposit(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),Double.parseDouble(operation[3]));
				break;
			case "W":
				withdraw(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),Double.parseDouble(operation[3]));
				break;
			case "I":
				inquiry(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),c.req_id);
				break;
			case "T":
				transfer(Integer.parseInt(operation[0]),Integer.parseInt(operation[1]),Double.parseDouble(operation[3]), Integer.parseInt(operation[4]),Integer.parseInt(operation[5]));
				break;
			default:
				System.err.println("Unknown command type "+operation[2]);
			}

		}

		private void transfer(int clientid1, int acnumber1, double amount,
				int clientid2, int acnumber2) {
			replicaState.update(acnumber1, clientid1, amount, false, replicaState.state);
			replicaState.update(acnumber2, clientid2, amount, true,replicaState.state);

		}

		private void inquiry(int clientid, int acnumber, int req_id) {
			System.out.print("At " + me +", ");
			if(!replicaState.state.containsKey(acnumber))
				System.out.println("The AC number: "+acnumber+" for client: "+clientid+" does not exist");
			else
				System.out.println("Inquiry command output: "+req_id+" for AC number: "+acnumber+" for client: "+clientid+" is "+replicaState.state.get(acnumber).balance);
		}

		private void withdraw(int clientid, int acnumber, double amount) {
			replicaState.update(acnumber, clientid, amount, false, replicaState.state);

		}

		private void deposit(int clientid, int acnumber, double amount) {
			replicaState.update(acnumber, clientid, amount, true, replicaState.state);
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
					//printSlots();
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
				else if (msg instanceof FailureDetectMessage) {
					FailureDetectMessage m = (FailureDetectMessage) msg;
					//System.out.println(me + " received FailureDetect from "+m.src);
					sendMessage(m.src, new AliveMessage(me));
				}
				else if (msg instanceof ReadOnlyDecisionMessage) {
					ReadOnlyDecisionMessage m = (ReadOnlyDecisionMessage) msg;
					System.out.println("" + me + ": perform " + m.command);
					for(ProcessId l:leaders){
						sendMessage(l, new RemoveReadOnly(me, m.command));
					}
					// TODO handle sending old state to already performed cids
					incomplete_readOnly.add(m.command);
					performIncompleteReadOnly();
				}
				else {
					System.err.println("Replica: unknown msg type");
				}
			}
		}
		
		private void printSlots(){
			System.out.print("Slots at "+me+" ");
			for(int s=1;s<slot_num;s++)
				System.out.print(s+":"+decisions.get(s)+",");
			System.out.println("");
		}
	}
