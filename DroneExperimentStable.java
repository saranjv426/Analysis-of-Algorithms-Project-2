import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class DroneExperimentStable {

    // ===================== DATA CLASSES ======================
    static class Task {
        int id;
        double x, y;
        double energyCost;
        double earliestStart;
        double latestFinish;
        double serviceTime = 1.0;

        Task(int id, double x, double y, double energyCost,
             double earliestStart, double latestFinish) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.energyCost = energyCost;
            this.earliestStart = earliestStart;
            this.latestFinish = latestFinish;
        }
    }

    static class Drone {
        int id;
        double battery;
        double speed = 1.0;
        double x = 0, y = 0;
        double time = 0;

        Drone(int id, double battery) {
            this.id = id;
            this.battery = battery;
        }
    }

    static class NoFlyZone {
        double cx, cy, radius, startTime, endTime;

        NoFlyZone(double cx, double cy, double radius,
                  double startTime, double endTime) {
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    // ===================== GEOMETRY ======================
    static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
    }

    static boolean intersectsNoFly(double x1, double y1, double x2, double y2,
                                   NoFlyZone z, double t1, double t2) {
        if (t2 < z.startTime || t1 > z.endTime) return false;

        double dx = x2 - x1, dy = y2 - y1;
        double fx = x1 - z.cx, fy = y1 - z.cy;

        double a = dx*dx + dy*dy;
        double b = 2*(fx*dx + fy*dy);
        double c = fx*fx + fy*fy - z.radius*z.radius;

        return (b*b - 4*a*c) >= 0;
    }

    static boolean violatesAirspace(double x1, double y1, double x2, double y2,
                                    NoFlyZone z, double t1, double t2) {
        return intersectsNoFly(x1, y1, x2, y2, z, t1, t2);
    }

    // ===================== GREEDY ASSIGNMENT ======================
    static int runGreedy(List<Drone> drones, List<Task> tasks, NoFlyZone z) {

        Set<Task> unassigned = new HashSet<>(tasks);
        Random rand = new Random(2025);
        int completed = 0;

        for (Drone d : drones) {

            while (true) {
                Task best = null;
                double bestDist = Double.MAX_VALUE;

                for (Task t : unassigned) {

                    double dist = distance(d.x, d.y, t.x, t.y);
                    double arrival = d.time + dist / d.speed;

                    if (arrival > t.latestFinish) continue;
                    if (arrival < t.earliestStart) arrival = t.earliestStart;

                    double finish = arrival + t.serviceTime;
                    if (finish > t.latestFinish) continue;

                    double wind1 = 1.0 + (rand.nextDouble() * 0.10 - 0.05);
                    double wind2 = 1.0 + (rand.nextDouble() * 0.10 - 0.05);

                    double energyToTask = dist * wind1;
                    double returnEnergy = distance(t.x, t.y, 0, 0) * wind2;

                    if (energyToTask + t.energyCost + returnEnergy > d.battery) continue;

                    if (violatesAirspace(d.x, d.y, t.x, t.y, z, d.time, arrival)) continue;

                    if (dist < bestDist) {
                        best = t;
                        bestDist = dist;
                    }
                }

                if (best == null) break;

                double dist = distance(d.x, d.y, best.x, best.y);
                double arrival = d.time + dist / d.speed;
                if (arrival < best.earliestStart) arrival = best.earliestStart;

                double finish = arrival + best.serviceTime;

                Random r2 = new Random(999 + best.id);
                double wind1 = 1.0 + (r2.nextDouble()*0.10 - 0.05);
                double wind2 = 1.0 + (r2.nextDouble()*0.10 - 0.05);

                double energyToTask = dist * wind1;
                double returnEnergy = distance(best.x, best.y, 0, 0) * wind2;
                double totalEnergy = energyToTask + best.energyCost + returnEnergy;

                d.battery -= totalEnergy;
                d.time = finish;
                d.x = best.x;
                d.y = best.y;

                unassigned.remove(best);
                completed++;
            }
        }

        return completed;
    }

    // ===================== MAIN EXPERIMENT ======================
    public static void main(String[] args) throws Exception {

        PrintWriter csv = new PrintWriter(new FileWriter("drone_experiment_output.csv"));
        csv.println("Tasks,Drones,CompletedTasks,RuntimeMillis");

        Random rand = new Random(2025);

        double[][] clusters = {
            {20,20}, {70,30}, {40,80}
        };

        // One safe no-fly zone
        NoFlyZone safeZone = new NoFlyZone(90, 90, 8, 50, 150);

        for (int numTasks = 20; numTasks <= 200; numTasks += 20) {

            int numDrones = numTasks / 5;

            // Generate tasks
            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < numTasks; i++) {
                int cid = i % 3;
                double cx = clusters[cid][0];
                double cy = clusters[cid][1];

                double x = cx + rand.nextGaussian() * 3;
                double y = cy + rand.nextGaussian() * 3;

                double start = cid * 20;
                double earliestStart = start + rand.nextDouble() * 3;
                double latestFinish = earliestStart + 50;

                tasks.add(new Task(i, x, y, 2.0, earliestStart, latestFinish));
            }

            // Generate drones
            List<Drone> drones = new ArrayList<>();
            for (int d = 0; d < numDrones; d++) {
double battery = 79 + numTasks * 0.03;
                drones.add(new Drone(d, battery));
            }

            long startTime = System.currentTimeMillis();
            int completed = runGreedy(drones, tasks, safeZone);
            long endTime = System.currentTimeMillis();

            csv.println(numTasks + "," + numDrones + "," +
                        completed + "," + (endTime - startTime));
        }

        csv.close();
        System.out.println("Experiment complete.");
    }
}
