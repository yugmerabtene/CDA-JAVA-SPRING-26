package com.cda.cdajava.dao;

import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;

import java.util.Optional;

public interface RoleDao {

    Optional<Role> findByName(RoleName name);
}
