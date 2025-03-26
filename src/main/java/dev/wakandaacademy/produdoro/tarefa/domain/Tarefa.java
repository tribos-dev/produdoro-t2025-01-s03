package dev.wakandaacademy.produdoro.tarefa.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Document(collection = "Tarefa")
public class Tarefa {
	@Id
	private UUID idTarefa;
	@NotBlank
	private String descricao;
	@Indexed
	private UUID idUsuario;
	@Indexed
	private UUID idArea;
	@Indexed
	private UUID idProjeto;
	private StatusTarefa status;
	private StatusAtivacaoTarefa statusAtivacao;
	private int contagemPomodoro;
	private Integer posicao;

	public Tarefa(TarefaRequest tarefaRequest, int novaPosicao) {
		this.idTarefa = UUID.randomUUID();
		this.idUsuario = tarefaRequest.getIdUsuario();
		this.descricao = tarefaRequest.getDescricao();
		this.idArea = tarefaRequest.getIdArea();
		this.idProjeto = tarefaRequest.getIdProjeto();
		this.status = StatusTarefa.A_FAZER;
		this.statusAtivacao = StatusAtivacaoTarefa.INATIVA;
		this.contagemPomodoro = 1;
		this.posicao = novaPosicao;
	}

	public void pertenceAoUsuario(Usuario usuarioPorEmail) {
		if(!this.idUsuario.equals(usuarioPorEmail.getIdUsuario())) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário não é dono da tarefa solicitada!");
		}
	}

	public void incrementaPomodoro(Tarefa tarefa, Usuario usuario) {
		if (!Objects.equals(usuario.getStatus(), StatusUsuario.FOCO)) {
		}
		this.ativaTarefa();
		this.incrementaPomodoro();
		StatusUsuario novoStatus = this.alteraStatusPorCadaPomodoro(this.contagemPomodoro);
		usuario.alterarStatusParaFoco(novoStatus);
	}

	private void ativaTarefa() {
		this.statusAtivacao = StatusAtivacaoTarefa.ATIVA;
	}

	private void incrementaPomodoro() {
		this.contagemPomodoro++;
	}

	private StatusUsuario alteraStatusPorCadaPomodoro(int totalDePomodoros) {
		return (totalDePomodoros % 4 == 0) ? StatusUsuario.PAUSA_LONGA : StatusUsuario.PAUSA_CURTA;
	}

	public void alteraPosicaoTarefa(int novaPosicaoTarefa) {
		this.posicao = novaPosicaoTarefa;
	}
}
