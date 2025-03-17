package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
public class TarefaEditaRequest {

    @NotNull
    private UUID id;
    @NotEmpty(message = "O campo não pode estar vazio")
    private String descricao;

    public TarefaEditaRequest(UUID idTarefa, String novaDescriçãoDaTarefa) {
        this.id = idTarefa;
        this.descricao = novaDescriçãoDaTarefa;
    }
}
