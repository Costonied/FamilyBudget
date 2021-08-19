package ru.savini.fb.domain.entity.security;

import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Authorities {
    @Id String username;
    @Column(nullable = false) String authority;
}
