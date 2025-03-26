package dev.wakandaacademy.produdoro.tarefa.application.repository;

import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaRepository {

    Tarefa salva(Tarefa tarefa);
    Optional<Tarefa> buscaTarefaPorId(UUID idTarefa);
    List<Tarefa> buscaPorIdUsuario(UUID idUsuario);
    void novaPosicaoTarefa(Tarefa tarefa, List<Tarefa> todasTarefas, NovaPosicaoRequest novaPosicao);
    List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario);
}
