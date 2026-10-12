package com.tech365.inventory;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
public record ProductInput(
 @NotBlank @Size(max=40) @Pattern(regexp="[A-Za-z0-9][A-Za-z0-9_-]*") String sku,
 @NotBlank @Size(max=160) String name,
 @NotBlank @Size(max=80) String category,
 @NotNull @Min(0) @Max(1000000000) @JsonProperty("price_minor") Integer priceMinor,
 @NotNull @Min(0) @Max(1000000000) Integer quantity
) {}
