package sgab.sgab.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "multa")
@Getter
@Setter
@NoArgsConstructor
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "multa_id")
    private Integer multaId;

    @ManyToOne
    @JoinColumn(name = "emprestimo_id", nullable = false)
    private Emprestimo emprestimo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao = LocalDateTime.now();

    @Column(name = "status_pagamento", nullable = false)
    private Boolean statusPagamento = false;
}