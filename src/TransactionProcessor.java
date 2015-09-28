import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Leo on 9/20/15.
 */
public class TransactionProcessor {

    private List<Transaction> transactionList;
    private Map<String, List<Transaction>> subMap;
    private Map<String, SubInfo> subInfos;
    private static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    private static TransactionComparator comparator = new TransactionComparator();

    private int growthYear = -1;
    private int lossYear = -1;

    public TransactionProcessor()
    {
        subMap = new HashMap<String, List<Transaction>>();
        subInfos = new HashMap<String, SubInfo>();
        transactionList = new LinkedList<Transaction>();
    }

    public void load(String fileName) throws IOException {
        BufferedReader br = null;

        try {
            FileInputStream fis = new FileInputStream(fileName);
            br = new BufferedReader(new InputStreamReader(fis));

            String line = null;

            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");

                if (fields != null && fields.length == 4) {
                    String id = fields[0];
                    String subID = fields[1];
                    String amtStr = fields[2];
                    String subDateStr = fields[3];

                    long amt = -1;
                    Date subDate;

                    try {
                        amt = Long.valueOf(amtStr);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid amount format for ID = " + id);
                        continue;
                    }

                    try {
                        subDate = formatter.parse(subDateStr);
                    } catch (ParseException e) {
                        System.err.println("Invalid amount format for ID = " + id);
                        continue;
                    }

                    Transaction trans = new Transaction(subDate, amt);

                    List<Transaction> transList = null;

                    if (subMap.containsKey(subID)) {
                        transList = subMap.get(subID);
                    } else {
                        transList = new LinkedList<Transaction>();
                        subMap.put(subID, transList);
                    }

                    transList.add(trans);
                    transactionList.add(trans);
                }
            }
        }
        finally {
            if (br != null)
                br.close();
        }
    }

    public void parseSubscriptionType()
    {
        for(String subID : subMap.keySet())
        {
            List<Transaction> transList = subMap.get(subID);
            Collections.sort(transList, comparator);

            if (transList.size() == 1)
            {
                SubInfo subInfo = new SubInfo();

                subInfo.setSubType(SubType.One_Off);
                subInfo.setDuration(1);
                subInfo.setDurationType(DurationType.Day);
                subInfo.setStartDate(transList.get(0).getSubDate());
                subInfo.setEndDate(transList.get(0).getSubDate());

                subInfos.put(subID, subInfo);
                continue;
            }

            SubType subType;
            Transaction firstTrans = transList.get(0);
            Date firstDate = firstTrans.getSubDate();
            Calendar prevCal = Calendar.getInstance();
            prevCal.setTime(firstDate);
            int firstDay = prevCal.get(Calendar.DAY_OF_MONTH);
            int firstMonth = prevCal.get(Calendar.MONTH);
            //int firstYear = prevCal.get(Calendar.YEAR);

            Transaction secondTran = transList.get(1);
            Date secondDate = secondTran.getSubDate();
            Calendar secondCal = Calendar.getInstance();
            secondCal.setTime(secondDate);
            int secondDay = secondCal.get(Calendar.DAY_OF_MONTH);
            int secondMonth = secondCal.get(Calendar.MONTH);
            //int secondYear = secondCal.get(Calendar.YEAR);

            Transaction lastTran = transList.get(transList.size()-1);
            Date lastDate = lastTran.getSubDate();

            if (firstDay != secondDay)
            {
                subType = SubType.Daily;
            }
            else if (firstMonth != secondMonth)
            {
                subType = SubType.Monthly;
            }
            else
            {
                subType = SubType.Yearly;
            }

            //Data Exploration: check if a user always keeps same subscription type
            /*List<Transaction> tailoredList = trans.subList(1,trans.size()-1);
            for(Transaction tran : tailoredList)
            {
                Date currDate = tran.getSubDate();
                Calendar currCal = Calendar.getInstance();
                currCal.setTime(currDate);
                int currDay = currCal.get(Calendar.DAY_OF_MONTH);
                int currMonth = currCal.get(Calendar.MONTH);
                int currYear = currCal.get(Calendar.YEAR);

                if (subType == SubType.Unknown)
                {
                    if (currDay != prevDay)
                    {
                        subType = SubType.Daily;
                    }
                    else if (currMonth != prevMonth)
                    {
                        subType = SubType.Monthly;
                    }
                    else
                    {
                        subType = SubType.Yearly;
                    }
                }
                else if (subType == SubType.Daily)
                {
                    prevCal.add(Calendar.DAY_OF_MONTH, 1);
                    if (!(prevCal.equals(currCal)))
                    {
                        prevCal.add(Calendar.DAY_OF_MONTH, -1);
                        System.err.println("[" + subID + "] Deduced Type: " + subType.toString() + ", prev date: " +
                        prevCal.toString() + " curr date: " + currCal.toString());
                    }
                }
                else if (subType == SubType.Monthly)
                {
                    prevCal.add(Calendar.MONTH, 1);
                    if (!(prevCal.equals(currCal)))
                    {
                        prevCal.add(Calendar.MONTH, -1);
                        System.err.println("[" + subID + "] Deduced Type: " + subType.toString() + ", prev date: " +
                                prevCal.toString() + " curr date: " + currCal.toString());
                    }
                }
                else
                {
                    prevCal.add(Calendar.YEAR, 1);
                    if (!(prevCal.equals(currCal)))
                    {
                        prevCal.add(Calendar.YEAR, -1);
                        System.err.println("[" + subID + "] Deduced Type: " + subType.toString() + ", prev date: " +
                                prevCal.toString() + " curr date: " + currCal.toString());
                    }
                }

                prevCal = currCal;
                prevDay = currDay;
                prevMonth = currMonth;
                prevYear = currYear;
            }*/

            SubInfo subInfo = new SubInfo();
            subInfo.setDuration(transList.size() - 1);
            subInfo.setSubType(subType);
            if (subType == SubType.Daily)
                subInfo.setDurationType(DurationType.Day);
            else if (subType == SubType.Monthly)
                subInfo.setDurationType(DurationType.Month);
            else
                subInfo.setDurationType(DurationType.Year);

            subInfo.setStartDate(firstDate);
            subInfo.setEndDate(lastDate);

            subInfos.put(subID, subInfo);
        }
    }

    public void outputSummary() throws IOException {
        BufferedWriter bw = null;

        try {
            File file = new File("subscriptionSummary.txt");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            bw = new BufferedWriter(fw);

            bw.write("================================List of subscription================================");
            bw.newLine();

            bw.write("Subscription ID,Subscription Type,Duration,Start Date,End Date");
            bw.newLine();
            for(String subID : subInfos.keySet()) {
                SubInfo subInfo = subInfos.get(subID);

                bw.write(subID + "," + subInfo.getSubType().toString() + "," + String.valueOf(subInfo.getDuration()) +
                    " " + subInfo.getDurationType().toString() + "," + formatter.format(subInfo.getStartDate()) + "," +
                    formatter.format(subInfo.getEndDate()));
                bw.newLine();
            }
            bw.newLine();
            bw.write("================================Bonus Question 1================================");
            bw.newLine();
            bw.write("The year of highest revenue growth: " + String.valueOf(growthYear));
            bw.newLine();
            bw.write("The year of highest revenue loss: " + String.valueOf(lossYear));
            bw.newLine();

        } finally {
            if (bw != null)
                bw.close();
        }
    }

    public void computerHighestRevenueLossGrowth() {
        long growth = 0;
        growthYear = 0;
        long loss = 0;
        lossYear = 0;

        Collections.sort(transactionList, comparator);

        Transaction prevTrans = null;
        long prevRevenue = -1;
        long currRevenue = 0;
        Calendar prevCal = Calendar.getInstance();
        Calendar currCal = Calendar.getInstance();

        for(Transaction currTrans : transactionList)
        {
            if (prevTrans == null)
            {
                prevTrans = currTrans;
                prevCal.setTime(currTrans.getSubDate());
            }
            else
            {
                currCal.setTime(currTrans.getSubDate());
                if (currCal.get(Calendar.YEAR) > prevCal.get(Calendar.YEAR))
                {
                    if (prevRevenue > 0)
                    {
                        long growthOrLoss = currRevenue - prevRevenue;
                        if (growthOrLoss > growth)
                        {
                            growth = growthOrLoss;
                            growthYear = prevCal.get(Calendar.YEAR);
                        }
                        else if (growthOrLoss < loss)
                        {
                            loss = growthOrLoss;
                            lossYear = prevCal.get(Calendar.YEAR);
                        }
                    }
                    prevRevenue = currRevenue;
                    currRevenue = 0;
                }
                prevCal.setTime(currTrans.getSubDate());
            }

            currRevenue += currTrans.getAmount();
        }
    }
}
