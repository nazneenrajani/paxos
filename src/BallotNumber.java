public class BallotNumber implements Comparable {
	int round;
	ProcessId leader_id;
	long lease_time=5000L;
	long start_lease;
	public long timeout = 1000L;
	private long additiveDecreaseFactor = 100L;
	private double multiplicativeIncreaseFactor = 1.1;

	
	public BallotNumber(int round, ProcessId leader_id){
		this.round = round;
		this.leader_id = leader_id;
	}
	
	public BallotNumber(int round, ProcessId leader_id, long timeout){
		this.round = round;
		this.leader_id = leader_id;
		this.timeout = timeout;
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

	public void increaseTimeout(){
		timeout *= multiplicativeIncreaseFactor;
	}

	public void decreaseTimeout(){
		timeout -= additiveDecreaseFactor;
		if(timeout<0)
			timeout=0;
	}

}
