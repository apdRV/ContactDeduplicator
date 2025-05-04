package ru.apdrv.contactdeduplicator;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import ru.apdrv.contactdeduplicator.models.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactHelper {

    @SuppressLint("Range")
    public static List<Contact> getAllContacts(Context context) {
        List<Contact> allContacts = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phone = null;
                String email = null;

                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    phone = phoneCursor.getString(
                            phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    );

                    phoneCursor.close();
                }

                Cursor emailCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if (emailCursor != null && emailCursor.moveToFirst()) {
                    email = emailCursor.getString(
                            emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    emailCursor.close();
                }

                allContacts.add(new Contact(id, name, phone, email));
            }
            cursor.close();
        }
        return allContacts;
    }

    public static List<Contact> findDuplicates(List<Contact> contacts) {
        ArrayList<Contact> duplicates = new ArrayList<>();
        HashMap<Contact, String> map = new HashMap<>();
        for (var contact : contacts) {
            if (map.containsKey(contact)) {
                duplicates.add(contact);
            } else {
                map.put(contact, contact.getId());
            }
        }
        return duplicates;
    }

    public static boolean deleteContact(Context context, String contactId) {
        ContentResolver contentResolver = context.getContentResolver();

        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build();

        int deletedRows = contentResolver.delete(
                rawContactUri,
                ContactsContract.RawContacts.CONTACT_ID + " = ?",
                new String[]{contactId}
        );

        contentResolver.delete(
                ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.Contacts._ID + " = ?",
                new String[]{contactId}
        );

        return deletedRows > 0;
    }
}
