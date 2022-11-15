package com.ecommerce.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    private String firstName;
    private String lastName;
    private String username;

    private String password;
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String image;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "admins_role", joinColumns = @JoinColumn(name = "admin_id"), inverseJoinColumns = @JoinColumn(name = "rode_id"))
    private Collection<Role> roles;
}
