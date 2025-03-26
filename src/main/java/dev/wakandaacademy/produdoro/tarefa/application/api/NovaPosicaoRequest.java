package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Getter;

import javax.validation.constraints.PositiveOrZero;

@Getter
public class NovaPosicaoRequest {

    @PositiveOrZero(message = "Posição deve ser maior ou igual a zero.")
    private int novaPosicao;
}
