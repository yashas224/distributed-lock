package com.example.distributedlock.lock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LockRepository extends JpaRepository<Lock, String> {

  Lock findByName(String name);
}
