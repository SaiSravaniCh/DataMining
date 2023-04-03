import javafx.util.Pair;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        DataMiner miner = new DataMiner();

        // prob 1

        /*
        String file = args[0];
        int c = Integer.parseInt(args[1]);
        int k = Integer.parseInt(args[2]);

        miner.readData(file);
        miner.binWithEntropy();
        Solution best = miner.findMax(c, k);
        miner.writeBinningItems("binningItemMap.csv");
        miner.writeSolution(best,"topPatterns.csv");
        System.out.println(best.gso);
        */

        // prob 2
        /*
        String file = args[0];
        int k = Integer.parseInt(args[1]);
        miner.readData(file);
        miner.binWithEntropy();
        Solution ncBest = miner.findMax(2, k);
        Solution cBest = miner.findMax(1, k);
        miner.predict(cBest, ncBest, true);
        */

        // prob 3
        String file = args[0];
        int k = Integer.parseInt(args[1]);
        miner.readData(file);
        miner.binWithEntropy();
        Predict pr = miner.findMaxPredict(k);
        System.out.println(miner.predict(pr.sol1, pr.sol2, true));
	// write your code here
    }

}
