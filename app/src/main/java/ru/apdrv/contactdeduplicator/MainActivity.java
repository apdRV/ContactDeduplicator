package ru.apdrv.contactdeduplicator;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ru.apdrv.contactdeduplicator.aidl.IContactService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private IContactService contactService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            contactService = IContactService.Stub.asInterface(service);
            Log.d("Service", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Service", "Service disconnected");
            contactService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cleanButton = findViewById(R.id.clean_button);

        bindService(
                new Intent(this, ContactService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );

        cleanButton.setOnClickListener(v -> start());
    }

    private void start() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Deleting", "Doesn't have needed permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    101);
        } else {
            Log.d("Deleting", "Got permissions");
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    int duplicatesQuantity = contactService.checkForDuplicates();
                    Log.println(Log.INFO, "Deleting", "Quantity of duplicates is  " + duplicatesQuantity);
                    if (duplicatesQuantity > 0) {
                        return contactService.removeDuplicates() + " duplicates removed";
                    }
                    return "No duplicates found";
                } catch (Exception e) {
                    return "Error";
                }
            }, Executors.newSingleThreadExecutor());
            future.thenAcceptAsync(result ->
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show()
            , runOnUiThreadExecutor());
        }

    }

    private Executor runOnUiThreadExecutor() {
        return this::runOnUiThread;
    }

}