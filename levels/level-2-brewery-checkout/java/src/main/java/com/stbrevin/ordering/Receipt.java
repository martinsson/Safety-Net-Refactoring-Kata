package com.stbrevin.ordering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Receipt {

    String number;
    String status;
    String message;
    String note;
    LocalDate deliveryDate;
    List lines = new ArrayList();
    double goods, discount, deposit, shipping, tax, total;

    public String getNumber() { return number; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public List<String> getLines() { return lines; }
    public double getTotal() { return total; }

    public String getText() {
        String s = "Order " + (number == null ? "-" : number) + "  issued " + LocalDate.now() + "\n";
        s += "Delivery " + deliveryDate + (note != null ? " (" + note + ")" : "") + "\n";
        for (Object line : lines) s += line + "\n";
        s += "Goods " + Checkout.fmt(goods) + " Discount " + Checkout.fmt(discount) + " Deposit " + Checkout.fmt(deposit) + "\n";
        s += "Shipping " + Checkout.fmt(shipping) + " Tax " + Checkout.fmt(tax) + " TOTAL " + Checkout.fmt(total) + "\n";
        s += status + (message != null ? " (" + message + ")" : "") + "\n";
        return s;
    }
}
