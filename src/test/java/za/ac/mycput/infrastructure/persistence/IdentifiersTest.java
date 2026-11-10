package za.ac.mycput.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.exception.DomainRuleException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentifiersTest {

    @Test
    void parsesAndFormatsAUuid() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa");
        assertEquals(id, Identifiers.parse(id.toString()));
        assertEquals(id.toString(), Identifiers.format(id));
    }

    @Test
    void rejectsAnIdThatIsNotAUuid() {
        assertThrows(DomainRuleException.class, () -> Identifiers.parse("P-001"));
        assertThrows(DomainRuleException.class, () -> Identifiers.parse(" "));
    }
}
