package com.example.reservationevenement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    private String id;
    private String title;
    private String category;
    private String location;
    private LocalDate date;
    private Integer capacity;
}
