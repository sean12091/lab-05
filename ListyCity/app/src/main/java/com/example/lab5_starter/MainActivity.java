package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    // Firestore
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // List + Adapter
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Firestore init
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // Snapshot listener -> ALWAYS keep ListView synced with Firestore
        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Listen failed", error);
                return;
            }

            cityArrayList.clear();

            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    String name = doc.getString("name");
                    String province = doc.getString("province");

                    if (name != null && province != null) {
                        City city = new City(name, province);
                        city.setId(doc.getId()); // store doc id for deletion
                        cityArrayList.add(city);
                    }
                }
            }

            cityArrayAdapter.notifyDataSetChanged();
        });

        // Add City button -> open dialog
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        // Tap -> open details dialog (optional, keep your existing behavior)
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            if (city != null) {
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(), "City Details");
            }
        });

        // ✅ Long press -> confirm delete -> delete from Firestore
        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            if (city == null) return true;

            new AlertDialog.Builder(this)
                    .setTitle("Delete City")
                    .setMessage("Delete " + city.getName() + " (" + city.getProvince() + ")?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteCity(city))
                    .setNegativeButton("Cancel", null)
                    .show();

            return true; // consumed
        });
    }

    @Override
    public void addCity(City city) {
        // Create a new document with an auto-generated id
        citiesRef.add(city)
                .addOnSuccessListener(ref -> Log.d("Firestore", "City added with id: " + ref.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Add failed", e));
        // No need to manually add to array; snapshot listener will refresh list.
    }

    @Override
    public void updateCity(City city, String title, String province) {
        // Update local object
        city.setName(title);
        city.setProvince(province);

        // If we have the doc id, update the same document
        if (city.getId() == null) {
            Toast.makeText(this, "Can't update: missing Firestore id", Toast.LENGTH_SHORT).show();
            return;
        }

        citiesRef.document(city.getId())
                .set(city)
                .addOnSuccessListener(unused -> Log.d("Firestore", "City updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Update failed", e));
    }

    // ✅ Participation requirement: deletion persists
    private void deleteCity(City city) {
        if (city.getId() == null) {
            Toast.makeText(this, "Can't delete: missing Firestore id", Toast.LENGTH_SHORT).show();
            return;
        }

        citiesRef.document(city.getId())
                .delete()
                .addOnSuccessListener(unused -> Log.d("Firestore", "City deleted"))
                .addOnFailureListener(e -> Log.e("Firestore", "Delete failed", e));
        // Snapshot listener will remove it from the ListView automatically.
    }
}
