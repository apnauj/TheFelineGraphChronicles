package main.java.com.missions;

import main.java.com.structures.Djikstra;

import java.util.Arrays;
import java.util.Scanner;

public class MissionTwo {
    static void main() {
        Scanner sc = new Scanner(System.in);
        int T = Integer.parseInt(sc.nextLine());
        int N, C;
        int[] NCSD;
        for (int i = 0; i < T; i++) {
            NCSD = Arrays.stream(sc.nextLine().trim().split("\\s+"))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            N = NCSD[0];
            Djikstra d = new Djikstra(N);
            C = NCSD[1];
            int A, B, W;
            int[] ABW;
            for (int j = 0; j < C; j++) {
                ABW = Arrays.stream(sc.nextLine().trim().split("\\s+"))
                        .mapToInt(Integer::parseInt)
                        .toArray();
                A = ABW[0];
                B = ABW[1];
                W = ABW[2];
                d.addEdge(A, B, W);
            }
            if(d.dijkstra(NCSD[2], NCSD[3]) != Integer.MAX_VALUE){
                System.out.printf("Case #%d: %d\n", (i+1), d.dijkstra(NCSD[2], NCSD[3]));
            } else {
                System.out.printf("Case #%d: Nina is very sad\n", (i+1));
            }
        }
    }
}
