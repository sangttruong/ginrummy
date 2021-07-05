import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class constructHand {
    static final int R = 4;
    static final int C = 13;
    static int[][] a = new int[R][C];
    static int[] rc = new int[R];
    static int[] cc = new int[C];
    static int[] drc = new int[R], drc_clone;
    static int[] dcc = new int[C], dcc_clone;
    static int melds = 0;
    static int deadwood = 0;

    static FileWriter fw;

    static int point(int x)
    {
        return x < 10 ? (x + 1) : 10;
    }

    static void print() throws IOException
    {
        for (int i = 0; i < R; i++) {
            for (int j = 0; j < C; j++)
                fw.write(a[i][j] + " ");
            fw.write('\n');
        }
        fw.write('\n');
    }

    static int run(boolean[][] chosen) {
        ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>();
        int s = 0;
        int cnt = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                if (a[i][j] == 1 && !chosen[i][j]) {
                    s++;
                    l.add(new ArrayList<Integer>(Arrays.asList(i, j)));
                } else {
                    if (s >= 3) {
                        cnt++;
                        for (ArrayList<Integer> coordinate : l)
                        {
                            chosen[coordinate.get(0)][coordinate.get(1)] = true;
                            // drc_clone[coordinate.get(0)] += point(coordinate.get(1));
                            // dcc_clone[coordinate.get(1)] += point(coordinate.get(1));
                        }
                    }
                    s = 0;
                    l.clear();
                }
            }
        }
        if (s >= 3) 
        {
            cnt++;
            for (ArrayList<Integer> coordinate : l)
            {
                chosen[coordinate.get(0)][coordinate.get(1)] = true;
                // drc_clone[coordinate.get(0)] += point(coordinate.get(1));
                // dcc_clone[coordinate.get(1)] += point(coordinate.get(1));
            }
        }
        return cnt;
    }

    static int set(boolean[][] chosen) {
        int cnt = 0;
        for (int j = 0; j < 13; j++) {
            ArrayList<ArrayList<Integer>> l = new ArrayList<ArrayList<Integer>>(); // list of "tuples"
            int s = 0;
            for (int i = 0; i < 4; i++) {
                if (a[i][j] == 1 && !chosen[i][j]) {
                    s++;
                    l.add(new ArrayList<Integer>(Arrays.asList(i, j)));
                }
            }
            if (s >= 3) {
                cnt++;
                for (ArrayList<Integer> coordinate : l)
                {
                    chosen[coordinate.get(0)][coordinate.get(1)] = true;
                    // drc_clone[coordinate.get(0)] += point(coordinate.get(1));
                    // dcc_clone[coordinate.get(1)] += point(coordinate.get(1));
                }
            }
        }
        return cnt;
    }

    static boolean check() throws IOException 
    {
        for (int i = 0; i < R; i++)
            if (rc[i] != 0)
                return false;
        for (int i = 0; i < C; i++)
            if (cc[i] != 0)
                return false;

        boolean[][] check = new boolean[R][C];
        if (run(check) + set(check) != melds)
            return false;
        
        int dw = 0;
        for (int i = 0; i < R; i++)
            for (int j = 0; j < C; j++)
                if (a[i][j] == 1 && !check[i][j])
                    dw += point(j);
        if (dw != deadwood)
            return false;

        // drc_clone = drc.clone();
        // dcc_clone = dcc.clone();
        // for (int i = 0; i < R; i++)
        //     if (drc_clone[i] != 0)
        //         return false;
        // for (int i = 0; i < C; i++)
        //     if (dcc_clone[i] != 0)
        //         return false;

        return true;
    }

    static void solve(int r, int c, int remaining) throws IOException {
        if (r == R) 
        {
            if (remaining == 0 && check())
                print();
            return;
        }

        for (int i = 0; i <= 1; i++) 
        {
            if (i == 1) 
            {
                if (rc[r] > 0 && cc[c] > 0) {
                    a[r][c] = 1;
                    rc[r]--;
                    cc[c]--;
                    drc[r] -= point(c);
                    dcc[c] -= point(c);
                    if (c + 1 == C)
                        solve(r + 1, 0, remaining - 1);
                    else
                        solve(r, c + 1, remaining - 1);
                    rc[r]++;
                    cc[c]++;
                    a[r][c] = 0;
                    drc[r] += point(c);
                    dcc[c] += point(c);
                }
            } else 
            {
                if (c + 1 == C)
                    solve(r + 1, 0, remaining);
                else
                    solve(r, c + 1, remaining);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(new File("/Users/hoangpham/Documents/Java/GinRummyTests/input.txt"));
        for (int i = 0; i < R; i++)
            rc[i] = sc.nextInt();

        for (int i = 0; i < C; i++)
            cc[i] = sc.nextInt();

        for (int i = 0; i < R; i++)
            drc[i] = sc.nextInt();

        for (int i = 0; i < C; i++)
            dcc[i] = sc.nextInt();

        melds = sc.nextInt();

        deadwood = sc.nextInt();
        fw = new FileWriter("/Users/hoangpham/Documents/Java/GinRummyTests/output.txt");
        solve(0, 0, 10);

        // for (int i = 0; i < R; i++)
        //     for (int j = 0; j < C; j++)
        //         a[i][j] = sc.nextInt();
        // boolean[][] check = new boolean[R][C];
        // check = run(check);
        // check = set(check);
        // int dw = 0;
        // for (int i = 0; i < R; i++)
        //     for (int j = 0; j < C; j++)
        //         if (a[i][j] == 1 && !check[i][j])
        //             dw += (j < 10 ? (j + 1) : 10);
        // for (int i = 0; i < R; i++)
        // {
        //     for (int j = 0; j < C; j++)
        //         System.out.print(check[i][j] + " ");
        //     System.out.println();
        // }
        // System.out.println(dw);
        sc.close();
        fw.close();

    }
}