import java.util.Comparator;

public class SortbyDate implements Comparator<Pedido> {
    // Used for sorting in ascending order of
    // estimated delivery date
    public int compare(Pedido a, Pedido b)
    {
        return b.getDataLimite().compareTo(a.getDataLimite());
    }
}