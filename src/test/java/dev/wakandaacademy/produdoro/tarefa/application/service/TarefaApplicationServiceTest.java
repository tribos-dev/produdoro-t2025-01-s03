package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaEditaRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;



    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }
    @Test
    @DisplayName("Deve incrementar pomodoro")
    void deveIncrementarPomodoro() {
        Usuario usuarioEmFoco = DataHelper.createUsuario1();
        Tarefa tarefa = DataHelper.createTarefa();
        int contagemPomodoroAntes = tarefa.getContagemPomodoro();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioEmFoco);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.incrementaPomodoro(usuarioEmFoco.getEmail(), tarefa.getIdTarefa());

        int contagemPomodoroDepois = tarefa.getContagemPomodoro();
        verify(tarefaRepository, times(1)).salva(any());
        assertEquals(contagemPomodoroAntes + 1, contagemPomodoroDepois);
    }

    @Test
    @DisplayName("Não deve incrementar pomodoro quando a tarefa não existe")
    void naoDeveIncrementarPomodoroQuandoTarefaNaoExiste() {
        Usuario usuarioEmFoco = DataHelper.createUsuario1();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioEmFoco);
        APIException e = assertThrows(APIException.class,
                () -> tarefaApplicationService.incrementaPomodoro(usuarioEmFoco.getEmail(), UUID.randomUUID()));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusException());
        assertEquals("Tarefa não encontrada!", e.getMessage());
    }

    @Test
    @DisplayName("Não deve incrementar pomodoro quando o usuário não é dono da tarefa")
    void naoDeveIncrementaPomodoroQuandoUsuarioNaoDonoDaTarefa() {
        Usuario usuarioNaoDonoDaTarefa = DataHelper.createUsuario2();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuarioNaoDonoDaTarefa);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        APIException e = assertThrows(APIException.class,
                () -> tarefaApplicationService.incrementaPomodoro(usuarioNaoDonoDaTarefa.getEmail(),
                        tarefa.getIdTarefa()));

        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusException());
        assertEquals("Usuário não é dono da tarefa solicitada!", e.getMessage());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    @Test
    void deveConcluirTarefa() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.salva(tarefa)).thenReturn(tarefa);
        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusTarefa.CONCLUIDA,tarefa.getStatus());
        verify(usuarioRepository, times(2)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
        verify(tarefaRepository, times(1)).salva(tarefa);
    }
    @Test
    void deveLancarExcecaoSeTarefaNaoEncontrada() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idInvalido = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        APIException exception = assertThrows(APIException.class,
                () ->tarefaApplicationService.concluiTarefa(usuario.getEmail(), idInvalido));

        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusException());
        verify(usuarioRepository, times(2)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(idInvalido);
        verify(tarefaRepository, never()).salva(any(Tarefa.class));
    }
}
