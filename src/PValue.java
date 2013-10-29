public class PValue {
	BallotNumber ballot_number;
	int slot_number;
	Command command;

	public PValue(BallotNumber ballot_number, int slot_number,
											Command command){
		this.ballot_number = ballot_number;
		this.slot_number = slot_number;
		this.command = command;
	}

	public String toString(){
		return "PV(" + ballot_number + ", " + slot_number + ", " + command + ")";
	}
}
