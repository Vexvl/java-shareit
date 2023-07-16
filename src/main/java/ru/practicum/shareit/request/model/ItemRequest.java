package ru.practicum.shareit.request.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "requests")
@Data
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
}