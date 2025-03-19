package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    @DisplayName("Deve mudar o status para pausa curta com sucesso")
    @Test
    void deveMudaStatusParaPausaCurtaComSucesso() {

        UUID idUsuario = UUID.fromString("a713162f-20a9-4db9-a85b-90cd51ab18f4");
        String email = "email@email.com";

        Usuario usuarioMock = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuarioMock);
        when(usuarioRepository.buscaUsuarioPorId(idUsuario)).thenReturn(usuarioMock);

        usuarioApplicationService.mudaStatusParaPausaCurta(email, idUsuario);

        assertEquals(StatusUsuario.PAUSA_CURTA, usuarioMock.getStatus());

        verify(usuarioRepository, times(1)).salva(usuarioMock);
    }

    @Test
    @DisplayName("Deve falhar ao mudar status para Pausa Curta se o ID do usuário não corresponder")
    void mudaStatusParaPausaCurta_Falha_UsuarioNaoEncontradoPorId() {

        UUID idUsuarioInvalido = UUID.randomUUID();
        String email = "email@email.com";

        Usuario usuarioMock = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(email)).thenReturn(usuarioMock);

        Exception exception = assertThrows(APIException.class, () -> {
            usuarioApplicationService.mudaStatusParaPausaCurta(email, idUsuarioInvalido);
        });

        assertEquals("O ID do usuário não corresponde às credenciais fornecidas.", exception.getMessage());
    }
}