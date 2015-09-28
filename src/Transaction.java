import java.util.Date;

/**
 * Created by Leo on 9/20/15.
 */
public class Transaction {

    public Transaction(Date subDate, long amount)
    {
        this.subDate = subDate;
        this.amount = amount;
    }
    private Date subDate;
    private long amount;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Date getSubDate() {
        return subDate;
    }

    public void setSubDate(Date subDate) {
        this.subDate = subDate;
    }
}
