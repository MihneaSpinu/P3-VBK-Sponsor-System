import java.time.LocalDate;

public class Contract {
    private LocalDate startDate;
    private LocalDate endDate;
    private int payment;
    private boolean status;
   private Type type;
   
    // Constructor
    public Contract(LocalDate startDate, LocalDate endDate, int payment, boolean status, String typeName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.payment = payment;
        this.status = status;
          this.type = new Type(typeName);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getPayment() {
        return payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
        public Type getType() {
        return type;
    }
    public void setType(Type type)
    {
        this.type = type;
    }
}
