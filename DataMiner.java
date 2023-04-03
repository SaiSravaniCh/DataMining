import java.awt.print.Paper;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataMiner {
    private int classCount = 2;
    private int attrCount;
    private List<Pattern> patterns;
    private List<Attribute> attributes;

    private List<Solution> sols;
    private List<Solution> nextSols;
    final int MAX_SOL = 100000;
    final int SOL_SIZE = 2000;
    final int MAX_STEP = 100;

    private double[][] ovlp;
    private int cc;
    private int kk;

    public void readData (String file) {
        int dataLineCount = 0;
        int i;
        String line;

        patterns = new ArrayList<>();
        attributes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((line = br.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue;
                }
                dataLineCount++;
                String[] lineParts = line.split(",");
                if (dataLineCount == 1) {
                    attrCount = lineParts.length;
                }

                Pattern p = new Pattern(attrCount);
                for (i = 0 ; i < attrCount ; i ++) {
                    p.a[i] = Double.parseDouble(lineParts[i]);
                }
                p.c = Integer.parseInt(lineParts[attrCount-1]);
                patterns.add(p);
            }
            for (i = 0 ; i < attrCount ; i ++)
                attributes.add(new Attribute(i));
        }
        // Trap these Exceptions
        catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void binWithEntropy () {
        int i, j;
        for (i = 0 ; i < attrCount ; i ++) {
            List<SingleAttr> attrs = new ArrayList<>();
            for (Pattern p : patterns) {
                attrs.add(new SingleAttr(p.a[i], p.c));
            }
            Collections.sort(attrs, (o1, o2) -> {
                if (o1.v == o2.v)
                    return 0;
                return o1.v < o2.v ? -1 : 1;
            });
            Double[] dv = DataDiscretizer.BinWithEntropy(attrs.toArray(new SingleAttr[0]));
            Attribute attr = attributes.get(i);
            attr.divideValues = dv;
            attr.count = dv.length;
            attr.leftBound = attrs.get(0).v;
            attr.rightBound = attrs.get(attrs.size()-1).v;
        }

        for (Pattern p : patterns) {
            for (i = 0 ; i < attrCount ; i ++)
            {
                Attribute attr = attributes.get(i);
                for (j = 0 ; j < attr.count ; j ++) {
                    if (p.a[i] <= attr.divideValues[j])
                        break;
                }
                p.x[i] = j + 1;
            }
        }
    }

    public double calcGSO (Solution sol) {
        double avGR = 0;
        double avgSupp  = 0;
        double avgOvlp  = 0;
        int i, j, avc = 0;
        for (i = 0 ; i < kk ; i ++) {
            avGR += patterns.get(sol.ind[i]).GR;
            avgSupp += patterns.get(sol.ind[i]).suppD;
            for (j = i + 1 ; j < kk ; j ++) {
                avgOvlp += ovlp[sol.ind[i]][sol.ind[j]];
                avc ++;
            }
        }
        avGR /= kk;
        avgSupp /= kk;
        avgOvlp /= avc;

        return avGR * avgSupp * (1 / (avgOvlp + 0.01));
    }

    public int getMaxGSO () {
        double maxGSO = 0;
        int di = 0;
        int i;
        for (i = 1; i < sols.size() ; i ++){
            double gso = calcGSO(sols.get(i));
            if (gso > maxGSO) {
                maxGSO = gso;
                di = i;
            }
        }
        return di;
    }

    public void doOneStep() {
        int maxi = getMaxGSO();
        nextSols = new ArrayList<>();
        nextSols.add(sols.get(maxi));
        int i;
        for (i = 0 ; i < SOL_SIZE*5-1 ; i ++) {
            int fi = (int)(Math.random()*sols.size());
            int mi = (int)(Math.random()*sols.size());
            nextSols.add(new Solution(sols.get(fi), sols.get(mi)));
        }
        for (i = 0 ; i < nextSols.size() ; i ++)
            nextSols.get(i).gso = calcGSO(nextSols.get(i));

        Collections.sort(nextSols, ((o1, o2) -> {
            if (o1.gso < o2.gso)
                return 1;
            if (o1.gso == o2.gso)
                return 0;
            return -1;
        }));
        sols = nextSols.subList(0, SOL_SIZE);
    }

    public void calcPatternValues () {
        int count = patterns.size();
        int cCount = 0;
        int ncCount = 0;
        for (Pattern p : patterns) {

            if (p.c == cc)
                cCount ++;
            else
                ncCount ++;
        }

        for (Pattern p : patterns) {
            int pCount = 0;
            int pcCount = 0;
            for (Pattern oth : patterns)
            {
                if (p.isMatch(oth))
                {
                    if (oth.c == cc)
                        pcCount ++;
                    pCount ++;
                }
            }

            p.suppC = pcCount * 1.0 / cCount;
            p.suppNC = (pCount-pcCount) * 1.0 / ncCount;
            p.suppD = pCount * 1.0 / count;
            p.mdsC = pcCount;
            p.mdsNC = pCount - pcCount;
            p.GR = (p.mdsC + 1) / (p.mdsNC + 1);
        }
        ovlp = new double[count][count];

        int i, j;
        for (i = 0 ; i < count ; i ++) {
            ovlp[i] = new double[count];
            for (j = i+1 ; j < count ; j ++)
                if (patterns.get(i).isMatch(patterns.get(j)))
                    ovlp[i][j] = patterns.get(i).suppD;
        }
    }

    public Solution findMax (int c, int k) {
        int i;
        int count = patterns.size();
        cc = c;
        kk = k;

        calcPatternValues();
        sols = new ArrayList<>();

        for (i = 0 ; i < MAX_SOL ; i ++)
            sols.add(new Solution(kk, count));

        for (i = 0 ; i < MAX_STEP ; i ++)
        {
            doOneStep();
        }

        int maxi = getMaxGSO();
        return sols.get(maxi);
    }

    public void writeBinningItems (String fname) {
        int i;
        try (BufferedWriter br = new BufferedWriter(new FileWriter(fname))) {
            br.write("Ai, lb, rb, j\n");
            for (i = 0 ; i < attrCount ; i ++)
            {
                Attribute attr = attributes.get(i);
                br.write(String.format("%d,%f,%f,%d\n", i+1, attr.leftBound, attr.rightBound, attr.count));
            }

        }
        catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void writeSolution (Solution sol, String fname) {
        int i, j;
        try (BufferedWriter br = new BufferedWriter(new FileWriter(fname))) {
            for (i = 0 ; i < kk ; i ++) {
                Pattern p = patterns.get(sol.ind[i]);
                br.write(String.format("Pattern,%d,%f,%f,%f,%f",sol.ind[i]+1, p.GR, p.suppC, p.suppNC, p.suppD));
                for (j = 0 ; j < attrCount ; j ++)
                    br.write(String.format(",%d", p.x[j]));
                br.write("\n");
            }

            for (i = 0 ; i < kk ; i ++)
            {
                for (j = i+1 ; j < kk ; j ++) {
                    br.write(String.format("Overlap,%d,%d,%f\n", i, j, ovlp[sol.ind[i]][sol.ind[j]]));
                }
            }
        }
        catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
        catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public double score (Solution sol, Pattern t, int cls) {
        int i;
        double sum = 0;
        for (i = 0 ; i < kk ; i ++) {
            Pattern oth = patterns.get(sol.ind[i]);
            if (oth.isMatch(t)) {
                if (cls == 1)
                    sum += oth.suppC * (oth.suppC + 1) / oth.suppD;
                else
                    sum += oth.suppNC * (oth.suppNC + 1) / oth.suppD;
            }
        }
        return  sum;
    }

    public double predict (Solution cBest, Solution ncBest, boolean print) {
        int right = 0;
        for (Pattern p : patterns) {
            int pc = score(cBest, p, 1) > score(ncBest, p, 2) ? 1 : 2;
            if (pc == p.c)
                right ++;
        }
        double accuracy = right*1.0 / patterns.size();
        if (print) {
            try (BufferedWriter br = new BufferedWriter(new FileWriter("P2bOut.csv"))) {
                br.write(String.format("%f\n", accuracy));
                int i = 1;
                for(Pattern p : patterns) {
                    int pc = score(cBest, p, 1) > score(ncBest, p, 2) ? 1 : 2;
                    br.write(String.format("%d,%d,%d\n", i, p.c, pc));
                    i ++;
                }
            }
            catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            }
            catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
        return accuracy;
    }

    private List<Predict> psols;
    private List<Predict> nextPSols;
    final int MAX_PSOL = 1000;
    final int PSOL_SIZE = 100;


    public double calcPredict (Predict pr) {
        return predict(pr.sol1, pr.sol2, false);
    }

    public int getMaxPredict () {
        double maxPr = 0;
        int di = 0;
        int i;
        for (i = 1; i < psols.size() ; i ++){
            double pr = calcPredict(psols.get(i));
            if (pr > maxPr) {
                maxPr = pr;
                di = i;
            }
        }
        return di;
    }

    public void doOnePredictStep() {
        int maxi = getMaxPredict();
        nextPSols = new ArrayList<>();
        nextPSols.add(psols.get(maxi));
        int i;
        for (i = 0 ; i < PSOL_SIZE*2-1 ; i ++) {
            int fi = (int)(Math.random()*psols.size());
            int mi = (int)(Math.random()*psols.size());
            nextPSols.add(new Predict(psols.get(fi), psols.get(mi)));
        }
        for (i = 0 ; i < nextPSols.size() ; i ++)
            nextPSols.get(i).pr = calcPredict(nextPSols.get(i));

        Collections.sort(nextPSols, ((o1, o2) -> {
            if (o1.pr < o2.pr)
                return 1;
            if (o1.pr == o2.pr)
                return 0;
            return -1;
        }));
        psols = nextPSols.subList(0, PSOL_SIZE);
    }

    public Predict findMaxPredict (int k) {
        this.kk = k;
        this.cc = 1;
        calcPatternValues();
        psols = new ArrayList<>();

        int i, count = patterns.size();
        for (i = 0 ; i < MAX_PSOL ; i ++)
            psols.add(new Predict(kk, count));

        for (i = 0 ; i < MAX_STEP ; i ++)
        {
            doOnePredictStep();
        }

        int maxi = getMaxPredict();
        return psols.get(maxi);
    }
}
