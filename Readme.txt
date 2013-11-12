November 11 2013
CS 380D Distributed Computing : Project 2 Paxos
Slip days used for this project: 1
Total slip days used: 3

Team Members:
--------------
Nazneen Rajani nnr259
Harsh Pareek hhp266

How to Run:
-----------
The main class is BankApplication.java. We have built upon the framework provided by the Van Renesse paper.

* BankApplication.java implements Exercise 3. Provide commands.txt as argument while running
* The Failure detector from Exercise 5 is implemented in Leader.java. Set bool doFailureDetect = true to use the failure detector
* To use our code for Read Only commands, set doFullPaxos=false in BankApplication.java. If doFullPaxos=true, we treat inquiry commands as normal update commands and assign them slots etc.   
* Additionally, the lease for a ballot and initial timeout for failure detector are specified in Ballot_number.java.
* To kill processes for testing robustness to failure, use die() in Process or killProc() in Env. 
* Set delay in Process.java to see the code in action!

Test Cases:
-----------
* Exercise 3: doFullPaxos = true. doFailureDetect = false
* Exercise 5: doFullPaxos = true. doFailureDetect = true
* ReadOnly: doFullPaxos = false. doFailureDetect = true/false. Set Lease time in BallotNumber.
* Set doKill = true to Kill Leader 4 and Kill Replica 0 after it has processed some readOnly commands

Implementation details:
-----------------------
* For Exercise 3, We added BankApplication.java and BankAccount.java. Our implementation reads commands from commands.txt, and maintains the state in ReplicaState.java.
* For Exercise 5, we implement the Failure Detector as in Section 3 of van Renesse in BallotNumber.java and Leader.java. Use doFailureDetect = true in Leader.java to turn this on.
* For the Read only commands our implementation is as follows:
There is some confusion regarding what the notion of stale actually means. In particular, Dr. Alvisi's notion of "already" in his definition of stale as "returns some value which has already been overwritten" is not 
precise in an asynchronous distributed setting. We express our notion of state through the following invariants:
- I1: Any replica which is sent a ReadOnlyDecisionMessage will eventually return the same value to the client, regardless of delays or its state at the time of receiving the request.
- I2: If our algorithm returns some output, there is some run of Full Paxos (paxos which treats read only commands as updates and assigns them slots) which would have returned the same output

Thus, the main technical challenge we address is, freeing up the slots in Full Paxos while maintaining I2.
In addition, we use Command.req_id to ensure the following:
- I3: A response to an inquiry is only sent after all the update commands before it (in the req_id sense) have been decided upon and processed at the Replica  
This is not strictly required for freshness (for example, full Paxos may not always respect it), but is intuitive and expected by the client. In the case of multiple clients, 
their req_ids can be totally ordered in the lexicographical order (command_id, client_id). 

Our design is as follows: The leader specified the slot at which the replica should respond to the client. As in Van Renesse, the leader sends the ReadOnly command as soon as it is adopted. 
To ensure that stale data is never returned to the client, the replica responds to the inquiry c with its state immediately after all client requests with req_id < c.reqid have been performed. 
It may happen that c is received at the replica after this has happened. In that case, we recompute the old state. This is required to maintain I1.   
We also implement leases at the acceptors, though this is not required for correctness in our implementation.  

Note that we do not ensure that responses to read only messages are delivered in case of Replica failure. In this case, we assume that the client will detect this through an end-to-end timeout and retransmit its request.
Use doFullPaxos = false to run this code.     