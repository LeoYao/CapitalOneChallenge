import java.io.IOException;

/**
 * Created by Leo on 9/20/15.
 */
public class Main {

    public static void main(String[] args) {
        TransactionProcessor map = new TransactionProcessor();

        try {
            map.load("/Users/Leo/Downloads/subscription_report.csv");
        }
        catch(IOException e)
        {
            System.err.println("Failed to load input file");
            System.err.println(e.toString());
            return;
        }

        map.parseSubscriptionType();
        map.computerHighestRevenueLossGrowth();



        try {
            map.outputSummary();
        }
        catch(IOException e)
        {
            System.err.println("Failed to write summary file");
            System.err.println(e.toString());
            return;
        }




        System.out.println();
    }
}
