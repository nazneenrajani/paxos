import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author nazneen
 *
 */
public class BankApplication extends Env {
	boolean doFullPaxos = true;
	boolean doFailureDetect = false;
	boolean doKill = false; 
	
	@Override
	void run(String[] args){
		nLeaders = 5;
		System.out.println("Current run: doFullPaxos=" + doFullPaxos + ", doFailureDetect = "+doFailureDetect);
		
		ProcessId[] acceptors = new ProcessId[nAcceptors];
		ProcessId[] replicas = new ProcessId[nReplicas];
		ProcessId[] leaders = new ProcessId[nLeaders];

		for (int i = 0; i < nAcceptors; i++) {
			acceptors[i] = new ProcessId("acceptor:" + i);
			Acceptor acc = new Acceptor(this, acceptors[i]);
		}
		for (int i = 0; i < nReplicas; i++) {
			replicas[i] = new ProcessId("replica:" + i);
			Replica repl = new Replica(this, replicas[i], leaders);
		}
		for (int i = 0; i < nLeaders; i++) {
			leaders[i] = new ProcessId("leader:" + i);
			Leader leader = new Leader(this, leaders[i], acceptors, replicas);
		}
		
		BufferedReader br = null;
		try {
			String sCurrentLine;
			int counter=0;
			br = new BufferedReader(new FileReader(args[0]));
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
				String[] c = sCurrentLine.split(",");
				boolean readOnly=false;
				if(c[2].equals("I"))
					if(!doFullPaxos)
						readOnly=true;
					ProcessId pid = new ProcessId("client:" + counter);
					for (int r = 0; r < nReplicas; r++) {
						sendMessage(replicas[r],
							new RequestMessage(pid, new Command(pid, counter, sCurrentLine, readOnly)));
					}
					counter++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		new BankApplication().run(args);
	}
}
