package com.example.distributedlock.lock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "shedlock")
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Lock {

  @Id
  @Column(name = "name")
  private String name;

  @NotNull
  @Column(name = "lock_until", columnDefinition = "TIMESTAMP")
  private Timestamp lockUntil;

  @NotNull
  @Column(name = "locked_at", columnDefinition = "TIMESTAMP")
  private Timestamp lockedAt;

  @NotNull
  @Column(name = "locked_by")
  private String lockedBy;
}
