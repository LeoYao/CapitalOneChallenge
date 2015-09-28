import java.util.Comparator;

/**
 * Created by Leo on 9/20/15.
 */
public class TransactionComparator implements Comparator<Transaction>
{
    @Override
    public int compare(Transaction o1, Transaction o2) {
        if (o1.getSubDate().before(o2.getSubDate()))
        {
            return -1;
        }
        else if (o1.getSubDate().after(o2.getSubDate()))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
}
