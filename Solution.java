public class Solution {
    public int[] ind;
    public double gso;
    public Solution (int k, int max) {
        ind = new int[k];

        int i, j;
        for (i = 0 ; i < k ; i ++) {
            int t = i;
            while (true) {
                t = (int)(Math.random()*max);
                for (j = 0; j < i ; j ++)
                    if (t == ind[j])
                        break;
                if (j == i)
                    break;
            }
            ind[i] = t;
        }
    }

    public Solution (Solution a, Solution b) {
        int k = a.ind.length;
        int i, j;

        ind = new int[k];
        for (i = 0 ; i < k ; i ++)
            ind[i] = a.ind[i];

        int dv = (int)(Math.random()*k);
        for (i = dv ; i < k ; i ++) {
            for (j = 0 ; j < i ; j ++)
                if (b.ind[i] == ind[j])
                    break;
            if (j == i)
                ind[i] = b.ind[i];
        }
    }
}
