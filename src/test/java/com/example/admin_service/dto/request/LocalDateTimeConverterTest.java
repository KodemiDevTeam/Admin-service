package com.example.admin_service.dto.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeConverterTest {

    private LocalDateTimeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LocalDateTimeConverter();
    }


    @Test
    void testConvertValidLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45);
        String result = converter.convert(dateTime);

        final String expectedResult = "2024-03-15T14:30:45";
        assertEquals(expectedResult, result);
    }



    @Test
    void testConvertNullValue() {
        String result = converter.convert(null);
        assertNull(result);
    }

    @Test
    void testUnconvertValidString() {
        String dateTimeString = "2024-03-15T14:30:45";
        LocalDateTime result = converter.unconvert(dateTimeString);
        
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30, 45), result);
    }

    @Test
    void testUnconvertNullValue() {
        LocalDateTime result = converter.unconvert(null);
        assertNull(result);
    }

    @Test
    void testRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2023, 12, 25, 10, 15, 30);
        String converted = converter.convert(original);
        LocalDateTime unconverted = converter.unconvert(converted);
        
        assertEquals(original, unconverted);
    }

    @Test
    void testConvertWithNanoseconds() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 14, 30, 45, 123456789);
        String result = converter.convert(dateTime);
        
        assertTrue(result.startsWith("2024-03-15T14:30:45"));
    }

    @Test
    void testConvertMidnight() {
        LocalDateTime midnight = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        String result = converter.convert(midnight);
        
        assertEquals("2024-01-01T00:00", result);
    }

    @Test
    void testConvertEndOfDay() {
        LocalDateTime endOfDay = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        String result = converter.convert(endOfDay);
        
        assertEquals("2024-12-31T23:59:59", result);
    }
}
