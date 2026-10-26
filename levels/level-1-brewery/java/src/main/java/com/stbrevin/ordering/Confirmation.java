package com.stbrevin.ordering;

import java.time.LocalDate;
import java.util.*;

public class Confirmation {

    String number;
    String terms;
    String note;
    int status;
    LocalDate deliveryDate;
    List lines = new ArrayList();
    double goods, discount, deposit, shipping, tax, total;
    double empties;

    public String getNumber() { return number; }
    public int getStatus() { return status; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public List<String> getLines() { return lines; }
    public double getTotal() { return total; }

    public String getText() {
        String s = "Order " + (number == null ? "-" : number) + "  issued " + LocalDate.now() + "\nDelivery " + deliveryDate + (note != null ? " (" + note + ")" : "") + "\n";
        for (Object line : lines) s += line + "\n";
        s += "Goods " + OrderDesk.fmt(goods) + " Discount " + OrderDesk.fmt(discount) + " Deposit " + OrderDesk.fmt(deposit) + "\n";
        if (empties > 0) s += "Empties returned " + OrderDesk.fmt(empties) + "\n";
        s += "Shipping " + OrderDesk.fmt(shipping) + " Tax " + OrderDesk.fmt(tax) + " TOTAL " + OrderDesk.fmt(total) + "\n";
        if (number != null) s += "Payment " + terms + "\n";
        return s;
    }
}
