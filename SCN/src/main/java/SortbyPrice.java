import java.util.Comparator;

public class SortbyPrice implements Comparator<Pedido> {
    // Used for sorting in descending order of
    // total price
    public int compare(Pedido a, Pedido b)
    {
        return b.getTotalPrice() - a.getTotalPrice();
    }
}