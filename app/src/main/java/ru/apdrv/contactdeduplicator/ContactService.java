package ru.apdrv.contactdeduplicator;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import ru.apdrv.contactdeduplicator.aidl.IContactService;
import ru.apdrv.contactdeduplicator.models.Contact;

import java.util.List;

public class ContactService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    private final IContactService.Stub binder = new IContactService.Stub() {
        @Override
        public int checkForDuplicates() {
            Log.d("Deleting", "Check for duplicates");
            List<Contact> allContacts = ContactHelper.getAllContacts(ContactService.this);
            List<Contact> duplicates = ContactHelper.findDuplicates(allContacts);
            return duplicates.size();
        }

        @Override
        public int removeDuplicates() {
            Log.d("Deleting", "Removing duplicates");
            List<Contact> allContacts = ContactHelper.getAllContacts(ContactService.this);
            List<Contact> duplicates = ContactHelper.findDuplicates(allContacts);

            int deletedCount = 0;
            for (var contact : duplicates) {
                if (ContactHelper.deleteContact(ContactService.this, contact.getId())) {
                    ++deletedCount;
                }
            }
            return deletedCount;
        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
