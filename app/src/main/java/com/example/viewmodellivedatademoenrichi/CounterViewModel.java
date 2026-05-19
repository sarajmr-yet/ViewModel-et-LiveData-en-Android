package com.example.viewmodellivedatademoenrichi;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * CounterViewModel - LAB 18 : ViewModel et LiveData en Android
 *
 * POURQUOI ViewModel ?
 * - Android détruit et recrée l'Activity à chaque rotation d'écran
 * - Les variables d'instance de l'Activity sont PERDUES à chaque rotation
 * - ViewModel survit automatiquement à la destruction/re-création de l'Activity
 * - Android le garde en mémoire dans un ViewModelStore lié au LifecycleOwner
 *
 * POURQUOI LiveData ?
 * - Observable qui respecte le cycle de vie (lifecycle-aware)
 * - Ne met à jour l'UI QUE si l'Activity est en état STARTED ou RESUMED
 * - → zéro crash, zéro memory leak
 *
 * MutableLiveData vs LiveData :
 * - MutableLiveData : on peut modifier la valeur (setValue / postValue)
 * - LiveData : lecture seule (exposé à l'extérieur pour sécurité)
 *
 * setValue vs postValue :
 * - setValue() : à appeler depuis le thread principal (Main Thread)
 * - postValue() : à appeler depuis un thread background
 */
public class CounterViewModel extends ViewModel {

    // MutableLiveData : modifiable en interne
    // On utilise Integer (objet) et non int (primitif) car LiveData nécessite un objet
    private final MutableLiveData<Integer> count = new MutableLiveData<>();

    // Constructeur : initialisation du compteur à 0
    public CounterViewModel() {
        count.setValue(0);
    }

    /**
     * Expose LiveData en lecture seule à l'extérieur.
     * L'Activity observe ce LiveData mais ne peut pas le modifier directement.
     */
    public MutableLiveData<Integer> getCount() {
        return count;
    }

    /**
     * Incrémente le compteur de 1.
     * getValue() peut être null en théorie → on vérifie avec une valeur par défaut.
     */
    public void increment() {
        Integer current = count.getValue();
        count.setValue(current != null ? current + 1 : 1);
    }

    /**
     * Décrémente le compteur de 1.
     * On peut décider d'autoriser les valeurs négatives (ici oui, pour démonstration).
     */
    public void decrement() {
        Integer current = count.getValue();
        count.setValue(current != null ? current - 1 : -1);
    }

    /**
     * Réinitialise le compteur à 0.
     */
    public void reset() {
        count.setValue(0);
    }

    /**
     * onCleared() est appelé par Android quand l'Activity est DÉFINITIVEMENT détruite
     * (ex: l'utilisateur quitte l'app, pas lors d'une rotation).
     * Idéal pour libérer des ressources (cancel coroutines, unsubscribe, etc.)
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        // Ici on pourrait annuler des tâches background, fermer des connexions, etc.
        android.util.Log.d("CounterViewModel", "onCleared() appelé - Activity définitivement détruite");
    }
}
