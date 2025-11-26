# Project 2 — Network Flow Reduction & NP-Hardness Analysis  
**Name:** Venkata Sai Saran Jonnalagadda  
**UFID:** 11114995  

---

## Overviewx`

This project presents the analysis, reduction, and implementation of two real-world computational problems:

1. **Problem 1 — Referee Scheduling**  
   A sports scheduling problem that is reduced to a **Maximum Flow** instance.

2. **Problem 2 — Autonomous Drone Task Assignment Problem (ADTAP)**  
   A logistics problem shown to be **NP-Hard**, solved in practice using a **greedy heuristic**.

The project includes complete mathematical formulations, correctness proofs, algorithms, Java implementations, experimental evaluations, and performance plots.

---

# Problem 1 — Referee Scheduling via Network Flow

## Real-World Context  
Large youth sports tournaments involve hundreds of matches that must be assigned to referees with different:

- Certification levels  
- Daily workload limits  
- Availability/time windows  

Conflicting matches cannot be assigned to the same referee.

## Reduction to Maximum Flow  
The scheduling system is modeled as a **capacitated bipartite graph**:

- Nodes: Referee-day nodes and match nodes  
- Edges: From referees → matches (if certified & no conflict)  
- Source → Referee (capacity = work hours)  
- Match → Sink (capacity = match duration)

This follows classical flow-modeling literature.

We solve it using **Dinic’s Algorithm**.

## Experimental Results  
Experiments evaluate:

- Feasibility vs tournament size  
- Runtime scaling  

Runtime stays between **0–2 ms** even for 200 matches.  
Plots available in the repository.

---

# Problem 2 — Autonomous Drone Task Assignment (NP-Hard)

## Real-World Context  
A fleet of drones must service spatially distributed tasks under constraints:

- Battery capacity  
- Travel energy and return-to-base energy  
- Time windows  
- Mild wind variation (±5%)  
- A small no-fly zone  
- Geographic clustering of tasks  

This resembles logistics systems like Amazon Prime Air, Zipline, etc.

## NP-Hardness  
We prove ADTAP is NP-Hard via reduction from the **Vehicle Routing Problem (VRP)**:

- VRP customers → ADTAP tasks  
- VRP vehicles → ADTAP drones  
- Route length → Battery capacity

Since VRP is strongly NP-hard, ADTAP is also NP-hard.

## Greedy Heuristic  
A **Nearest-Feasible-Task** greedy algorithm is implemented:

1. Drone selects the nearest feasible unassigned task  
2. Checks if battery allows travel + return  
3. Assigns the task, updates battery  
4. Continues until infeasible  

Runtime: **O(m · n²)**

## Experimental Results  
We simulate tasks in 3 clusters with time windows and mild wind variation.

Metrics:

- Completed tasks vs total tasks  
- Runtime vs number of tasks  

Results show:

- Clear diminishing-returns curve (fleet saturation around ~55–60 tasks)  
- Runtime < 3 ms for all tested sizes  

Plots included.

### How to run

## Running problem-1
Compile - javac RefereeSchedulerExperiment.java
Run - java RefereeSchedulerExperiment

This generates:
-> feasibility_plot.png
-> runtime_plot.png
-> referee_output.csv

## Running problem-2
Compile - javac DroneExperimentStable.java
Run - java DroneExperimentStable

This generates:
-> drone_experiment_output.csv
-> Completed tasks plot (via Python)
-> Runtime plot (via Python)


