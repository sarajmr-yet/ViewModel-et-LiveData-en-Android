package com.example.viewmodellivedatademoenrichi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

/**
 * MainActivity - LAB 18 : ViewModel et LiveData en Android
 * Version AVEC ViewModel + LiveData (Partie 2 - 20 min)
 *
 * ARCHITECTURE MVVM :
 * - Model     : données (le compteur dans ViewModel)
 * - View      : UI (activity_main.xml + cette Activity)
 * - ViewModel : logique métier + exposition des données via LiveData
 *
 * FLUX DE DONNÉES :
 * User → clique bouton → MainActivity appelle ViewModel.increment()
 *      → ViewModel met à jour MutableLiveData
 *      → LiveData notifie l'Observer dans MainActivity
 *      → MainActivity met à jour le TextView
 *
 * CE QUI SE PASSE À LA ROTATION :
 * 1. Android appelle onDestroy() sur l'Activity
 * 2. Le ViewModel SURVIT (Android le garde dans ViewModelStore)
 * 3. Android recrée l'Activity (onCreate())
 * 4. ViewModelProvider retrouve LE MÊME ViewModel (pas une nouvelle instance)
 * 5. L'Observer re-souscrit → reçoit immédiatement la dernière valeur
 * 6. L'UI affiche la valeur correcte → 0 perte de données !
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // ViewModel : survit aux rotations
    private CounterViewModel viewModel;

    // Vues UI
    private TextView tvCount;
    private Button btnIncrement;
    private Button btnDecrement;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate() - Activity (re)créée");

        // ── 1. Récupérer les vues ────────────────────────────────────────────
        tvCount      = findViewById(R.id.tvCount);
        btnIncrement = findViewById(R.id.btnIncrement);
        btnDecrement = findViewById(R.id.btnDecrement);
        btnReset     = findViewById(R.id.btnReset);

        // ── 2. Obtenir le ViewModel via ViewModelProvider ────────────────────
        //
        // ViewModelProvider(this) :
        //   - "this" = LifecycleOwner (l'Activity)
        //   - Si le ViewModel existe déjà (après rotation) → retourne la même instance
        //   - Si c'est la première fois → crée une nouvelle instance
        //
        // JAMAIS faire : new CounterViewModel() → perdrait les données à la rotation !
        viewModel = new ViewModelProvider(this).get(CounterViewModel.class);

        Log.d(TAG, "ViewModel obtenu : " + viewModel.hashCode());

        // ── 3. Observer le LiveData ──────────────────────────────────────────
        //
        // observe(this, observer) :
        //   - "this" = LifecycleOwner → LiveData sait quand l'Activity est active
        //   - L'observer est automatiquement retiré quand l'Activity est détruite
        //   - → zéro memory leak
        //
        // L'observer est appelé :
        //   a) Immédiatement avec la valeur actuelle (même après rotation !)
        //   b) À chaque fois que la valeur change
        viewModel.getCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer newCount) {
                // Cette méthode est appelée SEULEMENT si l'Activity est STARTED ou RESUMED
                // → zéro crash même si l'Activity est en background
                Log.d(TAG, "LiveData onChanged() → nouvelle valeur : " + newCount);

                // Mise à jour de l'UI
                tvCount.setText(String.valueOf(newCount));

                // Bonus : changer la couleur selon la valeur
                if (newCount > 0) {
                    tvCount.setTextColor(0xFF4CAF50); // Vert
                } else if (newCount < 0) {
                    tvCount.setTextColor(0xFFF44336); // Rouge
                } else {
                    tvCount.setTextColor(0xFF3F51B5); // Bleu (neutre)
                }
            }
        });

        // ── 4. Configurer les boutons ────────────────────────────────────────
        //
        // L'Activity ne modifie PAS directement les données.
        // Elle délègue au ViewModel → séparation des responsabilités (MVVM).

        btnIncrement.setOnClickListener(v -> {
            Log.d(TAG, "Bouton INCRÉMENTER cliqué");
            viewModel.increment();
            // Pas besoin de mettre à jour l'UI ici → l'Observer s'en charge !
        });

        btnDecrement.setOnClickListener(v -> {
            Log.d(TAG, "Bouton DÉCRÉMENTER cliqué");
            viewModel.decrement();
        });

        btnReset.setOnClickListener(v -> {
            Log.d(TAG, "Bouton RÉINITIALISER cliqué");
            viewModel.reset();
        });
    }

    // ── Cycle de vie pour comprendre ce qui se passe ────────────────────────

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() - LiveData commence à notifier");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - Activity active");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() - LiveData peut être suspendu");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() - LiveData ne notifie plus (lifecycle-aware)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() - Activity détruite (mais ViewModel survit si rotation !)");
        Log.d(TAG, "isFinishing() = " + isFinishing());
        // isFinishing() = true → destruction définitive (user quitte)
        // isFinishing() = false → rotation d'écran (ViewModel survit)
    }
}
