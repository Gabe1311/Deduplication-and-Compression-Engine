package brega.gabriele.deduplicationengine.model;

public class Operation {
    private Long amount;
    private Long position;
    public Operation(Long amount, Long position) {
        this.amount = amount;
        this.position = position;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "amount=" + amount +
                ", position=" + position +
                '}';
    }
}
