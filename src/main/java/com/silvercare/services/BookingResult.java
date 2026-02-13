package com.silvercare.services;

import java.util.ArrayList;
import java.util.List;

public class BookingResult {
    private List<Integer> createdBookingIds = new ArrayList<>();
    private List<Integer> succeededServiceIds = new ArrayList<>();

    public List<Integer> getCreatedBookingIds() { return createdBookingIds; }
    public void addCreatedBookingId(int id) { createdBookingIds.add(id); }

    public List<Integer> getSucceededServiceIds() { return succeededServiceIds; }
    public void addSucceededServiceId(int id) { succeededServiceIds.add(id); }
}
