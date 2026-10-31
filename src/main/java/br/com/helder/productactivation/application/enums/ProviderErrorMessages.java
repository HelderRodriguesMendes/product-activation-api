package br.com.helder.productactivation.application.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProviderErrorMessages {

    BAD_REQUEST("Provider rejected the activation request"),
    CLIENT_ERROR("Unexpected client error returned by provider"),
    UNAVAILABLE("Provider is unavailable"),
    TIMEOUT("Provider activation request timed out"),
    CONNECTION_ERROR("Unable to communicate with provider");

    private final String message;
}