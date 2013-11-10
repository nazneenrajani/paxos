public class BallotNumber implements Comparable {
	int round;
	ProcessId leader_id;
	long lease_time;
	long start_lease;
	
	public BallotNumber(int round, ProcessId leader_id){
		this.round = round;
		this.leader_id = leader_id;
		this.lease_time=5000L;
	}

	public boolean equals(Object other){
		return compareTo(other) == 0;
	}

	public int compareTo(Object other){
		BallotNumber bn = (BallotNumber) other;
		if (bn.round != round) {
			return round - bn.round;
		}
		return leader_id.compareTo(bn.leader_id);
	}

	public String toString(){
		return "BN(" + round + ", " + leader_id + ")";
	}
}
