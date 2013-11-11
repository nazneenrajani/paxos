import java.util.Arrays;

public class AliveReplicaFinder extends Process {
	ProcessId leader;
	ProcessId[] replicas;
	Boolean[] AliveReplicas;
	ProcessId minAliveReplica=null;

	public AliveReplicaFinder(Env env, ProcessId me, ProcessId[] replicas){
		this.env = env;
		this.me = me;
		this.replicas = replicas;
		env.addProc(me, this);
	}

	@Override
	public void body(){
		AliveReplicas = new Boolean[replicas.length];
		for(int i=0; i<AliveReplicas.length; i++){
			AliveReplicas[i]=false;
		}
		while(true){
			System.out.println(me +" "+ Arrays.toString(AliveReplicas));
			for(ProcessId r:replicas){
				sendMessage(r, new FailureDetectMessage(me));
			}

			Boolean[] responded = new Boolean[replicas.length];
			for(int i=0; i<responded.length; i++){
				responded[i]=false;
			}
			updateMinAliveReplica();

			Long start = System.currentTimeMillis();
			Long timeout = 2000L;
			while(System.currentTimeMillis()-start < timeout){
				PaxosMessage msg = getNextMessage(timeout - System.currentTimeMillis() + start);
				if(msg==null)
					continue;
				if(msg instanceof AliveMessage){
					AliveMessage m = (AliveMessage) msg;
					String[] s = m.src.name.split(":");
					int index = Integer.parseInt(s[1]);
					responded[index]=true;
					AliveReplicas[index]=responded[index];
					updateMinAliveReplica();
				} else if (msg instanceof GetMinReplicaMessage){
					if(minAliveReplica==null){
						deliver(msg);
						try {
							Thread.sleep(100L);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}
					sendMessage(msg.src, new MinReplicaMessage(me, minAliveReplica));
				} else {
					System.err.println("AliveReplicaFinder: Unknown message type "+msg);
				}
			}
			for(int i=0; i<AliveReplicas.length; i++){
				AliveReplicas[i]=responded[i];
			}
		}
	}
	
	private void updateMinAliveReplica(){
		for(int i=0;i<AliveReplicas.length;i++) {
			if(AliveReplicas[i]){
				minAliveReplica = replicas[i];
				break;
			}						
		}
	}
}