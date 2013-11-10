public class AliveReplicaFinder extends Process {
	ProcessId leader;
	ProcessId[] replicas;

	public AliveReplicaFinder(Env env, ProcessId me, ProcessId leader, ProcessId[] replicas){
		this.env = env;
		this.me = me;
		this.leader = leader;
		this.replicas = replicas;
		env.addProc(me, this);
	}

	@Override
	public void body(){
		for(ProcessId r:replicas){
			sendMessage(r, new FailureDetectMessage(me));
		}
		
		ProcessId AliveReplica=null;
		Long start = System.currentTimeMillis();
		Long timeout = 100L; 
		while(System.currentTimeMillis()-start < timeout){
			PaxosMessage msg = getNextMessage(timeout - System.currentTimeMillis() + start); 
			if(msg instanceof AliveMessage){
				AliveMessage m = (AliveMessage) msg;
				AliveReplica = m.src;
			}
		}
		
		sendMessage(leader, new ReplicaNumMessage(me, AliveReplica));
	}
}