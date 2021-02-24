# Bayesian_network
A repository for the assignment of the course Algorithms in AI.

# Project explanation

This project is an implementation of a Bayesian Network in Java.
Three algorithms are implemented, each algorithm answer to a query of the form : P(A|B,C).
- The first algorithm is simply a prediction using bayes rules (Mode1).
- The second algorithm is the Variable Elimination Algorithm (Mode2 - https://en.wikipedia.org/wiki/Variable_elimination).
- The third algorithm is the same that the second algorithm except that the order of the factors is determined such as the complexity of the algorithm should be more efficient (Inner Class in Mode2- sortHiddensAlgo3).

# More about the algorithm 3

From Wikipedia (https://en.wikipedia.org/wiki/Variable_elimination).

```
Ordering

Finding the optimal order in which to eliminate variables is an NP-hard problem. As such there are heuristics one may follow to better optimize performance by order:

    Minimum Degree: Eliminate the variable which results in constructing the smallest factor possible.
    Minimum Fill: By constructing an undirected graph showing variable relations expressed by all CPTs, eliminate the variable which would result in the least edges to be added post elimination.
```

This implementation of the algorithm use a greedy method, which minimize the number of neighbors.
Source: https://cedar.buffalo.edu/~srihari/CSE674/Chap9/9.3-VE-Algorithm.pdf slide 28.

# Run configuration

To run the program you need a txt file include the bayesian network and the queries as the next:

```
Network
Variables: B,E,A,J,M

Var B
Values: true, false
Parents: none
CPT:
=true,0.001

Var E
Values: true, false
Parents: none
CPT:
=true,0.002

Var A
Values: true, false
Parents: B,E
CPT:
true,true,=true,0.95
true,false,=true,0.94
false,true,=true,0.29
false,false,=true,0.001

Var J
Values: true, false
Parents: A
CPT:
true,=true,0.9
false,=true,0.05

Var M
Values: true, false
Parents: A
CPT:
true,=true,0.7
false,=true,0.01

Queries
P(B=true|J=true,M=true),1
P(B=true|J=true,M=true),2
P(B=true|J=true,M=true),3
P(J=true|B=true),1
P(J=true|B=true),2
P(J=true|B=true),3
```
Note: In the Queries paragraph, after each query, the mode of the running algorithm is specified.

Attention: The spaces, the commas, the line return, and the order of the Variables (parents before children) must be respected.

#The Output

In the end of the program running you should find an "output.txt" file which contains the result of the queries as the next:
```
0.28417,7,32
0.28417,7,16
0.28417,7,16
0.84902,15,64
0.84902,7,14
0.84902,5,10
```

In each line of output we have first the query answer, then the number of additions required, and the number of multiplications required.
As you can see the number of calculations decreases when we perform a more advanced algorithm.

# What contains the repository?

This repository includes the next folders:
- inputs: In this folder are found the inputs in txt files.
- src: In this folder the source code in java.

