package com.rev.app.mapper;

import com.rev.app.dto.UserDTO;
import com.rev.app.entity.User;

public class UserMapper {

   public static UserDTO toDTO(User user) {
      if (user == null)
         return null;

      UserDTO dto = new UserDTO();
      dto.setId(user.getId());
      dto.setName(user.getName());
      dto.setEmail(user.getEmail());
      dto.setPhone(user.getPhone());
      dto.setAddress(user.getAddress());
      dto.setGstNumber(user.getGstNumber());
      dto.setSellerDistributorName(user.getSellerDistributorName());
      dto.setRole(user.getRole() != null ? user.getRole().name() : null);
      if (user.getAdditionalAddresses() != null) {
         dto.setAdditionalAddresses(new java.util.ArrayList<>(user.getAdditionalAddresses()));
      }
      return dto;
   }

   public static User toEntity(UserDTO dto) {
      if (dto == null)
         return null;

      User user = new User();
      user.setId(dto.getId());
      user.setName(dto.getName());
      user.setEmail(dto.getEmail());
      user.setPhone(dto.getPhone());
      user.setAddress(dto.getAddress());
      user.setPassword(dto.getPassword());
      user.setGstNumber(dto.getGstNumber());
      user.setSellerDistributorName(dto.getSellerDistributorName());
      if (dto.getRole() != null) {
         try {
            user.setRole(com.rev.app.entity.enums.Role.valueOf(dto.getRole()));
         } catch (IllegalArgumentException e) {
            // Invalid role
         }
      }
      if (dto.getAdditionalAddresses() != null) {
         user.setAdditionalAddresses(new java.util.ArrayList<>(dto.getAdditionalAddresses()));
      } else {
         user.setAdditionalAddresses(new java.util.ArrayList<>());
      }
      return user;
   }
}