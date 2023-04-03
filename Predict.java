public class Predict {
    public Solution sol1;
    public Solution sol2;
    public double pr;

    public Predict(int kk, int count) {
        sol1 = new Solution(kk, count);
        sol2 = new Solution(kk, count);
    }

    public Predict(Predict a, Predict b) {
        sol1 = new Solution(a.sol1, b.sol1);
        sol2 = new Solution(a.sol2, b.sol2);
    }
}
