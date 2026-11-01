package br.com.helder.productactivation.web.controller.contract;

import br.com.helder.productactivation.web.dto.request.CreateActivationRequestDTO;
import br.com.helder.productactivation.web.dto.response.CreateActivationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/requests")
public interface ActivationControllerContract {

    @Operation(summary = "Activation processed successfully")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Activation created successfully"
        ),
        @ApiResponse(
            responseCode = "200",
            description = "Existing activation returned (idempotent request)"
        )
    })
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CreateActivationResponseDTO> createActivation(
        @Valid
        @RequestBody
        CreateActivationRequestDTO createActivationRequestDTO
    );


    @Operation(summary = "Get an activation by ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Activation found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(
                    implementation = CreateActivationResponseDTO.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Activation not found"
        )
    })
    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<CreateActivationResponseDTO> getActivation(
        @PathVariable("id") UUID id
    );

}
