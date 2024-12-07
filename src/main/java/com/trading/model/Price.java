package com.trading.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prices")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal askPrice;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
