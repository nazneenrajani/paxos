public class ProcessId implements Comparable {
	String name;

	public ProcessId(String name){ this.name = name; }

	public boolean equals(Object other){
		return name.equals(((ProcessId) other).name);
	}

	public int compareTo(Object other){
		return name.compareTo(((ProcessId) other).name);
	}

	public String toString(){ return name; }
}
