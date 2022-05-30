package com.ark.futsalbookedapps.Globals;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReferenceDatabase {
    public static DatabaseReference referenceRoot = FirebaseDatabase.getInstance().getReference();
    public static DatabaseReference referenceAccount = referenceRoot.child("account");
    public static DatabaseReference referenceProviderField = referenceRoot.child("provider_field");
    public static DatabaseReference referenceField = referenceRoot.child("field");
    public static DatabaseReference referenceBooked = referenceRoot.child("booked_field");
    public static DatabaseReference referenceReview = referenceRoot.child("review_provider");
}
