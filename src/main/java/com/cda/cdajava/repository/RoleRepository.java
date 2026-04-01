package com.cda.cdajava.repository;

import com.cda.cdajava.model.Role;
import com.cda.cdajava.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
