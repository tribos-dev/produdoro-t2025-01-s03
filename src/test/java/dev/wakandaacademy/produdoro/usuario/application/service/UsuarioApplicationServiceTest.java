package dev.wakandaacademy.produdoro.usuario.application.service;
import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import dev.wakandaacademy.produdoro.handler.APIException;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UsuarioApplicationServiceTest {

    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    private Usuario usuarioMock;
    private final String usuarioEmail = "admin@admin.com";
    private final UUID idUsuario = UUID.randomUUID();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        usuarioMock = mock(Usuario.class);
        when(usuarioRepository.buscaUsuarioPorEmail(usuarioEmail)).thenReturn(usuarioMock);
        when(usuarioRepository.buscaUsuarioPorId(idUsuario)).thenReturn(usuarioMock);
    }

    @Test
    void DeveAlterarStatusParaFoco() {
        doNothing().when(usuarioMock).validaUsuarioPorId(idUsuario);
        usuarioApplicationService.mudaStatusParaFoco(usuarioEmail, idUsuario);
        verify(usuarioRepository).buscaUsuarioPorEmail(usuarioEmail);
        verify(usuarioMock).alteraStatusParaFoco(idUsuario);
        verify(usuarioRepository).salva(usuarioMock);
    }

    @Test
    void alteraStatusParaFocoEUsuarioJaEstaEmFoco() {
        Usuario usuarioFoco = DataHelper.createUsuarioFoco();
        when(usuarioRepository.buscaUsuarioPorEmail(usuarioFoco.getEmail())).thenReturn(usuarioFoco);
        when(usuarioRepository.buscaUsuarioPorId(usuarioFoco.getIdUsuario())).thenReturn(usuarioFoco);
        APIException exception = assertThrows(APIException.class,()-> usuarioApplicationService.mudaStatusParaFoco(usuarioFoco.getEmail(),usuarioFoco.getIdUsuario()));
        assertEquals("Usuário ja esta em FOCO!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuarioFoco.getEmail());
    }
}