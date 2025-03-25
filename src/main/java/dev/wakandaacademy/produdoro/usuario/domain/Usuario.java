package dev.wakandaacademy.produdoro.usuario.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.Email;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString
@Document(collection = "Usuario")
@Log4j2
public class Usuario {
    @Id
    private UUID idUsuario;
    @Email
    @Indexed(unique = true)
    private String email;
    private ConfiguracaoUsuario configuracao;
    @Builder.Default
    private StatusUsuario status = StatusUsuario.FOCO;
    @Builder.Default
    private Integer quantidadePomodorosPausaCurta = 0;

    public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
        this.idUsuario = UUID.randomUUID();
        this.email = usuarioNovo.getEmail();
        this.status = StatusUsuario.FOCO;
        this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
    }

    public void mudaStatusParaPausaCurta(UUID idUsuario) {
        validaUsuaro(idUsuario);
        verificaSeJaEstaEmPausaCurta();
        this.status = StatusUsuario.PAUSA_CURTA;
    }

    private void verificaSeJaEstaEmPausaCurta() {
        if (this.status.equals(StatusUsuario.PAUSA_CURTA)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuario já esta em PAUSA CURTA.");
        }
    }

    private void validaUsuaro(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "O ID do usuário não corresponde às credenciais fornecidas.");
        }
    }

    public void mudaStatusParaPausaLonga(UUID idUsuario) {
        log.info("[inicia] usuario - mudaStatusParaPausaLonga");
        validaUsuario(idUsuario);
        validaSeUsuarioJaEstarEmPausaLonga();
        this.status = StatusUsuario.PAUSA_LONGA;
        log.info("[finaliza] usuario - mudaStatusParaPausaLonga");

    }

    private void validaSeUsuarioJaEstarEmPausaLonga() {
        log.info("[inicia] usuario - validaSeUsuarioJaEstarEmPausaLonga");
        if (this.status.equals(StatusUsuario.PAUSA_LONGA)) {
            log.info("[finaliza] APIExeception - validaSeUsuarioJaEstarEmPausaLonga");
            throw APIException.build(HttpStatus.CONFLICT, "Usuario ja eata em PAUSA LONGA");
        }
        log.info("[finaliza] usuario - validaSeUsuarioJaEstarEmPausaLonga");

    }

    private void validaUsuario(UUID idUsuario) {
        log.info("[inicia] usuario - validaUsuario");
        if (!this.idUsuario.equals(idUsuario)) {
            log.info("[finaliza] APIExeception - validaUsuario");
            throw APIException.build(HttpStatus.UNAUTHORIZED, "credencial de autenticação não é válida.");
        }
        log.info("[finaliza] usuario - validaUsuario");
    }

    public void alteraStatusParaFoco(UUID idUsuario) {
        validaUsuarioPorId(idUsuario);
        verificaStatusFoco();
    }

    public void validaUsuarioPorId(UUID idUsuario) {
        if (!this.idUsuario.equals(idUsuario)) {
            throw APIException.build(HttpStatus.UNAUTHORIZED, "Credencial de autenticação não é válida!");
        }
    }

    private void verificaStatusFoco() {
        if (this.status.equals(StatusUsuario.FOCO)) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Usuário já está em FOCO!");
        }
        mudaStatusParaFoco();
    }

    public void mudaStatusParaFoco() {
        this.status = StatusUsuario.FOCO;
    }
}