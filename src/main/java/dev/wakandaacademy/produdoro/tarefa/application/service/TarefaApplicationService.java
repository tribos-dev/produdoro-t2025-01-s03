package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        int novaPosicao = tarefaRepository.contarTarefas(tarefaRequest.getIdUsuario());
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest, novaPosicao));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa =
                tarefaRepository.buscaTarefaPorId(idTarefa).orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void deletaTarefasConcluidas(String email, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTarefasConcluidas");
        validaUsuario(email, idUsuario);
        List<Tarefa> tarefasConcluidas = tarefaRepository.buscaTarefasConcluidas(idUsuario);
        if (tarefasConcluidas.isEmpty()){
            throw APIException.build(HttpStatus.NOT_FOUND, "Usuário nâo possue nenhuma tarefa concluída");
        }
        tarefaRepository.deletaTarefasConcluidas(tarefasConcluidas);
        log.info("[finaliza] TarefaApplicationService - deletaTarefasConcluidas");

    }

    private void validaUsuario(String email, UUID idUsuario) {
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(email);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.pertenceAoUsuario(idUsuario);

    }

    @Override
    public void incrementaPomodoro(String emailUsuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - incrementaPomodoro");
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
        Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
        tarefa.incrementaPomodoro(tarefa, usuario);
        usuarioRepository.salva(usuario);
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - incrementaPomodoro");
    }

    @Override
    public void alteraPosicaoTarefa(String usuario, UUID idTarefa, NovaPosicaoRequest novaPosicao) {
        log.info("[start] TarefaApplicationService - alteraPosicaoTarefa");
        Tarefa tarefa = detalhaTarefa(usuario, idTarefa);
        List<Tarefa> todasTarefas = tarefaRepository.buscaTarefasDoUsuario(tarefa.getIdUsuario());
        tarefaRepository.novaPosicaoTarefa(tarefa, todasTarefas, novaPosicao);
        log.info("[finish] TarefaApplicationService - alteraPosicaoTarefa");
    }

    @Override
    public List<TarefaListResponse> buscaTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - buscaTodasTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - buscaTodasTarefas");
        return TarefaListResponse.converte(tarefas);
    }

    @Override
    public void concluiTarefa(String emailUsuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - concluiTarefa");
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(emailUsuario);
        Tarefa tarefa = detalhaTarefa(emailUsuario, idTarefa);
        tarefa.mudaStatusParaConcluido(usuario);
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - concluiTarefa");
    }

    @Override
    public void deletaTodasTarefas(String email, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - deletaTodasTarefas");
        Usuario usuarioEmail = usuarioRepository.buscaUsuarioPorEmail(email);
        log.info("[Email] {}", usuarioEmail);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(usuarioEmail.getIdUsuario());
        verificaSeListaEstaVazia(tarefas);
        verificaQuantidadeTarefas(tarefas);
        tarefaRepository.deletaTodasTarefasUsuario(tarefas);
        log.info("[finaliza] TarefaApplicationService - deletaTodasTarefas");
    }

    private void verificaQuantidadeTarefas(List<Tarefa> tarefas) {
        if (tarefas.size() < 2) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário não possui quantidade " +
                    "minima de tarefa(as) cadastrada(as)");
        }
    }


    private void verificaSeListaEstaVazia(List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) {
            throw APIException.build(HttpStatus.CONFLICT, "Usuário não possui tarefa(as) cadastrada(as)");
        }
    }

}
