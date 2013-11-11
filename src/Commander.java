import java.util.*;

public class Commander extends Process {
	ProcessId leader;
	ProcessId arf;
	ProcessId[] acceptors, replicas;
	BallotNumber ballot_number;
	int slot_number;
	Command command;

	public Commander(Env env, ProcessId me, ProcessId leader, ProcessId arf, ProcessId[] acceptors,
			ProcessId[] replicas, BallotNumber ballot_number, int slot_number, Command command){
		this.env = env;
		this.me = me;
		this.acceptors = acceptors;
		this.replicas = replicas;
		this.leader = leader;
		this.arf = arf;
		this.ballot_number = ballot_number;
		this.slot_number = slot_number;
		this.command = command;
		env.addProc(me, this);
	}

	public void body(){
		if(!command.readOnly){
			//System.out.println("Not a read only command");
			P2aMessage m2 = new P2aMessage(me, ballot_number, slot_number, command);
			Set<ProcessId> waitfor = new HashSet<ProcessId>();
			for (ProcessId a: acceptors) {
				sendMessage(a, m2);
				waitfor.add(a);
			}

			while (2 * waitfor.size() >= acceptors.length) {
				PaxosMessage msg = getNextMessage();

				if (msg instanceof P2bMessage) {
					P2bMessage m = (P2bMessage) msg;

					if (ballot_number.equals(m.ballot_number)) {
						if (waitfor.contains(m.src)) {
							waitfor.remove(m.src);
						}
					}
					else {
						sendMessage(leader, new PreemptedMessage(me, m.ballot_number));
						return;
					}
				}
			}
			for (ProcessId r: replicas) {
				sendMessage(r, new DecisionMessage(me, slot_number, command));
			}
		}
		else{

			if(System.currentTimeMillis()-ballot_number.start_lease < ballot_number.lease_time){
				sendMessage(arf, new GetMinReplicaMessage(me));
				MinReplicaMessage m= (MinReplicaMessage) getNextMessage();
				sendMessage(m.replicaId, new ReadOnlyDecisionMessage(me, command));
				
				//sendMessage(replicas[0], new ReadOnlyDecisionMessage(me, command));
			}
			else{
				sendMessage(leader, new ReadOnlyPreemptedMessage(me, ballot_number,command));
			}


		}
	}
}
