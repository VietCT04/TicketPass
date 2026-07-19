package com.ticketpass.api.auth;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @JsonProperty("display_name")
    @NotBlank
    @Size(max = 120)
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    @JsonAnySetter
    void rejectUnknownField(String fieldName, Object ignoredValue) {
        throw new IllegalArgumentException("Unknown request field: " + fieldName);
    }
}
