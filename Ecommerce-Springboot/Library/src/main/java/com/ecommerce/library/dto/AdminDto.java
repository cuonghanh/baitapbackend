package com.ecommerce.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDto {
    @Size(min = 3,max = 10,message = " tu 3 den 10 ky tu")
    private String firstName;
    @Size(min = 3,max = 10,message = " tu 3 den 10 ky tu")
    private String lastName;

    private String username;
    @Size(min = 5,max = 15,message = " tu 3 den 10 ky tu")

    private String password;

    private String repeatPassword;


}
