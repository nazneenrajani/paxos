November 11 2013
CS 380D Distributed Computing : Project 2 Paxos
Slip days used for this project: 1
Total slip days used: 3

Team Members:
--------------
Nazneen Rajani nnr259
Harsh Pareek hhp266

How to Run:
-----------------------
The main class is BankApplication.java. We have built upon the framework provided by the Van Renesse paper.

* BankApplication.java implements Exercise 3. Provide commands.txt as argument while running
* The Failure detector from Exercise 5 is implemented in Leader.java. Set bool doFailureDetect = true to use the failure detector
* To use our code for Read Only commands, set doFullPaxos=false in BankApplication.java. If doFullPaxos=true, we treat inquiry commands as normal update commands and assign them slots etc.   
* Additionally, the lease for a ballot is specified in Ballot_number.java. 

Implementation details:
-----------------------
* For Exercise 3, our implementation reads commands from commands.txt
* For Exercise 5, we implement the Failure Detector as in   