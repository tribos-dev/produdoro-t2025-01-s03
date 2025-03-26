package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.NovaPosicaoRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

    private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    private static final String POSICAO_INVALIDA_MENSAGEM =
            "Posição da tarefa deve ser menor que a quantidade total de tarefas (%d)";
    private static final String POSICAO_IGUAL_MENSAGEM =
            "A nova posição não pode ser igual à posição atual (%d)";

    @Override
    public Tarefa salva(Tarefa tarefa) {
        log.info("[inicia] TarefaInfraRepository - salva");
        try {
            tarefaSpringMongoDBRepository.save(tarefa);
        } catch (DataIntegrityViolationException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
        }
        log.info("[finaliza] TarefaInfraRepository - salva");
        return tarefa;
    }

    @Override
    public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
        Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
        return tarefaPorId;
    }

    @Override
    public List<Tarefa> buscaPorIdUsuario(UUID idUsuario) {
        return tarefaSpringMongoDBRepository.findByIdUsuario(idUsuario);
    }

    @Override
    public void novaPosicaoTarefa(Tarefa tarefa, List<Tarefa> todasTarefas, NovaPosicaoRequest novaPosicao) {
        log.info("[start] TarefaInfraRepository - novaPosicaoTarefa");

        validaNovaPosicao(tarefa, todasTarefas, novaPosicao);

        final int posicaoAtual = tarefa.getPosicao();
        final int novaPosicaoValue = novaPosicao.getNovaPosicao();

        if (novaPosicaoValue != posicaoAtual) {
            atualizaTarefasEntrePosicoes(todasTarefas, posicaoAtual, novaPosicaoValue);
            atualizaPosicaoTarefa(tarefa, novaPosicaoValue);
        }

        log.info("[finish] TarefaInfraRepository - novaPosicaoTarefa");
    }

    private void atualizaTarefasEntrePosicoes(List<Tarefa> tarefas, int posicaoOrigem, int posicaoDestino) {
        log.info("[start] TarefaInfraRepository - atualizaTarefasEntrePosicoes");
        final int incremento = posicaoDestino > posicaoOrigem ? -1 : 1;
        final int inicio = posicaoDestino > posicaoOrigem ? posicaoOrigem + 1 : posicaoDestino;
        final int fim = posicaoDestino > posicaoOrigem ? posicaoDestino : posicaoOrigem - 1;

        IntStream.rangeClosed(inicio, fim)
                .forEach(i -> atualizaPosicaoTarefa(tarefas.get(i), i + incremento));
        log.info("[finish] TarefaInfraRepository - atualizaTarefasEntrePosicoes");
    }

    private void atualizaPosicaoTarefa(Tarefa tarefa, int novaPosicao) {
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("idTarefa").is(tarefa.getIdTarefa())),
                new Update().set("posicao", novaPosicao),
                Tarefa.class
        );
    }

    private void validaNovaPosicao(Tarefa tarefa, List<Tarefa> todasTarefas, NovaPosicaoRequest novaPosicao) {
        log.info("[start] TarefaInfraRepository - validaNovaPosicao");
        int posicaoAtual = tarefa.getPosicao();
        int novaPosicaoValue = novaPosicao.getNovaPosicao();
        int totalTarefas = todasTarefas.size();

        validarLimitesPosicao(novaPosicaoValue, totalTarefas, posicaoAtual);
        log.info("[finish] TarefaInfraRepository - validaNovaPosicao");
    }

    private void validarLimitesPosicao(int novaPosicao, int totalTarefas, int posicaoAtual) {
        log.info("[start] TarefaInfraRepository - validarLimitesPosicao");
        if (novaPosicao >= totalTarefas) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    String.format(POSICAO_INVALIDA_MENSAGEM, totalTarefas));
        }
        if (novaPosicao == posicaoAtual) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    String.format(POSICAO_IGUAL_MENSAGEM, posicaoAtual));
        }
        log.info("[finish] TarefaInfraRepository - validarLimitesPosicao");
    }
}
