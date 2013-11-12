import java.util.*;

public class Leader extends Process {	
	ProcessId[] acceptors;
	ProcessId[] replicas;
	BallotNumber ballot_number;
	boolean active = false;
	Map<Integer, Command> proposals = new HashMap<Integer, Command>();
	Set<Command> readOnly = new HashSet<Command>();

	Boolean doFailureDetect = false;

	private long timeout = 1000L;
	private long additiveDecreaseFactor = 100L;
	private double multiplicativeIncreaseFactor = 1.1;

	public void increaseTimeout(){
		timeout *= multiplicativeIncreaseFactor;
	}

	public void decreaseTimeout(){
		timeout -= additiveDecreaseFactor;
	}

	public Leader(Env env, ProcessId me, ProcessId[] acceptors,
			ProcessId[] replicas){
		this.env = env;
		this.me = me;
		ballot_number = new BallotNumber(0, me);
		this.acceptors = acceptors;
		this.replicas = replicas;
		env.addProc(me, this);
	}

	public void body(){
		System.out.println("Here I am: " + me);

		ProcessId arf = new ProcessId("AliveReplicaFinder:" + me);
		new AliveReplicaFinder(env, arf, replicas);
		new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
				me, acceptors, ballot_number);
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof ProposeMessage) {
				ProposeMessage m = (ProposeMessage) msg;
				die("leader:3");
				if (!proposals.containsKey(m.slot_number)) {
					proposals.put(m.slot_number, m.command);
					if (active) {
						new Commander(env,
								new ProcessId("commander:" + me + ":" + ballot_number + ":" + m.slot_number),
								me, arf, acceptors, replicas, ballot_number, m.slot_number, m.command);
					}
				}
			}
			else if (msg instanceof ReadOnlyProposeMessage) {
				ReadOnlyProposeMessage m = (ReadOnlyProposeMessage) msg;
				readOnly.add(m.command);
				if (active) {
					new Commander(env,
							new ProcessId("commander:" + me + ":" + ballot_number+":"+"ReadOnly"+":"+m.command.req_id),
							me, arf, acceptors, replicas, ballot_number,-1, m.command);
				}
			}
			else if (msg instanceof AdoptedMessage) {
				decreaseTimeout();
				die("leader:4");
				System.out.println(me+ " is adopted as leader");
				AdoptedMessage m = (AdoptedMessage) msg;

				if (ballot_number.equals(m.ballot_number)) {
					Map<Integer, BallotNumber> max = new HashMap<Integer, BallotNumber>();
					for (PValue pv : m.accepted) {
						BallotNumber bn = max.get(pv.slot_number);
						if (bn == null || bn.compareTo(pv.ballot_number) < 0) {
							max.put(pv.slot_number, pv.ballot_number);
							proposals.put(pv.slot_number, pv.command);
						}
					}

					for (int sn : proposals.keySet()) {
						new Commander(env,
								new ProcessId("commander:" + me + ":" + ballot_number + ":" + sn),
								me, arf, acceptors, replicas, ballot_number, sn, proposals.get(sn));
					}
					for (Command c : readOnly) {
						new Commander(env,
								new ProcessId("commander:" + me + ":" + ballot_number + ":" +"ReadOnly"+":"+c.req_id),
								me, arf, acceptors, replicas, ballot_number, -1, c);
					}
					active = true;
				}
			}

			else if (msg instanceof PreemptedMessage) {
				increaseTimeout();

				PreemptedMessage m = (PreemptedMessage) msg;
				System.out.println(me + " is preempted. Active leader is "+m.ballot_number.leader_id);
				if (ballot_number.compareTo(m.ballot_number) < 0) {
					if(!isAlive(m.ballot_number.leader_id)){
						System.out.println(me + " Prempting "+m.ballot_number.leader_id);
						ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
						new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
								me, acceptors, ballot_number);
						active = false;
					}
				}
			}

			else if (msg instanceof ReadOnlyPreemptedMessage) {
				System.out.println(me + "was preempted due to lease expiry");

				ReadOnlyPreemptedMessage m = (ReadOnlyPreemptedMessage) msg;
				readOnly.add(m.command);
				if (ballot_number.compareTo(m.ballot_number) < 0) {
					ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
					new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
							me, acceptors, ballot_number);
					active = false;
				}
			}
			else if (msg instanceof RemoveReadOnly) {
				RemoveReadOnly m = (RemoveReadOnly) msg;
				readOnly.remove(m.command);
			}
			else if (msg instanceof FailureDetectMessage) {
				FailureDetectMessage m = (FailureDetectMessage) msg;
				//System.out.println(me + " received FailureDetect from "+m.src);
				sendMessage(m.src, new AliveMessage(me));
			}
			else if (msg instanceof AliveMessage){
				//drop
			}
			else {
				System.err.println("Leader: unknown msg type "+ msg);
			}
		}
	}

	private boolean isAlive(ProcessId leader_id) {
		if(!doFailureDetect)
			return false;
		PaxosMessage pxm = new FailureDetectMessage(me);
		sendMessage(leader_id, pxm);

		Boolean isAlive = false;

		System.out.println("Process " + me + " waiting for "+leader_id);

		LinkedList<PaxosMessage> pendingMessages = new LinkedList<PaxosMessage>(); 
		Long start = System.currentTimeMillis();
		while(System.currentTimeMillis()-start < timeout){
			PaxosMessage msg = getNextMessage(timeout - System.currentTimeMillis() + start); 
			// getNextMessage has a wait inside, so this is not a busy wait loop. 
			// getNextMessage() blocks after timeout and doesn't proceed while getNextMessage(long) returns null if it fails
			// RHS guaranteed > 0 ?
			if(msg instanceof AliveMessage){
				isAlive = true;
				break;
			}
			else{
				if(msg!=null)
					pendingMessages.add(msg);
			}
		}

		for(PaxosMessage msg:pendingMessages){
			deliver(msg);
		}

		if(isAlive)
			System.out.println(leader_id + " detected alive at timeout "+ timeout + " by " +me);
		else
			System.out.println(leader_id + " detected dead at timeout "+ timeout  + " by " +me);
		return isAlive;
	}
}
