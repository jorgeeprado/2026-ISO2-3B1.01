package es.uclm.esi.iso2.sescam.common.ports;

import es.uclm.esi.iso2.sescam.common.dto.UsuarioDTO;

/**
 * Puerto para la validación federada de identidad con el SSO Corporativo del SESCAM (RNF-04).
 */
public interface ISsoClient {

    /**
     * Valida un token federado (JWT / SAML 2.0) en un tiempo menor o igual a 300 ms.
     */
    UsuarioDTO validarTokenFederado(String token);
}

