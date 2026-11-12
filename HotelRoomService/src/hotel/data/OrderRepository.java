package hotel.data;

import hotel.model.Order;
import hotel.model.OrderLine;
import hotel.model.OrderStatus;

import java.io.File;

/** OOP: INHERITANCE - stores Orders by extending FileRepository. */
public class OrderRepository extends FileRepository<Order> {

    public OrderRepository(File file, StorageLock lock) {
        super(file, lock);
        if (!fileExists()) seed();
    }

    @Override protected Order fromFields(String[] f) {
        Order o = new Order(Integer.parseInt(f[0]), f[1], f[5], OrderStatus.parse(f[2]), Long.parseLong(f[3]), Long.parseLong(f[4]));
        if (!f[6].isEmpty())
            for (String s : f[6].split(";")) {
                String[] q = s.split(",", -1);
                o.addLine(new OrderLine(dec(q[0]), Double.parseDouble(q[1]), Integer.parseInt(q[2])));
            }
        return o;
    }

    @Override protected String[] toFields(Order o) {
        StringBuilder lines = new StringBuilder();
        for (OrderLine l : o.getLines()) {
            if (lines.length() > 0) lines.append(';');
            lines.append(enc(l.getName())).append(',').append(l.getUnitPrice()).append(',').append(l.getQuantity());
        }
        return new String[]{String.valueOf(o.getId()), o.getRoom(), o.getStatus().name(), String.valueOf(o.getCreatedAt()),
            String.valueOf(o.getUpdatedAt()), o.getNotes(), lines.toString()};
    }

    private void seed() {
        long now = System.currentTimeMillis();
        Order o = new Order(0, "412", "", OrderStatus.ON_THE_WAY, now - 26 * 60000L, now - 4 * 60000L);
        o.addLine(new OrderLine("Club Sandwich", 220, 1));
        o.addLine(new OrderLine("Orange Juice", 90, 1));
        add(o);
    }
}
