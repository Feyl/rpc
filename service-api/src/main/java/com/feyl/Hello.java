package com.feyl;

import lombok.*;

import java.io.Serializable;

/**
 * @author Feyl
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Hello implements Serializable {
    private String message;
    private String description;
}
