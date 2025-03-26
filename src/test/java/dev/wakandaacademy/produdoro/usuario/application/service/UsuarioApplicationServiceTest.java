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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    private Usuario usuarioMock;
    private final String usuarioEmail = "admin@admin.com";
    private final UUID idUsuario = UUID.randomUUID();


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

    @Test
    void deveMudarParaPausaLonga() {
        Usuario usuario = DataHelper.createUsuario1();
        when(usuarioRepository.buscaUsuarioPorEmail(anyString())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusPausaLonga(usuario.getEmail(), usuario.getIdUsuario());
        assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        verify(usuarioRepository, times(1)).salva(usuario);
    }



    @Test
    void DeveAlterarStatusParaFoco() {
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        usuarioApplicationService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario());
        verify(usuarioRepository,times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository,times(1)).salva(usuario);
    }

    @Test
    void alteraStatusParaFocoEUsuarioJaEstaEmFoco() {
        Usuario usuarioFoco = DataHelper.createUsuarioFoco();
        when(usuarioRepository.buscaUsuarioPorEmail(usuarioFoco.getEmail())).thenReturn(usuarioFoco);
        when(usuarioRepository.buscaUsuarioPorId(usuarioFoco.getIdUsuario())).thenReturn(usuarioFoco);
        APIException exception = assertThrows(APIException.class,
                ()-> usuarioApplicationService.mudaStatusParaFoco(usuarioFoco.getEmail(),usuarioFoco.getIdUsuario()));
        assertEquals("Usuário já está em FOCO!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuarioFoco.getEmail());
    }

    @Test
    void alteraStatusParaFocoUsuarioNaoEncontrado(){
       UUID idUsuarioNaoEncontrado = UUID.randomUUID();
       when(usuarioRepository.buscaUsuarioPorId(idUsuarioNaoEncontrado))
               .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuário não encontrado!"));
        APIException exception = assertThrows(APIException.class,
                ()-> usuarioApplicationService.mudaStatusParaFoco(usuarioEmail,idUsuarioNaoEncontrado));

        assertEquals("Usuário não encontrado!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorId(idUsuarioNaoEncontrado);
    }
}