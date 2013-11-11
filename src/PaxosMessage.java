import java.util.*;

public class PaxosMessage {
	ProcessId src;
}

class P1aMessage extends PaxosMessage {
	BallotNumber ballot_number;
	P1aMessage(ProcessId src, BallotNumber ballot_number, double lease){
		this.src = src; this.ballot_number = ballot_number; lease = 0.0;
	}	}
class P1bMessage extends PaxosMessage {
	BallotNumber ballot_number; Set<PValue> accepted;
	P1bMessage(ProcessId src, BallotNumber ballot_number, Set<PValue> accepted) {
		this.src = src; this.ballot_number = ballot_number; this.accepted = accepted;
	}	}
class P2aMessage extends PaxosMessage {
	BallotNumber ballot_number; int slot_number; Command command;
	P2aMessage(ProcessId src, BallotNumber ballot_number, int slot_number, Command command){
		this.src = src; this.ballot_number = ballot_number;
		this.slot_number = slot_number; this.command = command;
	}	}
class P2bMessage extends PaxosMessage {
	BallotNumber ballot_number; int slot_number;
	P2bMessage(ProcessId src, BallotNumber ballot_number, int slot_number){
		this.src = src; this.ballot_number = ballot_number; this.slot_number = slot_number;
	}	}
class PreemptedMessage extends PaxosMessage {
	BallotNumber ballot_number;
	PreemptedMessage(ProcessId src, BallotNumber ballot_number){
		this.src = src; this.ballot_number = ballot_number;
	}	}
class AdoptedMessage extends PaxosMessage {
	BallotNumber ballot_number; Set<PValue> accepted;
	AdoptedMessage(ProcessId src, BallotNumber ballot_number, Set<PValue> accepted){
		this.src = src; this.ballot_number = ballot_number; this.accepted = accepted;
	}	}
class DecisionMessage extends PaxosMessage {
	ProcessId src; int slot_number; Command command;
	public DecisionMessage(ProcessId src, int slot_number, Command command){
		this.src = src; this.slot_number = slot_number; this.command = command;
	}	}
class RequestMessage extends PaxosMessage {
	Command command;
	public RequestMessage(ProcessId src, Command command){
		this.src = src; this.command = command;
	}	}
class ProposeMessage extends PaxosMessage {
	int slot_number; Command command;
	public ProposeMessage(ProcessId src, int slot_number, Command command){
		this.src = src; this.slot_number = slot_number; this.command = command;
	}	}

// New message classes
class FailureDetectMessage extends PaxosMessage {
	public FailureDetectMessage(ProcessId src){
		this.src = src;
	}	}
class AliveMessage extends PaxosMessage {
	public AliveMessage(ProcessId src){
		this.src = src;
}	}
class MinReplicaMessage extends PaxosMessage {
	ProcessId replicaId;
	public MinReplicaMessage(ProcessId src, ProcessId replicaId){
		this.src = src;
		this.replicaId = replicaId;
}	}
class GetMinReplicaMessage extends PaxosMessage {
	public GetMinReplicaMessage(ProcessId src){
		this.src = src;
}	}
class ReadOnlyDecisionMessage extends PaxosMessage {
	ProcessId src; Command command;
	public ReadOnlyDecisionMessage(ProcessId src, Command command){
		this.src = src;  this.command = command;
	}	}
class ReadOnlyProposeMessage extends PaxosMessage {
	Command command;
	public ReadOnlyProposeMessage(ProcessId src, Command command){
		this.src = src; this.command = command;
	}	}
class ReadOnlyPreemptedMessage extends PaxosMessage {
	BallotNumber ballot_number; Command command;
	ReadOnlyPreemptedMessage(ProcessId src, BallotNumber ballot_number, Command c){
		this.src = src; this.ballot_number = ballot_number; this.command = c;
	}	}
class RemoveReadOnly extends PaxosMessage{
	Command command;
	RemoveReadOnly(ProcessId src, Command c) {
		this.src=src;this.command=c;
	}
}
