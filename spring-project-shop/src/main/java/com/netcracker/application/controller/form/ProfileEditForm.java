package com.netcracker.application.controller.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileEditForm {
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
}
