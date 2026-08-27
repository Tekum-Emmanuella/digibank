package com.m2ibank.transfer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferTest {

    @Test
    void shouldCreateTransferWithConstructorValues() {
        Transfer transfer = new Transfer(
                1L,
                2L,
                new BigDecimal("5000.00"),
                "Salary transfer"
        );

        assertEquals(1L, transfer.getSourceAccountId());
        assertEquals(2L, transfer.getDestinationAccountId());
        assertEquals(new BigDecimal("5000.00"), transfer.getAmount());
        assertEquals("Salary transfer", transfer.getDescription());
    }

    @Test
    void shouldSetCreatedAtInParameterizedConstructor() {
        Transfer transfer = new Transfer(
                1L,
                2L,
                new BigDecimal("5000.00"),
                "Salary transfer"
        );

        assertNotNull(transfer.getCreatedAt());
    }

    @Test
    void shouldSetCreatedAtInNoArgConstructor() {
        Transfer transfer = new Transfer();

        assertNotNull(transfer.getCreatedAt());
    }

    @Test
    void shouldAllowNullDescription() {
        Transfer transfer = new Transfer(
                1L,
                2L,
                new BigDecimal("5000.00"),
                null
        );

        assertEquals(null, transfer.getDescription());
    }

    @Test
    void shouldMapTransferEntityToTransfersTable() throws Exception {
        Table table = Transfer.class.getAnnotation(Table.class);
        Field amountField = Transfer.class.getDeclaredField("amount");
        Field sourceAccountIdField = Transfer.class.getDeclaredField("sourceAccountId");
        Field destinationAccountIdField = Transfer.class.getDeclaredField("destinationAccountId");
        Field descriptionField = Transfer.class.getDeclaredField("description");
        Field createdAtField = Transfer.class.getDeclaredField("createdAt");

        assertNotNull(table);
        assertEquals("transfers", table.name());

        Column amountColumn = amountField.getAnnotation(Column.class);
        assertEquals(19, amountColumn.precision());
        assertEquals(2, amountColumn.scale());
        assertTrue(!amountColumn.nullable());

        Column sourceColumn = sourceAccountIdField.getAnnotation(Column.class);
        assertTrue(!sourceColumn.nullable());

        Column destinationColumn = destinationAccountIdField.getAnnotation(Column.class);
        assertTrue(!destinationColumn.nullable());

        Column descriptionColumn = descriptionField.getAnnotation(Column.class);
        assertEquals(255, descriptionColumn.length());
        assertTrue(descriptionColumn.nullable());

        Column createdAtColumn = createdAtField.getAnnotation(Column.class);
        assertTrue(!createdAtColumn.nullable());
    }
}
