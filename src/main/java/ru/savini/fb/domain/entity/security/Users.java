package ru.savini.fb.domain.entity.security;

import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
public class Users {
    @Id String username;
    @Column(nullable = false) String password;
    @Column(columnDefinition="tinyint(1) default 1", nullable = false) Boolean enabled;

    @OneToMany(mappedBy="username")
    private Set<Authorities> authorities;
}
