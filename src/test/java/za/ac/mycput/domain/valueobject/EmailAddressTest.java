package za.ac.mycput.domain.valueobject;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.exception.DomainRuleException;

import static org.junit.jupiter.api.Assertions.*;

class EmailAddressTest {

    @Test
    void acceptsANormalAddress() {
        EmailAddress email = new EmailAddress("  ada@portico.test ");
        assertEquals("ada@portico.test", email.value());
        assertEquals(new EmailAddress("Ada@portico.test"), email);
    }

    @Test
    void rejectsBlankAndMalformedAddresses() {
        assertThrows(DomainRuleException.class, () -> new EmailAddress(null));
        assertThrows(DomainRuleException.class, () -> new EmailAddress(""));
        assertThrows(DomainRuleException.class, () -> new EmailAddress("ada-portico"));
    }
}
