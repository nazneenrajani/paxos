public class Command {
	ProcessId client;
	int req_id;
	Object op;
	Boolean readOnly;

	public Command(ProcessId client, int req_id, Object op){
		this.client = client;
		this.req_id = req_id;
		this.op = op;
		this.readOnly = false;
	}

	public boolean equals(Object o) {
		Command other = (Command) o;
		return client.equals(other.client) && req_id == other.req_id && op.equals(other.op);
	}

	public String toString(){
		return "Command(" + client + ", " + req_id + ", " + op + ")";
	}
}
