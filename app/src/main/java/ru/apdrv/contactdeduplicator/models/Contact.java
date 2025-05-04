package ru.apdrv.contactdeduplicator.models;

import java.util.Objects;

public class Contact {
    final private String id;
    final private String name;
    final private String phone;
    final private String email;

    public Contact(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        Contact contact = (Contact) o;
        return name.equals(contact.name) && email.equals(contact.email) && phone.equals((contact.phone));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email);
    }
}
