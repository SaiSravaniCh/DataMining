

import java.util.ArrayList;
import java.util.List;

public class DataDiscretizer {
    public static double CalcEntropy (SingleAttr[] attrs, int start, int end) {
        double cls1 = 0, cls2 = 0;
        int i;
        int count = end - start + 1;
        for (i = start ; i <= end ; i ++)
        {
            if (attrs[i].c == 1)
                cls1 += 1;
            else
                cls2 += 1;
        }
        cls1 /= count;
        cls2 /= count;

        return - cls1*log2(cls1) - cls2*log2(cls2);
    }

    public static double log2 (double vl) {
        if (vl == 0)
            return 0;
        return Math.log(vl) / Math.log(2);
    }

    public static int Divide (SingleAttr[] attrs, int start, int end) {
        int mind = -1;
        double mine = 1;
        int i, count = end-start+1;
        for (i = start ; i < end ; i ++) {
            if (attrs[i].v == attrs[i+1].v)
                continue;
            double e1 = CalcEntropy(attrs, start, i);
            double e2 = CalcEntropy(attrs, i+1, end);
            double e = e1 * (i-start+1)/count + e2 * (end-i)/count;
            if (e < mine) {
                mine = e;
                mind = i;
            }
        }
        return mind;
    }

    public static double TotalEntropy (SingleAttr[] attrs, Integer[] divides) {
        double e = 0;
        int i;
        for (i = 0; i < divides.length - 1 ; i ++) {
            e = e + CalcEntropy(attrs, divides[i], divides[i+1]-1) * (divides[i+1] - divides[i])/ attrs.length;
        }
        return e;
    }

    public static Double[] BinWithEntropy (SingleAttr[] attrs) {
        int i;
        int dc = (int)(Math.random()*2 + 5);
        List<Integer> dv = new ArrayList<>();
        dv.add(0);
        dv.add(attrs.length);
        while ((TotalEntropy(attrs, dv.toArray(new Integer[0])) > 0.3 || dv.size() <= 2) && dv.size() < dc) {
            int maxi = 0;
            double maxe = 0;
            for (i = 0 ; i < dv.size() - 1 ; i ++) {
                double e = CalcEntropy(attrs, dv.get(i), dv.get(i+1)-1);
                if (e > maxe) {
                    maxe = e;
                    maxi = i;
                }
            }
            int div = Divide(attrs, dv.get(maxi), dv.get(maxi+1)-1);
            if (div == -1) {
                // div = (dv.get(maxi) + dv.get(maxi+1)) / 2;
                break;
            }
            dv.add(maxi+1, div+1);
        }

        List<Double> dvs = new ArrayList<>();
        for (i = 1 ; i < dv.size() ; i ++)
            dvs.add(attrs[dv.get(i) - 1].v);
        return dvs.toArray(new Double[0]);
    }
}
