package main.java.com.missions;

import main.java.com.structures.Djikstra;

import java.util.Scanner;

public class MissionTwo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int T = sc.nextInt();

        for (int i = 1; i <= T; i++) {
            int N = sc.nextInt();
            int C = sc.nextInt();
            int S = sc.nextInt();
            int D = sc.nextInt();

            Djikstra d = new Djikstra(N);

            for (int j = 0; j < C; j++) {
                int A = sc.nextInt();
                int B = sc.nextInt();
                int W = sc.nextInt();
                d.addEdge(A, B, W);
            }

            int dist = d.dijkstra(S, D);

            if (dist != Integer.MAX_VALUE) {
                System.out.printf("Case #%d: %d%n", i, dist);
            } else {
                System.out.printf("Case #%d: Nina is very sad%n", i);
            }
        }
    }
}