package za.ac.mycput.domain.repository;
import za.ac.mycput.domain.repository.IManagerRepository;

/*
  ManagerRepositoryTest.java
  Entity for the Manager
  Author: Anas Pansari (230195342)
  Date: 25th March 2026
 */

import za.ac.mycput.domain.model.Manager;
import za.ac.mycput.domain.factory.ManagerFactory;
import za.ac.mycput.infrastructure.persistence.memory.ManagerRepositoryImpl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

 class ManagerRepositoryTest {

     private IManagerRepository repository = new ManagerRepositoryImpl();

     private Manager manager = ManagerFactory.createManager("Jacob Parker", "072183456", "EMP02");


     @Test
     void testCreate() {
         Manager created = repository.create(manager);
         assertNotNull(created);

     }

     @Test
     void testRead() {
         repository.create(manager);
         Manager read = repository.read(manager.getManagerId());
         assertNotNull(read);
     }

     @Test
     void testDelete() {
         repository.create(manager);
         boolean deleted = repository.delete(manager.getManagerId());
         assertTrue(deleted);


     }

 }
