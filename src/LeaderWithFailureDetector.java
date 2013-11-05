import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/** Implements Failure Detector from Section 3 of Paxos Made Simple
 * 
 */

/**
 * @author Harsh
 *
 */
public class LeaderWithFailureDetector extends Leader {

	/**
	 * @param env
	 * @param me
	 * @param acceptors
	 * @param replicas
	 */
	public LeaderWithFailureDetector(Env env, ProcessId me,
			ProcessId[] acceptors, ProcessId[] replicas) {
		super(env, me, acceptors, replicas);
		ch = new ConsoleHandler();
		ch.setLevel(Level.INFO);
		LOGGER.addHandler(ch);
		LOGGER.setLevel(Level.FINE);
	}

	private long timeout = 1000L;
	private long additiveDecreaseFactor = 100L;
	private double multiplicativeIncreaseFactor = 1.1;

	public void increaseTimeout(){
		timeout *= multiplicativeIncreaseFactor;
	}

	public void decreaseTimeout(){
		timeout -= additiveDecreaseFactor;
	}

	@Override
	public void body(){
		System.out.println("Here I am: " + me);

		new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
				me, acceptors, ballot_number);
		for (;;) {
			PaxosMessage msg = getNextMessage();
			LOGGER.fine(me + " received " + msg +" from " +msg.src);
			if (msg instanceof ProposeMessage) {
				ProposeMessage m = (ProposeMessage) msg;
				if (!proposals.containsKey(m.slot_number)) {
					proposals.put(m.slot_number, m.command);
					if (active) {
						new Commander(env,
								new ProcessId("commander:" + me + ":" + ballot_number + ":" + m.slot_number),
								me, acceptors, replicas, ballot_number, m.slot_number, m.command);
					}
				}
			}
			else if (msg instanceof AdoptedMessage) {
				decreaseTimeout();

				AdoptedMessage m = (AdoptedMessage) msg;
				if (ballot_number.equals(m.ballot_number)) {
					Map<Integer, BallotNumber> max = new HashMap<Integer, BallotNumber>();
					for (PValue pv : m.accepted) {
						BallotNumber bn = max.get(pv.slot_number);
						if (bn == null || bn.compareTo(pv.ballot_number) < 0) {
							max.put(pv.slot_number, pv.ballot_number);
							proposals.put(pv.slot_number, pv.command);
						}
					}

					for (int sn : proposals.keySet()) {
						new Commander(env,
								new ProcessId("commander:" + me + ":" + ballot_number + ":" + sn),
								me, acceptors, replicas, ballot_number, sn, proposals.get(sn));
					}
					active = true;
				}
				else 
				{
					//TODO Bad? Should not happen?
				}
			}
			else if (msg instanceof PreemptedMessage) {
				increaseTimeout();
				PreemptedMessage m = (PreemptedMessage) msg;
				if (ballot_number.compareTo(m.ballot_number) < 0) {
					if(!isAlive(m.ballot_number.leader_id)){
						System.out.println(me + " Prempting "+m.ballot_number.leader_id);
						ballot_number = new BallotNumber(m.ballot_number.round + 1, me);
						new Scout(env, new ProcessId("scout:" + me + ":" + ballot_number),
								me, acceptors, ballot_number);
						active = false; //TODO should this be outside?
					}
				}
			}
			else if (msg instanceof FailureDetectMessage){
				FailureDetectMessage m = (FailureDetectMessage) msg;
				System.out.println(me + " received FailureDetect from "+m.src);
				sendMessage(m.src, new AliveMessage(me));
			}
			else {
				System.err.println("Leader: unknown msg type "+msg);
			}
		}
	}

	private boolean isAlive(ProcessId leader_id) {
		PaxosMessage pxm = new FailureDetectMessage(me);
		sendMessage(leader_id, pxm);

		Boolean isAlive = false;

		System.out.println("Process " + me + " waiting for "+leader_id);

		LinkedList<PaxosMessage> pendingMessages = new LinkedList<PaxosMessage>(); 
		Long start = System.currentTimeMillis();
		while(System.currentTimeMillis()-start < timeout){
			//TODO maybe replace this sleep with a notify / wait
			try {
				//wait(timeout - System.currentTimeMillis() + start);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PaxosMessage msg = getNextMessage();
			if(msg instanceof AliveMessage){
				isAlive = true;
				break;
			}
			else{
				pendingMessages.add(msg);
			}
		}

		for(PaxosMessage msg:pendingMessages){
			deliver(msg);
		}

		if(isAlive)
			System.out.println(leader_id + " detected alive at timeout "+ timeout + " by " +me);
		else
			System.out.println(leader_id + " detected dead at timeout "+ timeout  + " by " +me);
		return isAlive;
	}
}