import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public abstract class Process extends Thread {
	ProcessId me;
	Queue<PaxosMessage> inbox = new Queue<PaxosMessage>();
	Env env;
	long delay = 500L;
	protected final Logger LOGGER = Logger.getLogger(Process.class.getSimpleName());
	protected ConsoleHandler ch;

	abstract void body();

	public void run(){
		body();
		env.removeProc(me);
	}

	public void die(String name){
		if(!me.name.equals(name+",")) //TODO remove , here
			return;
		System.err.println(me+" is dying");
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Sleep interrupted in die()");
		}
	}

	PaxosMessage getNextMessage(){
		if(inbox.ll.size()==0){
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				System.err.println("getNextMessage delay sleep interrupted");
				e.printStackTrace();
			}
		}
		return inbox.bdequeue();
	}

	PaxosMessage getNextMessage(long timeout){
		if(inbox.ll.size()==0){
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				System.err.println("getNextMessage delay sleep interrupted");
				e.printStackTrace();
			}
		}
		return inbox.bdequeue(timeout);
	}

	void sendMessage(ProcessId dst, PaxosMessage msg){
		env.sendMessage(dst, msg);
	}

	void deliver(PaxosMessage msg){
		inbox.enqueue(msg);
	}
}
