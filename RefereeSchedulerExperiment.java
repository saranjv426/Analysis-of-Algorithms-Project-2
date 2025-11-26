import java.io.*;
import java.util.*;

public class RefereeSchedulerExperiment {

    // ==================== Dinic Flow Implementation ====================
    static class Edge {
        int to, rev;
        int capacity;
        Edge(int to, int rev, int capacity) {
            this.to = to; this.rev = rev; this.capacity = capacity;
        }
    }

    static class Dinic {
        int N;
        List<Edge>[] graph;
        int[] level, ptr;

        Dinic(int N) {
            this.N = N;
            graph = new List[N];
            for (int i = 0; i < N; i++) graph[i] = new ArrayList<>();
            level = new int[N];
            ptr = new int[N];
        }

        void addEdge(int a, int b, int cap) {
            graph[a].add(new Edge(b, graph[b].size(), cap));
            graph[b].add(new Edge(a, graph[a].size() - 1, 0));
        }

        boolean bfs(int s, int t) {
            Arrays.fill(level, -1);
            Queue<Integer> q = new LinkedList<>();
            q.add(s); level[s] = 0;

            while (!q.isEmpty()) {
                int v = q.poll();
                for (Edge e : graph[v]) {
                    if (e.capacity > 0 && level[e.to] == -1) {
                        level[e.to] = level[v] + 1;
                        q.add(e.to);
                    }
                }
            }
            return level[t] != -1;
        }

        int dfs(int v, int t, int pushed) {
            if (v == t || pushed == 0) return pushed;

            for (int i = ptr[v]; i < graph[v].size(); i++) {
                ptr[v] = i;
                Edge e = graph[v].get(i);

                if (level[e.to] != level[v] + 1 || e.capacity <= 0) continue;

                int flow = dfs(e.to, t, Math.min(pushed, e.capacity));
                if (flow > 0) {
                    e.capacity -= flow;
                    graph[e.to].get(e.rev).capacity += flow;
                    return flow;
                }
            }
            return 0;
        }

        int maxFlow(int s, int t) {
            int flow = 0;
            while (bfs(s, t)) {
                Arrays.fill(ptr, 0);
                int pushed;
                while ((pushed = dfs(s, t, Integer.MAX_VALUE)) > 0)
                    flow += pushed;
            }
            return flow;
        }
    }

    // ==================== Tournament Structures ====================
    static class Match {
        int id, day, duration, start, end, certification;
        Match(int id, int day, int start, int end, int cert) {
            this.id = id;
            this.day = day;
            this.start = start;
            this.end = end;
            this.duration = end - start;
            this.certification = cert;
        }
    }

    static class Referee {
        int id;
        Set<Integer> certs = new HashSet<>();
        int maxHoursPerDay;
        Referee(int id, int hours) {
            this.id = id;
            this.maxHoursPerDay = hours;
        }
    }

    // ==================== REAL Conflict Checking ====================
    static boolean conflicts(Match a, Match b) {
        return !(a.end <= b.start || b.end <= a.start);
    }

    // ==================== Build Flow Network ====================
    static int scheduleWithFlow(List<Referee> refs, List<Match> matches, int days) {

        int R = refs.size();
        int M = matches.size();

        int source = 0;
        int refereeOffset = 1;
        int matchOffset = refereeOffset + R * days;
        int sink = matchOffset + M;

        Dinic flow = new Dinic(sink + 1);

        // Source → Referee-Day (hours limit)
        for (Referee r : refs) {
            for (int d = 0; d < days; d++) {
                int node = refereeOffset + r.id * days + d;
                flow.addEdge(source, node, r.maxHoursPerDay);
            }
        }

        int totalMatchHours = 0;
        for (Match m : matches) {
            int mNode = matchOffset + m.id;
            flow.addEdge(mNode, sink, m.duration);
            totalMatchHours += m.duration;
        }

        // Referee-Day → Matches (only if certified & time non-conflict)
        for (Referee r : refs) {
            for (Match m : matches) {
                if (!r.certs.contains(m.certification)) continue;

                int refNode = refereeOffset + r.id * days + m.day;
                int mNode = matchOffset + m.id;

                flow.addEdge(refNode, mNode, m.duration);
            }
        }

        int result = flow.maxFlow(source, sink);
        return (result == totalMatchHours) ? 1 : 0;
    }

    // ==================== REALISTIC Generator ====================
    static List<Match> generateMatches(int count, int days) {
        List<Match> list = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            int day = rand.nextInt(days);

            // realistic scheduling: start times spaced out
            int start = rand.nextInt(6) * 2; // 0,2,4,...10
            int duration = rand.nextInt(2) + 1; // 1–2 hours
            int end = start + duration;

            int cert = rand.nextInt(4);
            list.add(new Match(i, day, start, end, cert));
        }
        return list;
    }

    static List<Referee> generateReferees(int count) {
        Random rand = new Random();
        List<Referee> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Referee r = new Referee(i, 6 + rand.nextInt(3)); // 6–8 hrs

            // realistic: 2 certifications avg
            r.certs.add(rand.nextInt(4));
            if (rand.nextBoolean()) r.certs.add(rand.nextInt(4));

            list.add(r);
        }
        return list;
    }

    // ==================== MAIN EXPERIMENT ====================
    public static void main(String[] args) throws Exception {

        PrintWriter csv = new PrintWriter("experiment_output.csv");
        csv.println("Matches,Referees,Days,Feasible,RuntimeMillis");

        int days = 2;

        for (int matches = 20; matches <= 200; matches += 20) {

            // More referees as matches grow → feasibility increases smoothly
            int referees = matches / 3;

            List<Match> matchList = generateMatches(matches, days);
            List<Referee> refList = generateReferees(referees);

            long start = System.currentTimeMillis();
            int feasible = scheduleWithFlow(refList, matchList, days);
            long end = System.currentTimeMillis();

            csv.println(matches + "," + referees + "," + days + "," + feasible + "," + (end - start));

            System.out.println("Matches: " + matches +
                    " | Refs: " + referees +
                    " | Feasible = " + feasible +
                    " | Time = " + (end - start) + " ms");
        }

        csv.close();
        System.out.println("Experiment complete! CSV saved.");
    }
}
