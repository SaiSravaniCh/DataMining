public class Pattern {
    public int c;
    public double[] a;
    public int[] x;

    public double GR;
    public double suppC;
    public double suppNC;
    public double suppD;

    public int mdsC;
    public int mdsNC;

    public Pattern(int attrCount) {
        a = new double[attrCount];
        x = new int[attrCount];
    }

    public boolean isMatch (Pattern other) {
        int i;
        int diff = 0;
        for (i = 0 ; i < x.length-1 ; i ++) {
            diff += Math.abs(x[i] - other.x[i]);
        }
        return diff < 6;
    }
}
