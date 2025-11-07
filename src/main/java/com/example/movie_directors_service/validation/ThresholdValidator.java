package com.example.movie_directors_service.validation;

import com.example.movie_directors_service.exception.InvalidParameterException;
import org.springframework.stereotype.Component;

@Component
public class ThresholdValidator {

    public long parseAndValidate(String thresholdParam) {
        if (thresholdParam == null || thresholdParam.trim().isEmpty()) {
            throw new InvalidParameterException("Parameter 'threshold' cannot be empty or blank");
        }

        try {
            long threshold = Long.parseLong(thresholdParam.trim());
            if (threshold < 0) {
                throw new InvalidParameterException("Threshold must be non-negative");
            }
            return threshold;
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(
                    "Invalid value for parameter 'threshold': '" + thresholdParam + "' must be a valid number",
                    e);
        }
    }
}

