package ru.apdrv.contactdeduplicator.aidl;

interface IContactService {
    int checkForDuplicates();
    int removeDuplicates();
}