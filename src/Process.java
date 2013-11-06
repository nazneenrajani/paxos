import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public abstract class Process extends Thread {
	ProcessId me;
	Queue<PaxosMessage> inbox = new Queue<PaxosMessage>();
	Env env;
	protected final Logger LOGGER = Logger.getLogger(Process.class.getSimpleName());
	protected ConsoleHandler ch;
	
	abstract void body();

	public void run(){
		body();
		env.removeProc(me);
	}

	PaxosMessage getNextMessage(){
		return inbox.bdequeue();
	}

	PaxosMessage getNextMessage(long timeout){
		return inbox.bdequeue(timeout);
	}
	
	void sendMessage(ProcessId dst, PaxosMessage msg){
		env.sendMessage(dst, msg);
	}

	void deliver(PaxosMessage msg){
		inbox.enqueue(msg);
	}
}
