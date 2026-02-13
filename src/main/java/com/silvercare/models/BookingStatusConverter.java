package com.silvercare.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public BookingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String normalized = dbData.trim().toUpperCase();
        try {
            return BookingStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // Map some legacy or alternate values
            switch (normalized) {
                case "COMPLETED":
                    return BookingStatus.COMPLETED;
                case "CONFIRMED":
                    return BookingStatus.CONFIRMED;
                case "CANCELLED":
                case "CANCELED":
                    return BookingStatus.CANCELLED;
                case "PAID":
                case "PAYED":
                    return BookingStatus.PAID;
                case "PENDING":
                    return BookingStatus.PENDING;
                default:
                    // fallback: treat unknown as PENDING
                    return BookingStatus.PENDING;
            }
        }
    }
}
