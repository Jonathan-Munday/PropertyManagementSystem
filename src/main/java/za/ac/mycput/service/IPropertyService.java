/*
 * IPropertyService.java
 * Service interface for Property entity.
 * Author: Jonathan Munday (230677797)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.Property;
import java.util.List;

public interface IPropertyService extends IService<Property, String> {
    List<Property> getAll();
}
