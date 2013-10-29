import java.util.*;

 public class Queue<T> {
	LinkedList<T> ll = new LinkedList<T>();

	public synchronized void enqueue(T obj){
		ll.add(obj);
		notify();
	}

	public synchronized T bdequeue(){
		while (ll.size() == 0) {
			try { wait(); } catch (InterruptedException e) {}
		}
		return ll.removeFirst();
	}
}
